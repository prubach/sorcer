/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.service.*;

import static sorcer.service.Exec.*;

public class SpaceTaskDispatcher extends SpaceParallelDispatcher {

	public SpaceTaskDispatcher(final Task task,
            final Set<Context> sharedContexts,
            final boolean isSpawned, 
            final LokiMemberUtil myMemberUtil,
            final ProvisionManager provisionManager,
            final ProviderProvisionManager providerProvisionManager) throws ContextException, ExertionException {
        super(task, sharedContexts, isSpawned, myMemberUtil, null, provisionManager, providerProvisionManager);
	}

    @Override
    protected List<Exertion> getInputExertions() throws ContextException {
        return Arrays.asList((Exertion)xrt);
    }

	public void collectResults() throws ExertionException, SignatureException {
		ExertionEnvelop temp;
		temp = ExertionEnvelop.getTemplate();
		temp.exertionID = xrt.getId();
		temp.state = DONE;

		logger.debug("template for space task to be collected: {}",
				temp.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(temp);
		if (resultEnvelop != null) {
			logger.debug("collected result envelope {}",
					resultEnvelop.describe());
			
			Task result = (Task) resultEnvelop.exertion;
			state = DONE;
			result.setStatus(DONE);
			xrt = result;
		}
		dispatchers.remove(xrt.getId());
	}
	
	public void collectFails() throws ExertionException {
		ExertionEnvelop template;
		template = ExertionEnvelop.getTemplate();
		template.exertionID = xrt.getId();
		template.state = FAILED;

		logger.debug("template for failed task to be collected: {}",
				template.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(template);
		if (resultEnvelop != null) {
			Task result = (Task) resultEnvelop.exertion;
			state = FAILED;
			result.setStatus(FAILED);
			xrt = result;
		}
		dispatchers.remove(xrt.getId());
	}
	
	public void collectErrors() throws ExertionException {
		ExertionEnvelop template;
		template = ExertionEnvelop.getTemplate();
		template.exertionID = xrt.getId();
		template.state = ERROR;

		logger.debug("template for error task to be collected: {}",
				template.describe());

		ExertionEnvelop resultEnvelop = takeEnvelop(template);
		if (resultEnvelop != null) {
			Task result = (Task) resultEnvelop.exertion;
			state = ERROR;
			result.setStatus(ERROR);
			xrt = result;
		}
		dispatchers.remove(xrt.getId());
	}

}
