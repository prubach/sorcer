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

import org.junit.Ignore;
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
import sorcer.service.ServiceExertion;

import java.net.InetAddress;

@RunWith(SorcerSuite.class)
@Category(SorcerClient.class)
@ExportCodebase(
        "org.sorcersoft.sorcer:ex1-api"
)
@SorcerServiceConfigurations({
        @SorcerServiceConfiguration(
                ":ex1-cfg-all"
        ),
        @SorcerServiceConfiguration({
                ":ex1-cfg1",
                ":ex1-cfg2"
        })
})
@Ignore
public class WhoIsItSequentialTaskTest {

	private static Logger logger = LoggerFactory.getLogger(WhoIsItSequentialTaskTest.class);

    @Test
	public void whoIsItSequential() throws Exception {
		int tally = 5;
		// create a service task and execute it 'tally' times
		long start = System.currentTimeMillis();
		for (int i = 0; i < tally; i++) {
			Exertion task = getExertion();
			((ServiceExertion)task).setName(task.getName() + "-" + i);
			task = task.exert(null);
			logger.info("got sequentially executed task: {}", task.getName());
            ExertionErrors.check(task.getExceptions());
        }
		long end = System.currentTimeMillis();
        logger.info("Execution time for {} sequential tasks : {} ms.", tally, (end - start));
    }

	public Exertion getExertion() throws Exception {
		String hostname;
		InetAddress inetAddress = InetAddress.getLocalHost();
		hostname = inetAddress.getHostName();

		Context context = new ServiceContext("Who Is It?");
		context.putValue("requestor/message", new RequestorMessage("SORCER"));
		context.putValue("requestor/hostname", hostname);
		
		NetSignature signature = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class);

        return new NetTask("Who Is It?", signature, context);
	}
}
