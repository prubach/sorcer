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

package sorcer.ex3.runner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.ex2.requestor.Works;
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Task;

@SuppressWarnings("rawtypes")
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex2-api",
        "org.sorcersoft.sorcer:ex2-rdl"
})
@SorcerServiceConfiguration({
        ":ex2-cfg1",
        ":ex2-cfg2",
        ":ex2-cfg3"
})
//@Ignore("Works only if run separately")
public class FlowAccessWorkerTest {

    @Test
	public void getExertion() throws Exception {
		String requestorName = System.getProperty("user.name");
		String pn1, pn2, pn3;
        pn1 = SorcerEnv.getSuffixedName("Worker1");
        pn2 = SorcerEnv.getSuffixedName("Worker1");
        pn3 = SorcerEnv.getSuffixedName("Worker1");
        Job job;
        try {
            Context context1 = new ServiceContext("work1");
            context1.putValue("requestor/name", requestorName);
            context1.putValue("requestor/operand/1", 1);
            context1.putValue("requestor/operand/2", 1);
            context1.putValue("requestor/work", Works.work1);
            context1.putValue("to/provider/name", pn1);

            Context context2 = new ServiceContext("work2");
            context2.putValue("requestor/name", requestorName);
            context2.putValue("requestor/operand/1", 2);
            context2.putValue("requestor/operand/2", 2);
            context2.putValue("requestor/work", Works.work2);
            context2.putValue("to/provider/name", pn2);

            Context context3 = new ServiceContext("work3");
            context3.putValue("requestor/name", requestorName);
            context3.putValue("requestor/operand/1", 3);
            context3.putValue("requestor/operand/2", 3);
            context3.putValue("requestor/work", Works.work3);
            context3.putValue("to/provider/name", pn3);


			NetSignature signature1 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class, pn1);
			NetSignature signature2 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class, pn2);
			NetSignature signature3 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class, pn3);

			Task task1 = new NetTask("work1", signature1, context1);
			task1.setExecTimeRequested(true);
			Task task2 = new NetTask("work2", signature2, context2);
			Task task3 = new NetTask("work3", signature3, context3);
			job = new NetJob("flow");
			job.setExecTimeRequested(true);
			job.addExertion(task1);
			job.addExertion(task2);
			job.addExertion(task3);
						
			// PUSH or PULL provider access
            job.setAccessType(Access.PULL);
			
			// Exertion control flow PARallel or SEQential
            job.setFlowType(Flow.PAR);
			
//			logger.info("isPushAccess: " + isPushAccess + " iSequential: " + iSequential);
		} catch (Exception e) {
			throw new ExertionException("Failed to create exertion", e);
		}
        Exertion result = job.exert();
        ExertionErrors.check(result.getExceptions());
	}

}