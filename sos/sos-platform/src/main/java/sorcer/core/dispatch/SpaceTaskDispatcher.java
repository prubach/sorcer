/**
 *
 * Copyright 2013 the original author or authors.
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

import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.exertion.NetTask;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.service.Context;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.space.SpaceAccessor;

import java.rmi.RemoteException;
import java.util.Set;

public class SpaceTaskDispatcher extends SpaceExertDispatcher {
	
	@SuppressWarnings("rawtypes")
	public SpaceTaskDispatcher(NetTask task, Set<Context> sharedContexts,
			boolean isSpawned, LokiMemberUtil myMemberUtil) throws Throwable {
		if (space == null)
			space = SpaceAccessor.getSpace();
		// logger.info(this, "using space=" + Env.getSpaceName());
		xrt = task;
		subject = task.getSubject();
		this.sharedContexts = sharedContexts;
		this.isSpawned = isSpawned;
		isMonitored = task.isMonitorable();
		state = RUNNING;
		dispatchers.put(task.getId(), this);
		dispatchExertions();
		
		disatchGroup = new ThreadGroup("task-"+ task.getId());
		disatchGroup.setDaemon(true);
		disatchGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);

		CollectResultThread crThread = new CollectResultThread(disatchGroup);
		crThread.start();

		CollectFailThread cfThread = new CollectFailThread(disatchGroup);
		cfThread.start();

		CollectErrorThread efThread = new CollectErrorThread(disatchGroup);
		efThread.start();

		this.myMemberUtil = myMemberUtil;
	}

	public void dispatchExertions() throws ExertionException,
			SignatureException {
		reconcileInputExertions(xrt);
		logger.finer("space task: " + xrt);
		try {
			writeEnvelop(xrt);
			logger.finer("written task ==> SPACE EXECUTE TASK: "
					+ xrt.getName());
		} catch (RemoteException re) {
			re.printStackTrace();
			logger.severe("Space not reachable... resetting space");
			space = SpaceAccessor.getSpace();
			if (space == null) {
				xrt.setStatus(FAILED);
				throw new ExertionException("NO exertion space available!");
			}
		}
	}

	public void collectResults() throws ExertionException, SignatureException {
		ExertionEnvelop temp;
		temp = ExertionEnvelop.getTemplate();
		temp.exertionID = xrt.getId();
		temp.state = new Integer(DONE);

		logger.finer("<===================== template for space task to be collected: \n"
				+ temp.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(temp);
		logger.finer("collected result envelope  <===================== \n"
				+ resultEnvelop.describe());

		NetTask result = (NetTask) resultEnvelop.exertion;
		state = DONE;
		notifyExertionExecution(xrt, xrt, result);
		result.setStatus(DONE);
		dispatchers.remove(xrt.getId());
		xrt = result;
	}
	
	public void collectFails() throws ExertionException {
		ExertionEnvelop template;
		template = ExertionEnvelop.getTemplate();
		template.exertionID = xrt.getId();
		template.state = new Integer(FAILED);

		logger.finer("<===================== template for failed task to be collected: \n"
				+ template.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(template);
		if (resultEnvelop != null) {
			NetTask result = (NetTask) resultEnvelop.exertion;
			state = FAILED;
			notifyExertionExecution(xrt, xrt, result);
			result.setStatus(FAILED);
			xrt = result;
		}
		dispatchers.remove(xrt.getId());
	}
	
	public void collectErrors() throws ExertionException {
		ExertionEnvelop template;
		template = ExertionEnvelop.getTemplate();
		template.exertionID = xrt.getId();
		template.state = new Integer(ERROR);

		logger.finer("<===================== template for error task to be collected: \n"
				+ template.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(template);
		if (resultEnvelop != null) {
			NetTask result = (NetTask) resultEnvelop.exertion;
			state = ERROR;
			notifyExertionExecution(xrt, xrt, result);
			result.setStatus(ERROR);
			xrt = result;
		}
		dispatchers.remove(xrt.getId());
	}

}
