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

package sorcer.core.provider.outrigger;

import sorcer.config.AbstractBeanListener;
import sorcer.core.service.Configurer;
import sorcer.core.service.IServiceBuilder;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerProcessor extends AbstractBeanListener {
    @Inject
    private Configurer configurer;

    @Inject
    private ExecutorService executor;


    {
        if (spaceEnabled && spaceHandlingPools != null) {
                  for (SpaceTaker st : spaceTakers) {
                      st.destroy();
                  }
      			for (ExecutorService es : spaceHandlingPools)
      				shutdownAndAwaitTermination(es);
      			if (interfaceGroup != null) {
      				Thread[] ifgThreads = new Thread[interfaceGroup.activeCount()];
      				Thread[] ngThreads = new Thread[namedGroup.activeCount()];
      				interfaceGroup.enumerate(ifgThreads);
      				namedGroup.enumerate(ngThreads);
                      // Wait until spaceTakers shutdown
                      int attempts = 0;
                      Set<Thread> spaceTakerThreads = new HashSet<Thread>();
                      while (attempts < 11) {
                          try {
                              Thread.sleep(SpaceTaker.SPACE_TIMEOUT/10);
                          } catch (InterruptedException ie) {
                          }
                          attempts++;
                          for (int i = 0; i < ifgThreads.length; i++) {
                              if (ifgThreads[i].isAlive())
                                  spaceTakerThreads.add(ifgThreads[i]);
                              else
                                  spaceTakerThreads.remove(ifgThreads[i]);
                          }
                          for (int i = 0; i < ngThreads.length; i++) {
                              if (ngThreads[i].isAlive())
                                  spaceTakerThreads.add(ngThreads[i]);
                              else
                                  spaceTakerThreads.remove(ngThreads[i]);
                          }
                          if (spaceTakerThreads.isEmpty())
                              break;
                      }
                      for (Thread thread : spaceTakerThreads) {
                          if (thread.isAlive())
                              thread.interrupt();
                      }
                  }
      		}
    }


    private void initThreadGroups() {
		namedGroup = new ThreadGroup("Provider Group: " + getProviderName());
		namedGroup.setDaemon(true);
		namedGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);
		interfaceGroup = new ThreadGroup("Interface Threads");
		interfaceGroup.setDaemon(true);
		interfaceGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);
	}

	public void startSpaceTakers() throws ConfigurationException, RemoteException {
		ExecutorService spaceWorkerPool;
		spaceHandlingPools = new ArrayList<ExecutorService>();
		String msg;
		if (space == null) {
			msg = "ERROR: No space found, spaceName = " + spaceName
					+ ", spaceGroup = " + spaceGroup;
			logger.warn(msg);
		}
		if (workerTransactional && tManager == null) {
			msg = "ERROR: no transactional manager found....";
			logger.warn(msg);
		}
		if (publishedServiceTypes == null || publishedServiceTypes.length == 0) {
			msg = "ERROR: no published interfaces found....";
			logger.warn(msg);
		}

		initThreadGroups();
		ExertionEnvelop envelop;
		LokiMemberUtil memberInfo = null;
		if (spaceSecurityEnabled) {
			memberInfo = new LokiMemberUtil(ProviderDelegate.class.getName());
		}

		logger.debug("*** provider worker count: " + workerCount
                + ", spaceTransactional: " + workerTransactional);
		logger.info("publishedServiceTypes.length = "
				+ publishedServiceTypes.length);
		logger.info(Arrays.toString(publishedServiceTypes));

		// create a pair of taker threads for each published interface
		SpaceTaker worker = null;

		// make sure that the number of core threads equals the maximum number
		// of threads
		if (queueSize == 0) {
			if (maximumPoolSize > workerCount)
				workerCount = maximumPoolSize;
		}
		for (int i = 0; i < publishedServiceTypes.length; i++) {
			// spaceWorkerPool = Executors.newFixedThreadPool(workerCount);
            ConfigurableThreadFactory factory = new ConfigurableThreadFactory();
            factory.setNameFormat("SpaceTaker-" + spaceName + "-thread-%2$d");
            spaceWorkerPool = new ThreadPoolExecutor(workerCount,
					maximumPoolSize > workerCount ? maximumPoolSize
							: workerCount, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>(
							(queueSize == 0 ? workerCount : queueSize)), factory);
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
								(queueSize == 0 ? workerCount : queueSize)));
				spaceHandlingPools.add(spaceWorkerPool);
				envelop = ExertionEnvelop.getTemplate(publishedServiceTypes[i],
						getProviderName());
				if (spaceReadiness) {
					worker = new SpaceIsReadyTaker(
							new SpaceTaker.SpaceTakerData(envelop, memberInfo,
									provider, spaceName, spaceGroup,
									workerTransactional, queueSize == 0),
							spaceWorkerPool);
                    spaceTakers.add(worker);
				} else {
					worker = new SpaceTaker(new SpaceTaker.SpaceTakerData(
							envelop, memberInfo, provider, spaceName,
							spaceGroup, workerTransactional, queueSize == 0),
							spaceWorkerPool);
                    spaceTakers.add(worker);
				}
				Thread snth = new Thread(namedGroup, worker);
				snth.setDaemon(true);
				snth.start();
				logger.info("*** named space worker-" + i + " started for: "
						+ publishedServiceTypes[i] + ":" + getProviderName());
				// System.out.println("space template: " +
				// envelop.describe());
			}
		}
		// interfaceGroup.list();
		// namedGroup.list();
	}

    void initSpaceSupport() throws ConfigurationException, RemoteException {
   		if (!spaceEnabled)
   			return;

   		space = SpaceAccessor.getSpace(spaceName, spaceGroup);
   		if (space == null) {
   			int ctr = 0;
   			while (space == null && ctr++ < TRY_NUMBER) {
   				logger.warn("could not get space, trying again... try number = {}", ctr);
   				try {
   					Thread.sleep(500);
   				} catch (InterruptedException e) {
   					e.printStackTrace();
   				}
   				space = SpaceAccessor.getSpace(spaceName, spaceGroup);
   			}
   			if (space != null) {
   				logger.info("got space = " + space);
   			} else {
   				logger.warn("***warn: could not get space...moving on.");
   			}
   		}
   		if (workerTransactional)
   			tManager = TransactionManagerAccessor.getTransactionManager();

   		try {
   			startSpaceTakers();
   		} catch (Exception e) {
   			logger.error("Provider HALTED: Couldn't start Workers", e);
   			provider.destroy();
   		}
   	}





public List<ExecutorService> getSpaceHandlingPools() {
	return spaceHandlingPools;
}

void shutdownAndAwaitTermination(ExecutorService pool) {
	pool.shutdown(); // Disable new tasks from being submitted
	try {
		// Wait a while for existing tasks to terminate
		if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
			pool.shutdownNow(); // Cancel currently executing tasks
			// Wait a while for tasks to respond to being cancelled
			if (!pool.awaitTermination(3, TimeUnit.SECONDS))
				System.err.println("Pool did not terminate");
		}
	} catch (InterruptedException ie) {
		// (Re-)Cancel if current thread also interrupted
		pool.shutdownNow();
		// Preserve interrupt status
		Thread.currentThread().interrupt();
	}
