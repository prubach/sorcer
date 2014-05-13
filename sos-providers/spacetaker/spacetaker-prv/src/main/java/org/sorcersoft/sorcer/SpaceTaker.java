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
import sorcer.core.provider.Provider;
import sorcer.core.provider.outrigger.SpaceTakerConfiguration2;
import sorcer.river.TX;
import sorcer.service.ConfigurationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
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

    @ConfigEntry(value = "workerCount", required = false)
    public int workerCount = 1;

    @ConfigEntry(value = "rate", required = false)
    private int rate = 500;

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

    public void startSpaceTakers() throws ConfigurationException, RemoteException {
/*
        if (workerTransactional && tManager == null) {
            log.warn("ERROR: no transactional manager found....");
        }
        if (publishedServiceTypes == null || publishedServiceTypes.length == 0) {

            log.warn("ERROR: no published interfaces found....");
        }

        ExertionEnvelop envelop;

        log.debug("*** provider worker count: " + workerCount
                + ", spaceTransactional: " + workerTransactional);
        log.info("publishedServiceTypes.length = "
                + publishedServiceTypes.length);
        log.info(Arrays.toString(publishedServiceTypes));

        // create a pair of taker threads for each published interface
        SpaceTaker worker = null;

        for (int i = 0; i < publishedServiceTypes.length; i++) {
            // spaceWorkerPool = Executors.newFixedThreadPool(workerCount);
            ConfigurableThreadFactory factory = new ConfigurableThreadFactory();
            factory.setNameFormat("SpaceTaker-" + spaceName + "-thread-%2$d");
            spaceWorkerPool = new ThreadPoolExecutor(workerCount,
                    maximumPoolSize > workerCount ? maximumPoolSize
                            : workerCount, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(
                            (queueSize == 0 ? workerCount : queueSize)), factory
            );
            spaceHandlingPools.add(spaceWorkerPool);
            // SORCER.ANY is required for a ProviderWorker
            // to avoid matching to any provider name
            // that is Java null matching everything
            envelop = ExertionEnvelop.getTemplate(publishedServiceTypes[i],
                    SorcerConstants.ANY);
            if (spaceReadiness) {
                worker = new SpaceIsReadyTaker(new SpaceTaker.SpaceTakerData(
                        envelop, memberInfo, provider, spaceName, spaceGroup,
                        workerTransactional, queueSize == 0), spaceWorkerPool);
                spaceTakers.add(worker);
            } else {
                worker = new SpaceTaker(new SpaceTaker.SpaceTakerData(envelop,
                        memberInfo, provider, spaceName, spaceGroup,
                        workerTransactional, queueSize == 0), spaceWorkerPool);
                spaceTakers.add(worker);
            }
            Thread sith = new Thread(interfaceGroup, worker);
            sith.setDaemon(true);
            sith.start();
            logger.info("*** space worker-" + i + " started for: "
                    + publishedServiceTypes[i]);
            // System.out.println("space template: " +
            // envelop.describe());

            if (!matchInterfaceOnly) {
                // spaceWorkerPool = Executors.newFixedThreadPool(workerCount);
                spaceWorkerPool = new ThreadPoolExecutor(workerCount,
                        maximumPoolSize > workerCount ? maximumPoolSize
                                : workerCount, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(
                                (queueSize == 0 ? workerCount : queueSize))
                );
                spaceHandlingPools.add(spaceWorkerPool);
                envelop = ExertionEnvelop.getTemplate(publishedServiceTypes[i],
                        getProviderName());
                if (spaceReadiness) {
                    worker = new SpaceIsReadyTaker(
                            new SpaceTaker.SpaceTakerData(envelop, memberInfo,
                                    provider, spaceName, spaceGroup,
                                    workerTransactional, queueSize == 0),
                            spaceWorkerPool
                    );
                    spaceTakers.add(worker);
                } else {
                    worker = new SpaceTaker(new SpaceTaker.SpaceTakerData(
                            envelop, memberInfo, provider, spaceName,
                            spaceGroup, workerTransactional, queueSize == 0),
                            spaceWorkerPool
                    );
                    spaceTakers.add(worker);
                }
                Thread snth = new Thread(namedGroup, worker);
                snth.setDaemon(true);
                snth.start();
                logger.info("*** named space worker-" + i + " started for: "
                        + publishedServiceTypes[i] + ":" + getProviderName());
            }
        }
*/
    }

    public void addService(Object bean, SpaceTakerConfiguration2 cfg, Set<Class> publicIfaces) {

    }

    public void removeService(Object bean) {

    }


    class SpaceTakerWorker implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(SpaceTakerWorker.class);
        private JavaSpace05 space;
        ThreadPoolExecutor executor;
        int workerCount;

        boolean isTransactional;
        long rate;

        private long transactionLeaseTimeout;

        private int getFreeProviders() {
            return workerCount - executor.getActiveCount();
        }

        private SpaceTakerData[] getAvailableProviders() {
            return null;
        }

        private Collection<ExertionEnvelop> getAvailableProviders2() {
            return null;
        }

        @Override
        public void run() {
            int freeProviders = getFreeProviders();
            if (freeProviders <= 0)
                return;

            SpaceTakerData data;
            Transaction.Created txnCreated = null;
            Transaction transaction = null;
            if (isTransactional) {
                txnCreated = TX.createTransaction(transactionLeaseTimeout);
                if (txnCreated == null) {
                    logger.warn("Could not create transaction");
                    return;
                } else {
                    transaction = txnCreated.transaction;
                }
            }


            try {
                Collection<ExertionEnvelop> taken = space.take(getAvailableProviders2(), transaction, rate, 1);
                if (taken.isEmpty())
                    return;

                ExertionEnvelop ee;
                ee = taken.iterator().next();


                // after 'take' timeout abort transaction and sleep for a while
                // before 'taking' the next exertion
                if (ee == null) {
                    return;
                }
                spaceExecutor.execute(new SpaceWorker(ee, txnCreated));
            } catch (Exception ex) {
                logger.warn("Problem with SpaceTaker: ", ex);
            }
        }
    }
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
