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

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.*;
import sorcer.util.Log;

import java.net.InetAddress;
import java.util.logging.Logger;

public class WhoIsItSequentialTaskRunner extends ServiceRequestor {

	private static Logger logger = Log.getTestLog();

	@Override
	public void process(String... args) throws ExertionException {
		int tally = 3;
		if (args.length == 2)
			tally = new Integer(args[1]);
		// create a service task and execute it 'tally' times
		Exertion task = null;
		long start = System.currentTimeMillis();
		for (int i = 0; i < tally; i++) {
			task = getExertion();
			((ServiceExertion)task).setName(task.getName() + "-" + i);
            try {
                task = task.exert(null);
            } catch (Exception e) {
                throw new ExertionException(e);
            }
            logger.info("got sequentially executed task: " + task.getName());
		}
		long end = System.currentTimeMillis();
		System.out.println("Execution time for " + tally + " sequential tasks : " + (end - start) + " ms.");
	}
	
	@Override
	public Exertion getExertion(String... args) throws ExertionException {
		String hostname;
		Task task;
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
		
		hostname = inetAddress.getHostName();

		Context context = new ServiceContext("Who Is It?");
		context.putValue("requestor/message", new RequestorMessage("SORCER"));
		context.putValue("requestor/hostname", hostname);
		
		NetSignature signature = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class);

		task = new NetTask("Who Is It?", signature, context);
		} catch (Exception e) {
			throw new ExertionException(e);
		}
		return task;
	}

	@Override
	public void preprocess(String... args) {
		// do nothing
	}

}