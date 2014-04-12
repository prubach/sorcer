/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013 Sorcersoft.com S.A.
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

import java.util.Set;

import sorcer.core.monitor.MonitoringSession;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.provider.exertmonitor.MonitorHelper;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Task;

public class MonitoredTaskDispatcher extends MonitoredExertDispatcher {

	/**
	 * @param exertion
	 * @param sharedContext
	 * @param isSpawned
	 * @param provider
	 * @throws Throwable
	 */
	public MonitoredTaskDispatcher(Exertion exertion,
			Set<Context> sharedContext, boolean isSpawned, Provider provider,
            ProvisionManager provisionManager, ProviderProvisionManager providerProvisionManager)
			throws Throwable {
		super(exertion, sharedContext, isSpawned, provider, provisionManager, providerProvisionManager);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.core.dispatch.ExertDispatcher#dispatchExertions()
	 */
	@Override
	public void dispatchExertions() throws ExertionException {
        checkAndDispatchExertions();
		preExecExertion(xrt);
		collectResults();
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dispatch.ExertDispatcher#collectResults()
	 */
	@Override
	public void collectResults() throws ExertionException {
		NetTask result = null;
		try {
//			logger.finer("\n*** getting result... ***\n");
			result = (NetTask) ((ServiceProvider) provider).getDelegate()
					.doTask((Task) xrt, null);
			result.getControlContext().appendTrace(provider.getProviderName() 
					+ " dispatcher: " + getClass().getName());

            MonitoringSession session = MonitorHelper.getMonitoringSession(xrt.getContext());
            if (result.getStatus() <= FAILED) {
				xrt.setStatus(FAILED);
				state = FAILED;
				session.changed(result.getDataContext(),
						State.FAILED);
				ExertionException fe = new ExertionException(this.getClass()
						.getName() + " received failed task", result);
				result.reportException(fe);
				throw fe;
			} else {
				notifyExertionExecution(xrt, result);
				state = DONE;
				xrt.setStatus(DONE);
				session.changed(result.getDataContext(),
						State.DONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExertionException ne = new ExertionException(e);
			result.reportException(ne);
			throw ne;
		}
		postExecExertion(xrt);
	}

}
