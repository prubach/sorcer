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
package sorcer.ex3.requestor;

import sorcer.core.context.ServiceContext;
import sorcer.service.Signature.Type;
import sorcer.service.Signature.Direction;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context.*;
import sorcer.ex2.provider.InvalidWork;
import sorcer.ex2.provider.Work;
import sorcer.service.*;

public class ContextWorkReq extends ServiceRequestor {
    ServiceContext context = new ServiceContext("common/context");
	
	public Exertion getExertion(String... args) throws ExertionException {
		String requestorName = getProperty("requestor.name");
		String p1 = getProperty("op1.prefix");
		String p2 = getProperty("op2.prefix");
		String p3 = getProperty("op3.prefix");
		
		// define requestor data
        try {
			System.out.println("reqName " + requestorName);
			context.putValue("requestor/name", requestorName);

            System.out.println("p1: " + p1);
            context.putValue(p1+"/requestor/operand/1", 20);
			context.putValue(p1+"/requestor/operand/2", 80);
            context.putValue(p1+"/requestor/work", Works.work1);
            context.putValue(p1+"/provider/result", Value.NULL);
			
			System.out.println("p2: " + p2);
			context.putValue(p2+"/requestor/operand/1", 10);
			context.putValue(p2+"/requestor/operand/2", 50);
            context.putValue(p2+"/requestor/work", Works.work2);
            context.putValue(p2+"/provider/result", Value.NULL);
			
			System.out.println("p3: " + p3);
            context.putValue(p3+"/requestor/operand/1", Value.NULL);
            context.putValue(p3+"/requestor/operand/2", Value.NULL);
            context.putValue(p3+"/requestor/work", Works.work3);
            context.putValue(p3+"/provider/result", Value.NULL);

            // define required signatures
			NetSignature signature1 = new NetSignature("doWork#"+p1,
					sorcer.ex2.provider.Worker.class);
            signature1.setType(Type.PRE).setReturnPath(p3+"/requestor/operand/2");
            NetSignature signature2 = new NetSignature("doWork#"+p2,
					sorcer.ex2.provider.Worker.class);
            signature2.setType(Type.SRV).setReturnPath(p3+"/requestor/operand/1");
            NetSignature signature3 = new NetSignature("doWork#"+p3,
					sorcer.ex2.provider.Worker.class);
            signature3.setType(Type.POST);

			// define a task
            Task task = new NetTask("common", signature1, context);
            task.addSignature(signature2);
            task.addSignature(signature3);

		    return task;
        } catch (Exception e) {
            throw new ExertionException("Failed to create exertion", e);
        }
	}

}