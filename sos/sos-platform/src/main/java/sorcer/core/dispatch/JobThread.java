/**
 *
 * Copyright 2013 the original author or authors.
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

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import sorcer.core.DispatchResult;
import sorcer.core.Dispatcher;
import sorcer.core.provider.Provider;
import sorcer.service.ContextException;
import sorcer.service.Job;

public class JobThread implements Runnable {
	private final static Logger logger = Logger.getLogger(JobThread.class
			.getName());

	private static final int SLEEP_TIME = 250;
	// doJob method calls this internally
	private Job job;

	private Job result;

	Provider provider;

    private DispatcherFactory dispatcherFactory;

	public JobThread(Job job, Provider provider, DispatcherFactory dispatcherFactory) {
		this.job = job;
		this.provider = provider;
        this.dispatcherFactory = dispatcherFactory;
	}

	public void run() {
		logger.finer("*** Exertion dispatcher started with control context ***\n"
				+ job.getControlContext());
		try {
            Dispatcher dispatcher = dispatcherFactory.createDispatcher(job, provider);
			try {
				job.getControlContext().appendTrace(provider.getProviderName() +
						" dispatcher: " + dispatcher.getClass().getName());
			} catch (RemoteException e) {
                logger.severe("exception in dispatcher: " + e);
				// ignore it, locall call
			}
 			dispatcher.exec();
            DispatchResult dispatchResult = dispatcher.getResult();
			logger.finer("*** Dispatcher exit state = " + dispatcher.getClass().getName()  + " state: " + dispatchResult.state
					+ " for job***\n" + job.getControlContext());
            result = (Job) dispatchResult.exertion;
		} catch (DispatcherException de) {
			logger.log(Level.SEVERE, "Error", de);
		}
	}

	public Job getJob() {
		return job;
	}

	public Job getResult() throws ContextException {
		return result;
	}
}
