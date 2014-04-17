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
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Task;

import java.net.InetAddress;

@RunWith(SorcerSuite.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex1-api",
        "org.sorcersoft.sorcer:ex1-rdl"
})
@SorcerServiceConfigurations({
        @SorcerServiceConfiguration(
                ":ex1-cfg-all"
        ),
        @SorcerServiceConfiguration({
                ":ex1-cfg1",
                ":ex1-cfg2"
        })
})

public class WhoIsItPullJobTest {

    private static Logger logger = LoggerFactory.getLogger(WhoIsItPullJobTest.class);

    @Test(expected = ExertionErrors.MultiException.class)
    public void whoIsItPullTest() throws Exception {
        // get the queried provider name
        String providerName1 = SorcerEnv.getSuffixedName("XYZ");
        String providerName2 = SorcerEnv.getSuffixedName("ABC");
        String jobberName = SorcerEnv.getSuffixedName("Spacer");

		logger.info("Who is '{}'?", providerName1);
		logger.info("Who is '{}'?", providerName2);

        Job ex = getExertion(providerName1, providerName2);
		Exertion result = ex.exert(null, jobberName);

		logger.info("Job exceptions job: \n" + result.getExceptions());
		logger.info("Output job: \n" + result);
		logger.info("Output context1: \n" + result.getContext("Who Is It1?"));
		logger.info("Output context2: \n" + result.getContext("Who Is It2?"));
        ExertionErrors.check(result.getExceptions());
	}

    private Job getExertion(String providerName1, String providerName2) throws Exception {
		String hostname, ipAddress;
		InetAddress inetAddress = InetAddress.getLocalHost();
		hostname = inetAddress.getHostName();
		ipAddress = inetAddress.getHostAddress();

		Context context1 = new ServiceContext("Who Is It?");
		context1.putValue("requestor/message", new RequestorMessage(providerName1));
		context1.putValue("requestor/hostname", hostname);

		Context context2 = new ServiceContext("Who Is It?");
		context2.putValue("requestor/message", new RequestorMessage(providerName2));
		context2.putValue("requestor/hostname", hostname);
		context2.putValue("requestor/address", ipAddress);

		NetSignature signature1 = new NetSignature("getHostName",
				sorcer.ex1.WhoIsIt.class, providerName1);
		NetSignature signature2 = new NetSignature("getHostAddress",
				sorcer.ex1.WhoIsIt.class, providerName2);

		Task task1 = new NetTask("Who Is It1?", signature1, context1);
		Task task2 = new NetTask("Who Is It2?", signature2, context2);
		Job job = new NetJob();
		job.addExertion(task1);
		job.addExertion(task2);

		// PUSH or PULL provider access
		job.setAccess(Access.PULL);
		// Exertion control flow PARALLEL or SEQUENTIAL
		job.setFlow(Flow.PAR);

		return job;
	}
}
