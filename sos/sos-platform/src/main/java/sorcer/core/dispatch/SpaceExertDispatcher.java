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

import java.rmi.RemoteException;
import java.util.*;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.id.Uuid;
import net.jini.space.JavaSpace05;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.exertion.Jobs;
import sorcer.core.exertion.NetJob;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.core.provider.SpaceTaker;
import sorcer.ext.ProvisioningException;
import sorcer.service.*;
import sorcer.service.space.SpaceAccessor;

import static sorcer.service.Exec.*;
import static sorcer.util.StringUtils.tName;

@SuppressWarnings("rawtypes")
abstract public class SpaceExertDispatcher extends ExertDispatcher {
	protected JavaSpace05 space;
	protected int doneExertionIndex = 0;
	protected LokiMemberUtil myMemberUtil;

	public SpaceExertDispatcher() {
		//do nothing
	}
	
	public SpaceExertDispatcher(Exertion exertion, 
            Set<Context> sharedContext,
            boolean isSpawned,
            LokiMemberUtil memUtil,
            Provider provider,
            ProvisionManager provisionManager,
            ProviderProvisionManager providerProvisionManager) throws ExertionException, ContextException {
		super(exertion, sharedContext, isSpawned, provider, provisionManager, providerProvisionManager);
	
		space = SpaceAccessor.getSpace();
		if (space == null) {
			throw new ExertionException("NO exertion space available!");
		}

		if (exertion instanceof Job)
			inputXrts = Jobs.getInputExertions((Job)exertion);
		else if (exertion instanceof Block)
			inputXrts = exertion.getAllExertions();

		disatchGroup = new ThreadGroup("exertion-"+ exertion.getId());
		disatchGroup.setDaemon(true);
		disatchGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);

		dThread = new DispatchThread(disatchGroup);
		dThread.start();

		CollectResultThread crThread = new CollectResultThread(disatchGroup);
		crThread.start();

		CollectFailThread cfThread = new CollectFailThread(disatchGroup);
		cfThread.start();

		CollectErrorThread efThread = new CollectErrorThread(disatchGroup);
		efThread.start();

