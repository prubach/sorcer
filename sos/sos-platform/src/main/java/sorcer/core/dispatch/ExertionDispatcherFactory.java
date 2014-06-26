/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

package sorcer.core.dispatch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.jini.core.lease.Lease;
import net.jini.lease.LeaseRenewalManager;
import sorcer.core.deploy.Deployment;
import sorcer.core.monitor.MonitorSessionManagement;
import sorcer.core.monitor.MonitoringManagement;
import sorcer.core.monitor.MonitoringSession;
import sorcer.core.provider.Cataloger;
import sorcer.core.Dispatcher;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.Jobs;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.core.provider.exertmonitor.MonitorSession;
import sorcer.service.*;
import sorcer.service.monitor.MonitorUtil;

import static sorcer.service.monitor.MonitorUtil.getMonitoringSession;

/**
 * This class creates instances of appropriate subclasses of Dispatcher. The
 * appropriate subclass is determined by calling the ServiceJob object's
 */
public class ExertionDispatcherFactory implements DispatcherFactory {
    public static Cataloger catalog; // The service catalog object
    private final static Logger logger = Logger.getLogger(ExertionDispatcherFactory.class.getName());
    private ProviderProvisionManager providerProvisionManager = ProviderProvisionManager.getInstance();

    private LokiMemberUtil loki;

    public static final long LEASE_RENEWAL_PERIOD = 1 * 1000 * 60L;
    public static final long DEFAULT_TIMEOUT_PERIOD = 1 * 1000 * 90L;

    protected ExertionDispatcherFactory(LokiMemberUtil loki){
        this.loki = loki;
	}

	public static DispatcherFactory getFactory() {
		return new ExertionDispatcherFactory(null);
	}

	public static DispatcherFactory getFactory(LokiMemberUtil loki) {
		return new ExertionDispatcherFactory(loki);
	}

    public Dispatcher createDispatcher(Exertion exertion,
                                       Set<Context> sharedContexts,
                                       boolean isSpawned,
                                       Provider provider) throws DispatcherException {
        Dispatcher dispatcher = null;
        ProvisionManager provisionManager = null;
        List<Deployment> deployments = ((ServiceExertion)exertion).getDeployments();
        if (deployments.size() > 0)
            provisionManager = new ProvisionManager(exertion);
        try {
            if(exertion instanceof Job)
                exertion = new ExertionSorter(exertion).getSortedJob();

			if (Jobs.isCatalogBlock(exertion) && exertion instanceof Block) {
				logger.info("Running Catalog Block Dispatcher...");
                dispatcher = new CatalogBlockDispatcher(exertion,
						                                  sharedContexts,
						                                  isSpawned,
						                                  provider,
                         provisionManager,
                         providerProvisionManager);
			} else if (isSpaceSequential(exertion)) {
				logger.info("Running Space Sequential Dispatcher...");
				dispatcher = new SpaceSequentialDispatcher(exertion,
						                                  sharedContexts,
						                                  isSpawned,
						                                  loki,
						                                  provider,
                        provisionManager,
                        providerProvisionManager);
			}
            if (dispatcher==null && exertion instanceof Job) {
                Job job = (Job) exertion;
                if (Jobs.isSpaceParallel(job)) {
                    logger.info("Running Space Parallel Dispatcher...");
                    dispatcher = new SpaceParallelDispatcher(job,
                            sharedContexts,
                            isSpawned,
                            loki,
                            provider,
                            provisionManager,
                            providerProvisionManager);
                } else if (Jobs.isCatalogParallel(job)) {
                    logger.info("Running Catalog Parallel Dispatcher...");
                    dispatcher = new CatalogParallelDispatcher(job,
                            sharedContexts,
                            isSpawned,
                            provider,
                            provisionManager,
                            providerProvisionManager);
                } else if (Jobs.isCatalogSequential(job)) {
                    logger.info("Running Catalog Sequential Dispatcher...");
                    dispatcher = new CatalogSequentialDispatcher(job,
                            sharedContexts,
                            isSpawned,
                            provider,
                            provisionManager,
                            providerProvisionManager);
                }
            }
            assert dispatcher != null;
            MonitoringSession monSession = MonitorUtil.getMonitoringSession(exertion);
            if (exertion.isMonitorable() && monSession!=null) {
                logger.fine("Initializing monitor session for : " + exertion.getName());
                if (!(monSession.getState()==Exec.INSPACE)) {
                    monSession.init((Monitorable) provider.getProxy(), LEASE_RENEWAL_PERIOD,
                            DEFAULT_TIMEOUT_PERIOD);
                } else {
                    monSession.init((Monitorable)provider.getProxy());
                }
                LeaseRenewalManager lrm = new LeaseRenewalManager();
                lrm.renewUntil(monSession.getLease(), Lease.FOREVER, LEASE_RENEWAL_PERIOD, null);
                dispatcher.setLrm(lrm);

                logger.fine("Exertion state: " + Exec.State.name(exertion.getStatus()));
                logger.fine("Session for the exertion = " + monSession);
                logger.fine("Lease to be renewed for duration = " +
                        (monSession.getLease().getExpiration() - System
                                .currentTimeMillis()));
            }

            logger.info("*** tally of used dispatchers: " + ExertDispatcher.getDispatchers().size());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new DispatcherException(
                    "Failed to create the exertion dispatcher for job: "+ exertion.getName(), e);
        }
        return dispatcher;
    }

    protected boolean isSpaceSequential(Exertion exertion) {
        if(exertion instanceof Job) {
            Job job = (Job) exertion;
            return Jobs.isSpaceSingleton(job) || Jobs.isSpaceSequential(job);
        }
        return Jobs.isSpaceBlock(exertion);
    }

    /**
     * Returns an instance of the appropriate subclass of Dispatcher as
     * determined from information provided by the given Job instance.
     *
     * @param exertion
     *            The SORCER job that will be used to perform a collection of
     *            components exertions
     */
    @Override
    public Dispatcher createDispatcher(Exertion exertion, Provider provider, String... config) throws DispatcherException {
        return createDispatcher(exertion, new HashSet<Context>(), false, provider);
    }

    @Override
    public SpaceTaskDispatcher createDispatcher(Task task, Provider provider, String... config) throws DispatcherException {
        ProvisionManager provisionManager = null;
        List<Deployment> deployments = task.getDeployments();
        if (deployments.size() > 0)
            provisionManager = new ProvisionManager(task);

        logger.info("Running Space Task Dispatcher...");
        try {
            return new SpaceTaskDispatcher(task,
                    new HashSet<Context>(),
                    false,
                    loki,
                    provisionManager,
                    providerProvisionManager);
        } catch (ContextException e) {
            throw new DispatcherException(
                    "Failed to create the exertion dispatcher for job: "+ task.getName(), e);
        } catch (ExertionException e) {
            throw new DispatcherException(
                    "Failed to create the exertion dispatcher for job: "+ task.getName(), e);
        }
    }
}
