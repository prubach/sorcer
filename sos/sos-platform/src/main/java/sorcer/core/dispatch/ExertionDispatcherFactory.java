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

import sorcer.core.deploy.Deployment;
import sorcer.core.provider.Cataloger;
import sorcer.core.Dispatcher;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.Jobs;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.service.*;

/**
 * This class creates instances of appropriate subclasses of Dispatcher. The
 * appropriate subclass is determined by calling the ServiceJob object's
 */
public class ExertionDispatcherFactory implements DispatcherFactory {
    public static Cataloger catalog; // The service catalog object
    private final static Logger logger = Logger.getLogger(ExertionDispatcherFactory.class.getName());
    private ProviderProvisionManager providerProvisionManager = ProviderProvisionManager.getInstance();

    private LokiMemberUtil loki;

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
			if (exertion instanceof Task) {
				logger.info("Running Space Task Dispatcher...");
				return new SpaceTaskDispatcher((Task)exertion,
						                                    sharedContexts, 
						                                    isSpawned, 
						                                    loki,
                        provisionManager,
                        providerProvisionManager);
			} else if (Jobs.isCatalogBlock(exertion) && exertion instanceof Block) {
				logger.info("Running Catalog Block Dispatcher...");
				 return new CatalogBlockDispatcher((Block)exertion,
						                                  sharedContexts, 
						                                  isSpawned, 
						                                  provider,
                         provisionManager,
                         providerProvisionManager);
			} else if (Jobs.isSpaceBlock(exertion) && exertion instanceof Block) {
				logger.info("Running Catalog Block Dispatcher...");
				return new SpaceBlockDispatcher((Block)exertion,
						                                  sharedContexts, 
						                                  isSpawned, 
						                                  loki,
						                                  provider,
                        provisionManager,
                        providerProvisionManager);
			}
            Job job = (Job)exertion;
            ExertionSorter es = new ExertionSorter(job);
            job = (Job)es.getSortedJob();
            if (Jobs.isSpaceSingleton(job)) {
                logger.info("Running Space Sequential Dispatcher...");
                dispatcher = new SpaceSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        loki,
                        provider,
                        provisionManager,
                        providerProvisionManager);
            } else if (Jobs.isSpaceParallel(job)) {
                logger.info("Running Space Parallel Dispatcher...");
                dispatcher = new SpaceParallelDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        loki,
                        provider,
                        provisionManager,
                        providerProvisionManager);
            } else if (Jobs.isSpaceSequential(job)) {
                logger.info("Running Space Sequential Dispatcher ...");
                dispatcher = new SpaceSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        loki,
                        provider,
                        provisionManager,
                        providerProvisionManager);
            } else if (Jobs.isCatalogSingleton(job)) {
                logger.info("Running Catalog Singleton Dispatcher...");
                dispatcher = new CatalogSingletonDispatcher(job,
                        sharedContexts,
                        isSpawned,
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
            logger.info("*** tally of used dispatchers: " + ExertDispatcher.getDispatchers().size());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DispatcherException(
                    "Failed to create the exertion dispatcher for job: "+ exertion.getName(), e);
        }
        return dispatcher;
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
}
