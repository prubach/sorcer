/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013 SorcerSoft.com S.A.
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
import java.util.Set;
import java.util.logging.Logger;

import net.jini.config.ConfigurationException;
import sorcer.core.Cataloger;
import sorcer.core.Dispatcher;
import sorcer.core.Provider;
import sorcer.core.exertion.Jobs;
import sorcer.core.exertion.NetTask;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Job;

/**
 * This class creates instances of appropriate subclasses of Dispatcher. The
 * appropriate subclass is determined by calling the ServiceJob object's
 */
public class ExertionDispatcherFactory implements DispatcherFactory {

    private static ExertionDispatcherFactory factory;
    public static Cataloger catalog; // The service catalog object
    private final static Logger logger = Logger.getLogger(ExertionDispatcherFactory.class.getName());

    public static ExertionDispatcherFactory getFactory() {
        if (factory == null)
            factory = new ExertionDispatcherFactory();
        return factory;
    }

    public static ExertionDispatcherFactory getProvisionableFactory() {
        if (factory == null)
            factory = new ExertionDispatcherFactory();
        return factory;
    }

    /**
     * Returns an instance of the appropriate subclass of Dispatcher as
     * determined from information provided by the given Job instance.
     *
     * @param job
     *            The SORCER job that will be used to perform a collection of
     *            components exertions
     */
    public Dispatcher createDispatcher(Job job, Provider provider) throws DispatcherException {
        return createDispatcher(job, new HashSet<Context>(), false, null, provider);
    }

    public Dispatcher createDispatcher(Job job,
                                       Set<Context> sharedContexts,
                                       boolean isSpawned,
                                       Provider provider) throws DispatcherException {
        return createDispatcher(job, sharedContexts, isSpawned, null, provider);
    }

    public Dispatcher createDispatcher(Exertion exertion,
                                       Set<Context> sharedContexts,
                                       boolean isSpawned,
                                       LokiMemberUtil myMemberUtil,
                                       Provider provider,
                                       String... config) throws DispatcherException {
        Dispatcher dispatcher = null;
        ProvisionManager provisionManager = null;
//		logger.severe("Create ProvisionManager with configuration arg: "+
//				(config==null?"<NULL>":(config.length==0?"<NO CONFIGURATION>":config[0])));

//		System.out.println("ZZZZZZZZZZZZZZZZZZZZZZ config: " + Arrays.toString(config));
        if (config!=null && config.length>0) {
            try {
                provisionManager = new ProvisionManager(exertion, config);
            } catch (ConfigurationException e) {
                throw new DispatcherException("Unable to create the ProvisionManager", e);
            }
        }

        try {
            if (!exertion.isJob()) {
                logger.info("Running Space Task Dispatcher...");
                return dispatcher = new SpaceTaskDispatcher((NetTask)exertion,
                        sharedContexts,
                        isSpawned,
                        myMemberUtil,
                        provisionManager);
            }

            Job job = (Job)exertion;
            if (Jobs.isSpaceSingleton(job)) {
                logger.info("Running Space Sequential Dispatcher...");
                dispatcher = new SpaceSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        myMemberUtil,
                        provider,
                        provisionManager);
            } else if (Jobs.isSpaceParallel(job)) {
                logger.info("Running Space Parallel Dispatcher...");
                dispatcher = new SpaceParallelDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        myMemberUtil,
                        provider,
                        provisionManager);
            } else if (Jobs.isSpaceSequential(job)) {
                logger.info("Running Space Sequential Dispatcher ...");
                dispatcher = new SpaceSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        myMemberUtil,
                        provider,
                        provisionManager);
            } else if (Jobs.isCatalogSingleton(job)) {
                logger.info("Running Catalog Singleton Dispatcher...");
                dispatcher = new CatalogSingletonDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
            } else if (Jobs.isCatalogParallel(job)) {
                logger.info("Running Catalog Parallel Dispatcher...");
                dispatcher = new CatalogParallelDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
            } else if (Jobs.isCatalogSequential(job)) {
                logger.info("Running Catalog Sequential Dispatcher...");
                dispatcher = new CatalogSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
            } else if (Jobs.isSWIFSequential(job)) {
                logger.info("Running SWIF Sequential Dispatcher...");
                dispatcher = new SWIFSequentialDispatcher(job,
                        sharedContexts,
                        isSpawned,
                        provider,
                        provisionManager);
            }
            logger.info("*** tally of used dispatchers: " + ExertDispatcher.getDispatchers().size());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DispatcherException(
                    "Failed to create the exertion dispatcher for job: "+ exertion.getName(), e);
        }
        return dispatcher;
    }


    @Override
    public Dispatcher createDispatcher(Job job, Provider provider, String... config) throws DispatcherException {
        return createDispatcher(job, new HashSet<Context>(), false, null, provider, config);
    }
}
