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
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.space.JavaSpace05;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.BeanListener;
import sorcer.config.ConfigEntry;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.provider.Provider;
import sorcer.core.service.IServiceBuilder;
import sorcer.core.service.ThreadFactoryConfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.rmi.RemoteException;
import java.util.concurrent.*;
import java.util.logging.Level;

public class SpaceTaker implements BeanListener, DestroyAdmin {
    private static Logger log = LoggerFactory.getLogger(SpaceTaker.class);

    @Inject
    private ScheduledExecutorService executor;

    private ThreadPoolExecutor spaceExecutor;

    @ThreadFactoryConfig
    private ThreadFactory threadFactory;

    @ConfigEntry(value = "workerCount", required = false)
    public int workerCount = 1;

    @ConfigEntry(value = "rate", required = false)
    private int rate = 500;

    private ScheduledFuture<?> workerFuture;

    @PostConstruct
    public void start() {
        spaceExecutor = new ThreadPoolExecutor(workerCount, workerCount, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(workerCount), threadFactory);
        workerFuture = executor.scheduleAtFixedRate(new SpaceTakerWorker(), 0, rate, TimeUnit.MILLISECONDS);
    }


    @Override
    public void preProcess(IServiceBuilder serviceBuilder) {

    }

    @Override
    public void preProcess(IServiceBuilder serviceBuilder, Object bean) {
        serviceBuilder.getProviderConfiguration().getEntry(getClass().getName(), )
    }

    @Override
    public void destroy(IServiceBuilder serviceBuilder, Object bean) {

    }

    @Override
    public void destroy() throws RemoteException {
        workerFuture.cancel(false);
    }
}

class SpaceTakerWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SpaceTakerWorker.class);
    private JavaSpace05 space;
    ThreadPoolExecutor executor;
    int workerCount;

    boolean isTransactional;
    long rate;

    private int getFreeProviders() {
        return workerCount - executor.getActiveCount();
    }

    @Override
    public void run() {
        int freeProviders = getFreeProviders();
        if (freeProviders <= 0)
            return;

        ExertionEnvelop ee;
        SpaceTakerData data;
        try {

            Transaction transaction = null;
            if (isTransactional) {
                Transaction.Created txnCreated = createTransaction();
                if (txnCreated == null) {
                    logger.warn("Could not create transaction");
                    return;
                } else {
                    transaction = txnCreated.transaction;
                }
            }
            ee = (ExertionEnvelop) space.take(data.entry,
                    transaction, rate);


            // after 'take' timeout abort transaction and sleep for a while
            // before 'taking' the next exertion
            if (ee == null) {
                if (txnCreated != null) {

                    //doLog("\taborting txn...", threadId, txnCreated);
                    abortTransaction(txnCreated);
                    //doLog("\tDONE aborting txn.", threadId, txnCreated);
                    try {
                        Thread.sleep(spaceTimeout / 2);
                    } catch (InterruptedException ie) {
                        keepGoing = false;
                        break;
                    }
                }

                txnCreated = null;
                continue;
            }
            ExecutorService spaceExecutor;
            spaceExecutor.execute(new SpaceWorker(ee, txnCreated));
        } catch (Exception ex) {
            //logger.info("END LOOP SPACE TAKER EXCEPTION");
            //ex.printStackTrace();
            log.log(Level.SEVERE, "Problem with SpaceTaker: ", ex);

        }

    }

    synchronized public Transaction.Created createTransaction() {
        LeaseRenewalManager leaseManager;
        if (leaseManager == null) {
            leaseManager = new LeaseRenewalManager();
        }
        try {
            TransactionManager tManager = TransactionManagerAccessor
                    .getTransactionManager();
            if (tManager == null) {
                return null;
            }
            Transaction.Created created = TransactionFactory.create(tManager,
                    transactionLeaseTimeout);


            leaseManager.renewFor(created.lease, Lease.FOREVER, transactionLeaseTimeout, this);

            return created;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LeaseDeniedException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class SpaceTakerConfiguration {
    @ConfigEntry(value = "workerTransactional", required = false)
    public boolean workerTransactional;

    @ConfigEntry(value = "matchInterfaceOnly", required = false)
    public boolean matchInterfaceOnly;

    @ConfigEntry(value = "spaceEnabled", required = false)
    private boolean spaceEnabled;

}

class SpaceTakerData {
    public ExertionEnvelop entry;
    public Provider provider;
    public String spaceName;
    public String spaceGroup;
    public boolean workerTransactional;

    public SpaceTakerData() {
    }

    public SpaceTakerData(ExertionEnvelop entry,
                          Provider provider, String spaceName, String spaceGroup,
                          boolean workerIsTransactional) {
        this.provider = provider;
        this.entry = entry;
        this.spaceName = spaceName;
        this.spaceGroup = spaceGroup;
        this.workerTransactional = workerIsTransactional;
    }

    public String toString() {
        return entry.describe();
    }
}