		myMemberUtil = memUtil;
	}

	protected void addPoison(Exertion exertion) {
		space = SpaceAccessor.getSpace();
		if (space == null) {
			return;
		}
		ExertionEnvelop ee = new ExertionEnvelop();
		if (exertion == xrt)
			ee.parentID = exertion.getId();
		else
			ee.parentID = exertion.getParentId();
		ee.state = Exec.POISONED;
		try {
			space.write(ee, null, Lease.FOREVER);
			logger.debug("written poisoned envelop for: "
					+ ee.describe() + "\n to: " + space);
		} catch (Exception e) {
			logger.warn("writting poisoned ExertionEnvelop", e);
		}
	}

	synchronized void waitForExertion(int index) {
		while (index - doneExertionIndex > -1) {
			try {
				wait();
			} catch (InterruptedException e) {
				// continue
			}
		}
	}

	protected synchronized void changeDoneExertionIndex(int index) {
		doneExertionIndex = index + 1;
		notifyAll();
	}

	// abstract in ExertionDispatcher
	protected void preExecExertion(Exertion exertion) throws ExertionException,
			SignatureException {
//		try {
//			exertion.getControlContext().appendTrace(provider.getProviderName() 
//					+ " dispatcher: " + getClass().getName());
//		} catch (RemoteException e) {
//			// ignore it, local call
//		}
		try {
			updateInputs(exertion);
		} catch (ContextException e) {
			throw new ExertionException(e);
		}
		((ServiceExertion) exertion).startExecTime();
		((ServiceExertion) exertion).setStatus(RUNNING);
	}

    private void provisionProviderForExertion(Exertion exertion) throws ProvisioningException {
        providerProvisionManager.add(exertion, this);
    }


	protected void writeEnvelop(Exertion exertion) throws 
			ExertionException, SignatureException, RemoteException, ProvisioningException {
		// setSubject before exertion is dropped
		space = SpaceAccessor.getSpace();
		if (space == null) {
			throw new ExertionException("NO exertion space available!");
		}

        if (exertion.isProvisionable())
            provisionProviderForExertion(exertion);

        ((ServiceExertion) exertion).setSubject(subject);
		preExecExertion(exertion);
		ExertionEnvelop ee = ExertionEnvelop.getTemplate(exertion);
		ee.state = INITIAL;
		try {
			space.write(ee, null, Lease.FOREVER);
			logger.debug("written envelop: "
					+ ee.describe() + "\n to: " + space);
		} catch (Exception e) {
			logger.warn("writeEnvelop", e);
            state = Exec.FAILED;
		}
	}

	protected ExertionEnvelop takeEnvelop(Entry template)
			throws ExertionException {
		space = SpaceAccessor.getSpace();
		if (space == null) {
			throw new ExertionException("NO exertion space available!");
		}
		ExertionEnvelop result;
		try {
			while (state == RUNNING) {
				result = (ExertionEnvelop) space.take(template, null, SpaceTaker.SPACE_TIMEOUT);
				if (result != null) {
					return result;
				}
			}
            return null;
        } catch (UnusableEntryException e){
            logger.warn("UnusableEntryException! unusable fields = " + e.partialEntry, e);
            throw new ExertionException("Taking exertion envelop failed", e);
		} catch (Throwable e) {
			throw new ExertionException("Taking exertion envelop failed", e);
		}
	}

	protected void postExecExertion(Exertion ex, Exertion result)
			throws  ExertionException, SignatureException {
		((ServiceExertion) result).stopExecTime();
		try {
			result.getControlContext().appendTrace(provider.getProviderName() 
					+ " dispatcher: " + getClass().getName());

			((NetJob)xrt).setExertionAt(result, ex.getIndex());
			ServiceExertion ser = (ServiceExertion) result;
			if (ser.getStatus() > FAILED && ser.getStatus() != SUSPENDED) {
				ser.setStatus(DONE);
				collectOutputs(result);
			}
		} catch (Exception e) {
			throw new ExertionException(e);
		}
		changeDoneExertionIndex(result.getIndex());
	}

	public void collectFails() throws ExertionException {
		ExertionEnvelop template = ExertionEnvelop.getParentTemplate(xrt.getId(),
				null);
		template.state = FAILED;
		while (state != DONE && state != FAILED) {
			ExertionEnvelop ee;
			try {
				//System.out.println("fail template: " + template.describe());
				ee = takeEnvelop(template);
			} catch (ExertionException e) {
				xrt.getControlContext().addException(e);
				handleError(xrt, FAILED);
				break;
			}
			if (ee != null) {
				Exertion result = ee.exertion;
				logger.debug("collected space FAILED exertion {}", result);
				if (result != null) {
					handleError(result, FAILED);
				}
			}
		}
	}

	public void collectErrors() throws ExertionException {
		ExertionEnvelop template = ExertionEnvelop.getParentTemplate(xrt.getId(),
				null);
		template.state = ERROR;
		while (state != DONE && state != FAILED) {
			ExertionEnvelop ee;
			try {
				//System.out.println("error template: " + template.describe());
				ee = takeEnvelop(template);
			} catch (ExertionException e) {
				xrt.getControlContext().addException(e);
				handleError(xrt, FAILED);
				break;
			}
			if (ee != null) {
				Exertion result = ee.exertion;
				logger.debug("collectd space ERROR exertion {}", result);
				if (result != null) {
					handleError(result, ERROR);
				}
			}
		}
	}

	protected void handleError(Exertion exertion, int state) {
		if (exertion != xrt)
			((NetJob)xrt).setExertionAt(exertion, 
					exertion.getIndex());
		addPoison(exertion);
		this.state = state;
		dThread.stop = true;
		cleanRemainingFailedExertions(xrt.getId());
	}

	private void cleanRemainingFailedExertions(Uuid id) {
        logger.debug("clean remaining failed exertions for {}", id);
		ExertionEnvelop template = ExertionEnvelop.getParentTemplate(id, null);
		ExertionEnvelop ee = null;

        do {
			try {
                logger.debug("take envelop {}", template);
				ee = takeEnvelop(template);
			} catch (ExertionException e) {
                logger.warn("Error while taking {}", template);
			}
        } while (ee != null);
	}
	
	protected void executeMasterExertion() throws 
			ExertionException, SignatureException {
		if (masterXrt == null)
			return;
		logger
				.info("executeMasterExertion ==============> SPACE EXECUTE MASTER EXERTION");
		try {
			writeEnvelop(masterXrt);
		} catch (ProvisioningException pe) {
            masterXrt.setStatus(FAILED);
            throw new ExertionException(pe.getLocalizedMessage());
        } catch (RemoteException re) {
			re.printStackTrace();
			logger.warn("Space died....resetting space");
			space = SpaceAccessor.getSpace();
			if (space == null) {
				throw new ExertionException("NO exertion space available!");
			}
			try {
				writeEnvelop(masterXrt);
			} catch (Exception e) {
				logger.warn("Space died....could not recover", e);
			}
		}

		ExertionEnvelop template = ExertionEnvelop.getTakeTemplate(
				masterXrt.getParentId(), masterXrt.getId());

		ExertionEnvelop result = takeEnvelop(template);
		logger.debug("executeMasterExertion MASTER EXERTION RESULT RECIEVED");
		if (result != null && result.exertion != null) {
			postExecExertion(masterXrt, result.exertion);
		}
	}

	protected class CollectFailThread extends Thread {

		public CollectFailThread(ThreadGroup disatchGroup) {
			super(disatchGroup, tName("Fail collector"));
		}

		public void run() {
			try {
				collectFails();
			} catch (Exception ex) {
				xrt.setStatus(FAILED);
				xrt.reportException(ex);
				ex.printStackTrace();
			}
			dispatchers.remove(xrt.getId());
		}
	}

	protected class CollectErrorThread extends Thread {

		public CollectErrorThread(ThreadGroup disatchGroup) {
			super(disatchGroup, tName("Error collector"));
		}

		public void run() {
			try {
				collectErrors();
			} catch (Exception ex) {
				xrt.setStatus(ERROR);
				xrt.reportException(ex);
				ex.printStackTrace();
			}
			dispatchers.remove(xrt.getId());
		}
	}

}
