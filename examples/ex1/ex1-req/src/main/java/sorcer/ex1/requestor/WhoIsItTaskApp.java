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
import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Strategy.Access;
import sorcer.service.Task;
import sorcer.util.Log;
import sorcer.util.Sorcer;

public class WhoIsItTaskApp {

	private static Logger logger = Log.getTestLog();

	public static void main(String... args) throws Exception {
		System.setSecurityManager(new RMISecurityManager());
		// initialize system environment from configs/sorcer.env
		Sorcer.getEnvProperties();
		Access providerAccess = Access.PUSH;
		if (args.length == 1) {
			if (!args[0].equals(Access.PUSH))
				providerAccess = Access.PULL;
		}
		Exertion result = new WhoIsItTaskApp().getExertion(providerAccess)
				.exert();
		logger.info("Output dataContext: \n" + result.getDataContext());
	}

	public Exertion getExertion(Access providerAccess) throws Exception {
		String hostname;
		InetAddress inetAddress = SorcerEnv.getLocalHost();
		hostname = inetAddress.getHostName();

		Context context = new ServiceContext("Who Is It?");
		context.putValue("requestor/message", new RequestorMessage("friend"));
		context.putValue("requestor/hostname", hostname);
		
		NetSignature signature = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class);

		Task task = new NetTask("Who Is It?", signature, context);
		task.setAccess(providerAccess);
		return task;
	}
}
