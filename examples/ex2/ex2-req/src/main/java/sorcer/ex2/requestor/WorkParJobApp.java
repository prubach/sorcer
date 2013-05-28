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
package sorcer.ex2.requestor;

import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Task;
import sorcer.util.Log;
import sorcer.util.Sorcer;

import java.rmi.RMISecurityManager;
import java.util.Arrays;
import java.util.logging.Logger;

public class WorkParJobApp {

	private static Logger logger = Log.getTestLog();

	public static void main(String... args) throws Exception {
		System.setSecurityManager(new RMISecurityManager());
        ServiceRequestor.prepareCodebase();
		// initialize system properties
		Sorcer.getEnvProperties();

		Exertion result = new WorkParJobApp()
			.getExertion().exert();

		// get contexts of component exertions - in this case tasks
		logger.info("Output context1: \n" + result.getContext("work1"));
		logger.info("Output context2: \n" + result.getContext("work2"));
		logger.info("Output context3: \n" + result.getContext("work3"));
	}

	private Exertion getExertion(String... args) throws Exception {
		String hostname = SorcerEnv.getLocalHost().getHostName();
        logger.info("arg: " + Arrays.toString(args));
        String pn1 = null, pn2 = null, pn3 = null;
        if (args != null && args.length == 3)  {
            pn1 = Sorcer.getSuffixedName(args[0]);
            pn2 = Sorcer.getSuffixedName(args[1]);
            pn3 = Sorcer.getSuffixedName(args[2]);
        }

		Context context1 = new ServiceContext("work1");
		context1.putValue("requstor/name", hostname);
		context1.putValue("requestor/operand/1", 1);
		context1.putValue("requestor/operand/2", 1);
        context1.putValue("to/provider/name", pn1);
        context1.putValue("requestor/work", Works.work1);

        Context context2 = new ServiceContext("work2");
		context2.putValue("requstor/name", hostname);
		context2.putValue("requestor/operand/1", 2);
		context2.putValue("requestor/operand/2", 2);
        context1.putValue("to/provider/name", pn2);
        context2.putValue("requestor/work", Works.work2);

        Context context3 = new ServiceContext("work3");
		context3.putValue("requstor/name", hostname);
		context3.putValue("requestor/operand/1", 3);
		context3.putValue("requestor/operand/2", 3);
        context1.putValue("to/provider/name", pn3);
        context3.putValue("requestor/work", Works.work3);

        NetSignature signature1 = new NetSignature("doWork",
				sorcer.ex2.provider.Worker.class, pn1);
		NetSignature signature2 = new NetSignature("doWork",
				sorcer.ex2.provider.Worker.class, pn2);
		NetSignature signature3 = new NetSignature("doWork",
				sorcer.ex2.provider.Worker.class, pn3);
		
		Task task1 = new NetTask("work1", signature1, context1);
		Task task2 = new NetTask("work2", signature2, context2);
		Task task3 = new NetTask("work3", signature3, context3);
		Job job = new NetJob();
		job.addExertion(task1);
		job.addExertion(task2);
		job.addExertion(task3);
		job.setFlow(Flow.PAR);
		job.setAccess(Access.PULL);
		return job;
	}
}
