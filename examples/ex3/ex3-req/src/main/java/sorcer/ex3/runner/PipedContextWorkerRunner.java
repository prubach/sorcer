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

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;

public class PipedContextWorkerRunner extends ServiceRequestor {

	public Exertion getExertion(String... args) throws ExertionException {
		String requestorName = getProperty("requestor.name");
		
		// define requestor data
		Job job = null;
		try {
			Context context1 = new ServiceContext("work1");

			context1.putValue("requestor/name", requestorName);

			context1.putValue("requestor/operand/1", 1);
			context1.putValue("requestor/operand/2", 1);
			context1.putOutValue("provider/result", 0);

			Context context2 = new ServiceContext("work2");
			context2.putValue("requestor/name", requestorName);
			context2.putValue("requestor/operand/1", 2);
			context2.putValue("requestor/operand/2", 2);
			context2.putOutValue("provider/result", 0);

			Context context3 = new ServiceContext("work3");
			context3.putValue("requestor/name", requestorName);
			context3.putInValue("requestor/operand/1", 0);
			context3.putInValue("requestor/operand/2", 0);

			// pass the parameters from one dataContext to the next dataContext
			// piping parameters should be annotated via in, out, or inout paths
			context1.connect("provider/result", "requestor/operand/1", context3);
			context2.connect("provider/result", "requestor/operand/2", context3);

			// define required services
			NetSignature signature1 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class);
			NetSignature signature2 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class);
			NetSignature signature3 = new NetSignature("doWork",
					sorcer.ex2.provider.Worker.class);

			// define tasks
			Task task1 = new NetTask("work1", signature1, context1);
			Task task2 = new NetTask("work2", signature2, context2);
			Task task3 = new NetTask("work3", signature3, context3);

			// define a job
			job = new NetJob("piped");
			job.addExertion(task1);
			job.addExertion(task2);
			job.addExertion(task3);
		} catch (Exception e) {
			throw new ExertionException("Failed to create exertion", e);
		}

		// define a job control strategy
		// use the catalog to delegate the tasks
		job.setAccessType(Access.PUSH);
		// either parallel or sequential
		job.setFlowType(Flow.SEQ);
		// time the job execution
		job.setExecTimeRequested(true);

		return job;
	}

}