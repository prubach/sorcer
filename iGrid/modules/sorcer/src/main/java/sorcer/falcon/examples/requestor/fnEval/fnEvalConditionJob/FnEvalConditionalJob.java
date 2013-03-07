/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.falcon.examples.requestor.fnEval.fnEvalConditionJob;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import sorcer.core.Provider;
import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.context.ControlContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.core.exertion.IfExertion;
import sorcer.falcon.examples.condition.FnIfCondition;
import sorcer.falcon.examples.context.FnContext;
import sorcer.falcon.examples.provider.fnEval.FnEvalRemote;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.Jobber;
import sorcer.service.Servicer;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.util.Log;
import sorcer.util.ProviderAccessor;

/**
 * This test is an example how to use falcon (Federated Algorithmic
 * MetaComputing). It is composed of new exertions with the capabilities of
 * iterations and branching.
 * 
 * @author Michael Alger
 */

public class FnEvalConditionalJob {

	private static Logger log = Log.getTestLog(); // logger framework

	Map<String, String> map; // using HashMap implementation for mapping

	// variables name to data nodes

	/**
	 * Main method for testing only
	 */
	public static void main(String[] args) {

		FnEvalConditionalJob client = new FnEvalConditionalJob();

		client.executeTask(); // execute the Task
	}

	/**
	 * The default constructor which sets the RMI Security Manager
	 */
	public FnEvalConditionalJob() {
		// sets the RMI Security Manager for all permission stated in the policy
		// file
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	/**
	 * This method shows how to use SORCER's S2S framework.
	 * 
	 * @see Provider
	 * @see Exertion
	 * @see NetTask
	 * @see RemoteServiceContext
	 */
	public void executeTask() {

		// retrieve the Function-Evaluator remote proxy
		Servicer fnEval = ProviderAccessor.getProvider("SORCER-Jobber",
				Jobber.class);

		log.info("Function-Evaluator proxy: " + fnEval);

		// create the RemoteServiceJob
		Job job = createFnJob();
		NetTask task1 = createFnTask("2x");
		NetTask thenTask = createFnTask("1/x");
		NetTask elseTask = createFnTask("2x^2");

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("X1", "in/value/var1");
			map.put("result", "out/value/functionResult");
			// for increment if needed
			map.put(SorcerConstants.C_INCREMENT + "X1", new Double(1.0));

			// condition expression
			String expression = "X1 == 2";

			FnIfCondition ifCondition = new FnIfCondition(expression, map);
			IfExertion ifTask = new IfExertion(ifCondition, thenTask, elseTask);

			job.addExertion(task1);
			job.addExertion(ifTask);

			job.setMasterExertion(ifTask);

			Contexts.map("out/value/functionResult", task1.getContext(),
					"in/value/var1", elseTask.getContext());

			// Contexts.map("out/value/functionResult", task1.getContext(),
			// "in/value/var1", ifTask.getContext());

			// execute the exertion on the provider using the remote proxy
			Exertion resultJob = fnEval.service(job, null);

			// retrieves the service context or service data
			Context resultContext = ((Job) resultJob).getMasterExertion()
					.getContext();

			log
					.info("Function: "
							+ resultContext.getValue("in/value/function"));
			log.info("Where: x = " + resultContext.getValue("in/value/var1"));
			log.info("ifTask condition: \"" + expression + "\"");
			log.info("Result: "
					+ resultContext.getValue("out/value/functionResult"));
			log.info("checking mapping: "
					+ ((Context) resultContext
							.getValue("in/context/elseExertion"))
							.getValue("in/value/var1"));
		} catch (ContextException ce) {
			log.severe("Problem retrieving the data nodes from the context");
			ce.printStackTrace();
		} catch (ExertionException ee) {
			log.severe("Exertion problem");
			ee.printStackTrace();
		} catch (RemoteException re) {
			log.severe("Remote problem");
			re.printStackTrace();
		} catch (TransactionException te) {
			log.severe("Transactiov problem");
			te.printStackTrace();
		}

	}

	/**
	 * This method creates a simple task of type RemoteServiceTask. A
	 * RemoteServiceTask is composed of a RemoteServiceMethod and a
	 * ServiceContext (data). In this case, the type of ServiceContext will be
	 * 
	 * @param taskName
	 *            The name of the ServiceTask and the message passed to the
	 *            provider
	 * @return RemoteServiceTask The elementary exertion for Simple Provider
	 * @see FnContext
	 * @see RemoteServiceSignature
	 * @see NetTask
	 */
	public NetTask createFnTask(String function) {

		Map<String, String> map = new HashMap<String, String>();

		// create the service context of type SimpleContext
		FnContext context = new FnContext("Message");

		// create the service method, arguments follows: method name, interface
		// name, and provider name (which is optional)
		NetSignature method;
		NetTask task = null;
		method = new NetSignature("evaluateFunction", FnEvalRemote.class,
				"Function-Evaluator");

		// create the task and insert the service method
		task = new NetTask("FnTask", "WhileExertion example", method);

		// now set the context to the task
		task.setContext(context);

		// assign a unique exertion ID across multiple VM
		task.setId(UuidFactory.generate());

		try {
			// initialize the data nodes to be used by the function
			context.putInValue("in/value/var1", 1.0);
			context.putInValue("in/value/var2", 10.0);
			context.putOutValue("out/value/functionResult", 0.0);

			// set the mapping of the variable name to the path of the data
			// nodes
			map.put("x", "in/value/var1");
			map.put("y", "in/value/var2");

			// insert the map into the context for the provider to use
			context.setMapReference(map);

			// set the function for the provider to evaluate
			context.setFn(function);
		} catch (ContextException ce) {
			log.severe("Problem adding data nodes into the context: " + ce);
			ce.printStackTrace();
		}

		return task;
	}

	/**
	 * Example how to create a RemoteServiceJob composed of three simple
	 * RemoteServiceTask.
	 * 
	 * @return RemoteServiceJob The job containing the three RemoteServiceTask
	 * @see NetJob
	 * @see NetTask
	 */
	public Job createFnJob() {

		Job job = new NetJob("*** Fn Job 1 ***"); // create a
		// ServcieJob

		// create the three tasks
		// RemoteServiceTask task1 = createFnTask("1/x");
		// RemoteServiceTask task2 = createFnTask("2x^2");

		// add the three task into the job
		// job.addExertion(task1);
		// job.addExertion(task2);
		// job.setMasterExertion(task2); //explicitly specify the master
		// exertion

		// set the ServiceJob attributes
		job.setId(UuidFactory.generate()); // set the exertion id
		((ControlContext) job.getContext()).setAccessType(Access.PUSH); // use
																			// the
		// catalog to
		// delegate the
		// tasks
		((ControlContext) job.getContext()).setFlowType(Flow.SEQ); // either
																				// parallel
		// or sequential
		((ControlContext) job.getContext()).setExecTimeRequested(true); // time
		// the
		// job
		// execution
		((ControlContext) job.getContext()).setMonitored(false); // job
		// can
		// be
		// monitored
		((ControlContext) job.getContext()).setWait(true);

		return job;
	}
}
