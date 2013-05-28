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
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Task;
import sorcer.util.Log;
import sorcer.util.Sorcer;

import java.rmi.RMISecurityManager;
import java.util.Arrays;
import java.util.logging.Logger;

public class WorkTaskApp {

	private static Logger logger = Log.getTestLog();

	public static void main(String... args) throws Exception {
		System.setSecurityManager(new RMISecurityManager());
        ServiceRequestor.prepareCodebase();
		// initialize system properties
		Sorcer.getEnvProperties();

		Exertion exertion = new WorkTaskApp().getExertion(args);
		Exertion result = exertion.exert();

		logger.info("Output dataContext: \n" + result.getDataContext());
	}

	private Exertion getExertion(String... args) throws Exception {
		String hostname = SorcerEnv.getLocalHost().getHostName();
        // get the queried provider name from the command line
        logger.info("arg: " + Arrays.toString(args));
        String pn = Sorcer.getSuffixedName(args[0]);
        logger.info("Provider name: " + pn);

		Context context = new ServiceContext("work");
		context.putValue("requstor/name", hostname);
		context.putValue("requestor/operand/1", 4);
		context.putValue("requestor/operand/2", 4);
		context.putValue("to/provider/name", pn);
        context.putValue("requestor/work", Works.work2);

        NetSignature signature = new NetSignature("doWork",
				sorcer.ex2.provider.Worker.class, pn);

		Task task = new NetTask("work", signature, context);

		return task;
	}
}
