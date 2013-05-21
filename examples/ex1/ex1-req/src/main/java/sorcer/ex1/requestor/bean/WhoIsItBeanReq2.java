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

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.ex1.requestor.RequestorMessage;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Task;
import sorcer.util.Sorcer;

public class WhoIsItBeanReq2 extends ServiceRequestor {

	public Exertion getExertion(String... args) throws ExertionException {
		String hostname, ipAddress;
		InetAddress inetAddress;
		String providerName = null;
		Context context = null;
		NetSignature signature = null;
		// define requestor data
		if (args.length == 2)
			providerName = Sorcer.getSuffixedName(args[1]);
		logger.info("providerName: " + providerName);
		Task task = null;
		try {
			inetAddress = InetAddress.getLocalHost();

			hostname = inetAddress.getHostName();
			ipAddress = inetAddress.getHostAddress();

			context = new ServiceContext("Who Is It?");
			context.putValue("requestor/message", new RequestorMessage(
					"WhoIsIt Bean"));
			context.putValue("requestor/hostname", hostname);
			context.putValue("requestor/address", ipAddress);

			signature = new NetSignature("getHostName",
					sorcer.ex1.WhoIsIt.class, providerName != null ? providerName : null);
			
			task = new NetTask("Who Is It?", signature, context);
		} catch (Exception e) {
			throw new ExertionException("Failed to create exertion", e);
		}
		return task;
	}

	public void postprocess() {
		logger.info("<<<<<<<<<< Exceptions: \n" + exertion.getExceptions());
		logger.info("<<<<<<<<<< Trace list: \n" + exertion.getControlContext().getTrace());
		logger.info("<<<<<<<<<< BeanReq2 Result: \n" + exertion);
	}
	
}
