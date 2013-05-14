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
import sorcer.util.ProviderAccessor;

import java.util.Set;

public class SpaceParallelDispatcher extends SpaceExertDispatcher {

	public SpaceParallelDispatcher(Job job, Set<Context> sharedContexts,
			boolean isSpawned, LokiMemberUtil myMemberUtil, Provider provider) throws Throwable {
		super(job, sharedContexts, isSpawned, myMemberUtil, provider);
	}

	public void dispatchExertions() throws ExertionException,
			SignatureException {
		reconcileInputExertions(xrt);

		int index = inputXrts.size() - 1;
		ServiceExertion exertion = null;
		try {
			while (index >= 0) {
				exertion = (ServiceExertion) inputXrts.elementAt(index);
				logger
						.info("generateTasks ==> SPACE PARALLEL EXECUTE EXERTION: "
								+ exertion.getName());
				writeEnvelop(exertion);
				index--;
			}
			dThread.stop = true;
		} catch (Exception re) {
			re.printStackTrace();
			logger.severe("Space not reachable....resetting space");
			space = ProviderAccessor.getSpace();
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
	}

	public void collectResults() throws ExertionException, SignatureException {
		int count = 0;
		// get all children of the underlying parent job
		ExertionEnvelop template = ExertionEnvelop.getTakeTemplate(xrt.getId(),
				null);
		logger
				.finer("<===================== collect exertions for template: \n"
						+ template.describe());
		while (count < inputXrts.size() && state != FAILED) {
			ExertionEnvelop resultEnvelop = (ExertionEnvelop) takeEnvelop(template);
			if (resultEnvelop != null && resultEnvelop.exertion != null) {
				ServiceExertion input = (ServiceExertion) ((NetJob)xrt)
						.exertionAt(((ServiceExertion) resultEnvelop.exertion)
								.getIndex());
				logger
						.finer("collected result envelope  <===================== \n"
								+ resultEnvelop.describe());
				ServiceExertion result = (ServiceExertion) resultEnvelop.exertion;
				postExecExertion(input, result);
				count++;
			} else {
				logger.finest("continue for envelop: " + resultEnvelop);
				continue;
			}
		}
		executeMasterExertion();
		dispatchers.remove(xrt.getId());
		state = DONE;
	}
}
