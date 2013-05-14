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
package sorcer.ex1.requestor;

import java.net.InetAddress;
import java.util.logging.Logger;

import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Signature;
import sorcer.service.Task;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.util.Log;
import sorcer.util.Sorcer;

public class WhoIsItParallelTaskRunner extends ServiceRequestor {

	private static Logger logger = Log.getTestLog();

	public Exertion getExertion(String... args) throws ExertionException {
		// get the queried provider names and the requested jobber name
		// arg[0] is the class name of this runner
		String providerName1 = Sorcer.getSuffixedName(args[1]);
		String providerName2 = Sorcer.getSuffixedName(args[2]);
		jobberName = Sorcer.getSuffixedName(args[3]);
		String hostname = null;
		String ipAddress = null;
		InetAddress inetAddress = null;
		// define requestor data
		Job job = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			hostname = inetAddress.getHostName();
			ipAddress = inetAddress.getHostAddress();

			Context context1 = new ServiceContext("Who Is It?");
			context1.putValue("requestor/message", new RequestorMessage(
					providerName1));
			context1.putValue("requestor/hostname", hostname);

			Context context2 = new ServiceContext("Who Is It?");
			context2.putValue("requestor/message", new RequestorMessage(
					providerName2));
			context2.putValue("requestor/hostname", hostname);
			context2.putValue("requestor/address", ipAddress);

			NetSignature signature1 = new NetSignature("getHostName",
					sorcer.ex1.WhoIsIt.class, providerName1);
			NetSignature signature2 = new NetSignature("getHostAddress",
					sorcer.ex1.WhoIsIt.class, providerName2);

			Task task1 = new NetTask("Who Is It1?", signature1, context1);
			Task task2 = new NetTask("Who Is It2?", signature2,  context2);
			job = new NetJob();
			job.addExertion(task1);
			job.addExertion(task2);
		} catch (Exception e) {
			throw new ExertionException("Failed to create exertion", e);
		}
		ControlContext cc = job.getControlContext();
		// PUSH or PULL provider access
		cc.setAccessType(Access.PULL);
		// Exertion control flow PAR or SEQ
		cc.setFlowType(Flow.PAR);

		return job;
	}

	public void postprocess() {
		ServiceExertion.debug = true;
		logger.info("Output context1: \n"
				+ exertion.getContext("Who Is It1?"));
		logger.info("Output context2: \n"
				+ exertion.getContext("Who Is It2?"));
		logger.info("Output job: \n" + exertion);
	}

}
