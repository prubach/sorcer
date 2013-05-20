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
package sorcer.ex1.requestor.bean;

import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.ex1.requestor.RequestorMessage;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Task;
import sorcer.util.Log;
import sorcer.util.Sorcer;

public class WhoIsItBeanApp2 {

	private static Logger logger = Log.getTestLog();
	private static String providerName;
	
	public static void main(String... args) throws Exception {
		System.setSecurityManager(new RMISecurityManager());
		if (args.length == 1)
			providerName = Sorcer.getSuffixedName(args[0]);
		logger.info("providerName: " + providerName);
		
		Exertion result = new WhoIsItBeanApp2()
			.getExertion().exert(null);
		logger.info("<<<<<<<<<< Exceptions: \n" + result.getExceptions());
		logger.info("<<<<<<<<<< Trace list: \n" + result.getControlContext().getTrace());
		logger.info("<<<<<<<<<< Result: \n" + result);
	}

	private Exertion getExertion() throws Exception {
		String hostname, ipAddress;
		InetAddress inetAddress = InetAddress.getLocalHost();
		hostname = inetAddress.getHostName();
		ipAddress = inetAddress.getHostAddress();

		Context context = new ServiceContext("Who Is It?");
		context.putValue("requestor/message", new RequestorMessage(
				"WhoIsIt Bean"));
		context.putValue("requestor/hostname", hostname);
		context.putValue("requestor/address", ipAddress);

		NetSignature signature = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class, providerName != null ? providerName : null);

		Task task = new NetTask("Who Is It?",signature, context);
		return task;
	}
}
