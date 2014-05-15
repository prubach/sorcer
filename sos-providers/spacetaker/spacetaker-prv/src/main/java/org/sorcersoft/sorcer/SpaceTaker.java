/*
 * Copyright 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sorcersoft.sorcer;

import com.sun.jini.admin.DestroyAdmin;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace05;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.ConfigEntry;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.provider.outrigger.SpaceTakerConfiguration;
import sorcer.core.service.IServiceBuilder;
import sorcer.river.TX;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;

public class SpaceTaker implements DestroyAdmin {
    private static Logger log = LoggerFactory.getLogger(SpaceTaker.class);

    private Set<Class> currentProviders = Collections.newSetFromMap(new ConcurrentHashMap<Class, Boolean>());

    @Inject
    private ScheduledExecutorService executor;

    private ThreadPoolExecutor spaceExecutor;

    //@ThreadFactoryConfig
    @Inject
    private ThreadFactory threadFactory;

    @ConfigEntry(value = "workerCount")
    public int workerCount = 1;

    @ConfigEntry(value = "rate")
    protected int rate = 500;

    protected static long TRANSACTION_LEASE_TIME = 1000 * 60 * 1; // 1 minute

    @ConfigEntry(value = "workerTransactionLeaseTime", required = false)
    private long transactionLeaseTimeout = TRANSACTION_LEASE_TIME;

    private ScheduledFuture<?> workerFuture;

    @PostConstruct
    public void start() {
        spaceExecutor = new ThreadPoolExecutor(workerCount, workerCount, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(workerCount), threadFactory);
        workerFuture = executor.scheduleAtFixedRate(new SpaceTakerWorker(), 0, rate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() throws RemoteException {
        workerFuture.cancel(false);
    }

    protected Map<Object, SpaceTakerData> beans = new HashMap<Object, SpaceTakerData>();

    public void addService(IServiceBuilder builder, Object bean, SpaceTakerConfiguration cfg) {
        beans.put(bean, new SpaceTakerData(builder, cfg));
    }

    public void removeService(Object bean) {
        beans.remove(bean);
    }


    class SpaceTakerWorker implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(SpaceTakerWorker.class);
        private JavaSpace05 space;
        ThreadPoolExecutor executor;
        int workerCount;

        boolean isTransactional;

        private long transactionLeaseTimeout = rate;

        private int getFreeProviders() {
            return workerCount - executor.getActiveCount();
        }

        private Collection<ExertionEnvelop> getAvailableProviders() {
            Collection<ExertionEnvelop> result = new LinkedList<ExertionEnvelop>();
            for (SpaceTakerData e : beans.values())
                result.addAll(e.entries);
            return result;
        }

        @Override
        public void run() {
            while (getFreeProviders() > 0)
                handleSpace();
        }

        private void handleSpace() {
            Transaction.Created tx = null;
            Transaction transaction = null;
            if (isTransactional) {
                tx = TX.createTransaction(transactionLeaseTimeout);
                if (tx == null) {
                    logger.warn("Could not create transaction");
                    return;
                } else {
                    transaction = tx.transaction;
                }
            }
            try {
                ExertionEnvelop ee;
                try {
                    Collection<ExertionEnvelop> taken = space.take(getAvailableProviders(), transaction, rate, 1);
                    if (taken.isEmpty())
                        return;

                    ee = taken.iterator().next();
                    executor.submit(new SpaceTaskWorker(space, ee, tx));
                } catch (Exception ex) {
                    logger.warn("Problem with SpaceTaker: ", ex);
                    if (tx != null) {
                        try {
                            TX.abortTransaction(tx);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tx = null;
                    }
                }
            } finally {
                if (tx != null)
                    try {
                        tx.transaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }

    }
}

