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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import sorcer.core.DispatchResult;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.Jobs;
import sorcer.service.*;

import static sorcer.service.Exec.*;

public class CatalogParallelDispatcher extends CatalogExertDispatcher {
	List<ExertionThread> workers;
    protected ExecutorService executor;
    private List<Future<Exertion>>results;

	public CatalogParallelDispatcher(Job job, 
            Set<Context> sharedContexts,
            boolean isSpawned, 
            Provider provider,
            ProvisionManager provisionManager,
            ProviderProvisionManager providerProvisionManager) {
		super(job, sharedContexts, isSpawned, provider, provisionManager, providerProvisionManager);
	}

    @Override
    public DispatchResult getResult() {

        return new ;
    }

    public void dispatchExertions() throws ExertionException,
			SignatureException {
		workers = new ArrayList<ExertionThread>();
		try {
			inputXrts = Jobs.getInputExertions(((Job)xrt));
			reconcileInputExertions(xrt);
		} catch (ContextException e) {
			throw new ExertionException(e);
		}
		for (Exertion exertion : inputXrts) {
			workers.add(runExertion((ServiceExertion)exertion));
		}
		collectResults();
		dThread.stop = true;
	}

	public void collectResults() throws ExertionException, SignatureException {
		boolean isFailed = false;
		boolean isSuspended = false;
		Exertion result;
		while (workers.size() > 0) {
            List<ExertionThread> workersToRemove = new ArrayList<ExertionThread>();
			for (ExertionThread exThread : workers) {
                    result = exThread.getResult();
                    if (result != null) {
                        ServiceExertion se = (ServiceExertion) result;
                        se.stopExecTime();
                        if (se.getStatus() == FAILED)
                            isFailed = true;
                        else if (se.getStatus() == SUSPENDED)
                            isSuspended = true;
                        workersToRemove.add(exThread);
                }
			}
            workers.removeAll(workersToRemove);
		}

		if (isFailed) {
			xrt.setStatus(FAILED);
			state = FAILED;
			ExertionException fe = new ExertionException(this.getClass().getName() 
					+ " failed job", xrt);
			xrt.reportException(fe);
			dispatchers.remove(xrt.getId());
			throw fe;
		}
		else if (isSuspended) {
			xrt.setStatus(SUSPENDED);
			state = SUSPENDED;
			ExertionException fe = new ExertionException(this.getClass().getName() 
					+ " suspended job", xrt);
			xrt.reportException(fe);
			dispatchers.remove(xrt.getId());
			throw fe;
		}
		
		if (masterXrt != null) {
			if (isInterupted(masterXrt)) {
				masterXrt.stopExecTime();
				dispatchers.remove(xrt.getId());
				return;
			}
			// finally execute Master Exertion
			masterXrt = (ServiceExertion) execExertion(masterXrt);
			masterXrt.stopExecTime();
			if (masterXrt.getStatus() <= FAILED)
				xrt.setStatus(FAILED);
			else
				xrt.setStatus(DONE);

		}
		xrt.setStatus(DONE);
		dispatchers.remove(xrt.getId());
		state = DONE;
	}
}
