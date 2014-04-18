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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionCallable;
import sorcer.service.ServiceExertion;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex1-api",
        "org.sorcersoft.sorcer:ex1-rdl"
})
@SorcerServiceConfigurations(
        @SorcerServiceConfiguration( { ":ex1-cfg1" }
   )
)
public class WhoIsItParallelTaskTest {

	private static Logger logger = LoggerFactory.getLogger(WhoIsItParallelTaskTest.class);

    @Test
	public void whoIsItParallel() throws Exception {
		int tally = 5;

		List<Future<Exertion>> fList = new ArrayList<Future<Exertion>>(tally);
		ExecutorService pool = Executors.newFixedThreadPool(tally);
		long start = System.currentTimeMillis();
		for (int i = 0; i < tally; i++) {
            Exertion task = getExertion();
			((ServiceExertion)task).setName(task.getName() + "-" + i);
            ExertionCallable ec = new ExertionCallable(task);
			logger.info("exertion submit: {}", task.getName());
			Future<Exertion> future = pool.submit(ec);
			fList.add(future);
		}
		pool.shutdown();
		for (int i = 0; i < tally; i++) {
            Exertion exertion = fList.get(i).get();
            logger.info("got back task executed in parallel: {}", exertion.getName());
            ExertionErrors.check(exertion.getExceptions());
		}
		long end = System.currentTimeMillis();
        logger.info("Execution time for {} parallel tasks : {} ms.", tally, (end - start));
	}

	private Exertion getExertion() throws Exception {
		String hostname;
		InetAddress inetAddress = InetAddress.getLocalHost();
		hostname = inetAddress.getHostName();

		Context context = new ServiceContext("Who Is It?");
		context.putValue("requestor/message",  new RequestorMessage("SORCER"));
		context.putValue("requestor/hostname", hostname);
		
		NetSignature signature = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class);

        return new NetTask("Who Is It?",  signature, context);
	}
}
