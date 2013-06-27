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

import sorcer.core.Provider;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.exertion.NetJob;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.service.*;
import sorcer.service.space.SpaceAccessor;

import java.rmi.RemoteException;
import java.util.Set;

public class SpaceSequentialDispatcher extends SpaceExertDispatcher {

	@SuppressWarnings("rawtypes")
	public SpaceSequentialDispatcher(Job job, Set<Context> sharedContexts,
			boolean isSpawned, LokiMemberUtil myMemberUtil, Provider provider) throws Throwable {
		super(job, sharedContexts, isSpawned, myMemberUtil, provider);
	}

	public void dispatchExertions() throws ExertionException,
			SignatureException {
		reconcileInputExertions(xrt);
		logger.finer("exertion count: " + inputXrts.size());
		for (int i = 0; i < inputXrts.size(); i++) {
			ServiceExertion exertion = (ServiceExertion) inputXrts
					.elementAt(i);
			logger.finer("exertion #: " + i + ", exertion:\n" + exertion);
			try {
				writeEnvelop(exertion);
				logger.finer("generateTasks ==> SPACE SEQUENIAL EXECUTE EXERTION: "
								+ exertion.getName());
			} catch (RemoteException re) {
				re.printStackTrace();
				logger.severe("Space not reachable....resetting space");
				space = SpaceAccessor.getSpace();
				if (space == null) {
					xrt.setStatus(FAILED);
					throw new ExertionException("NO exertion space available!");
				}
				if (masterXrt != null) {
					try {
						writeEnvelop(masterXrt);
					} catch (Exception e) {
						e.printStackTrace();
						xrt.setStatus(FAILED);
						throw new ExertionException(
								"Wrting master exertion into exertion space failed!",
								e);
					}
				}
			}
			logger.finer("waiting for exertion " + i + ", id="
					+ exertion.getId() + ".............");
			waitForExertion(exertion.getIndex());
		}
		dThread.stop = true;
	}

	public void collectResults() throws ExertionException, SignatureException {
		int count = 0;
		ExertionEnvelop temp;
		temp = ExertionEnvelop.getTemplate();
		temp.parentID = xrt.getId();
		temp.state = new Integer(DONE);

		logger.finer("<===================== collect exertions for template: \n"
						+ temp.describe());
		while (count < inputXrts.size() && state != FAILED) {
			ExertionEnvelop resultEnvelop = (ExertionEnvelop) takeEnvelop(temp);
			logger.finer("collected result envelope  <===================== \n"
					+ resultEnvelop.describe());

			if (resultEnvelop != null && resultEnvelop.exertion != null) {
				ServiceExertion input = (ServiceExertion) ((NetJob)xrt)
						.exertionAt(((ServiceExertion) resultEnvelop.exertion)
								.getIndex());
				ServiceExertion result = (ServiceExertion) resultEnvelop.exertion;
				postExecExertion(input, result);
				count++;
			}
		}
		executeMasterExertion();
		dispatchers.remove(xrt.getId());
		state = DONE;
	}
}
