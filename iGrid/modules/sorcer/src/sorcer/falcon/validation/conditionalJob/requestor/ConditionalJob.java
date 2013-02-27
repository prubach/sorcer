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

package sorcer.falcon.validation.conditionalJob.requestor;

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
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.core.exertion.IfExertion;
import sorcer.falcon.core.exertion.WhileExertion;
import sorcer.falcon.validation.base.DerivativeRemote;
import sorcer.falcon.validation.base.InitializeConditionRemote;
import sorcer.falcon.validation.base.IntegralRemote;
import sorcer.falcon.validation.condition.DerivativeWhileCondition;
import sorcer.falcon.validation.condition.IfCondition;
import sorcer.falcon.validation.condition.IntegralWhileCondition;
import sorcer.falcon.validation.context.CalcContext;
import sorcer.falcon.validation.differentiation.requestor.FunctionImpl;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.Jobber;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.util.Log;
import sorcer.util.ProviderAccessor;

/**
 * Example of a WhileExertion task for the Derivative-Evaluater provider.
 * 
 * @author Michael Alger
 */

public class ConditionalJob {

	private static Logger log = Log.getTestLog(); // logger framework

	/**
	 * Main method for testing only
	 */
	public static void main(String[] args) {

		ConditionalJob client = new ConditionalJob();

		client.executeConditionalJob(); // execute the Task
	}

	/**
	 * The default constructor which sets the RMI Security Manager
	 */
	public ConditionalJob() {
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
	public void executeConditionalJob() {
		try {
			Map<String, Object> ifMapping = new HashMap<String, Object>();
			ifMapping.put("value", "in/value/sentinel");
		
			Map<String, Object> wIntegralMapping = new HashMap<String, Object>();
			wIntegralMapping.put("result", CalcContext.getIntegralFnPath());
			wIntegralMapping.put("oldResult", CalcContext
					.getOldIntegralFnPath());
			wIntegralMapping.put("iteration", CalcContext.getIterPath());
			wIntegralMapping.put("N", CalcContext.getNPath());
			wIntegralMapping.put(SorcerConstants.C_INCREMENT + "N", new Double(
					100));
			Map<String, Object> wDerivativeMapping = new HashMap<String, Object>();
			wDerivativeMapping.put("result", CalcContext.getDerivativeFnPath());
			wDerivativeMapping.put("oldResult", CalcContext
					.getOldDerivativeFnPath());
			wDerivativeMapping.put("iteration", CalcContext.getIterPath());
			wDerivativeMapping.put("H", CalcContext.getHPath());
			wDerivativeMapping.put(SorcerConstants.C_INCREMENT + "H",
					new Double(100));

			String booleanIfCondition = "value <= 4";
			String booleanWhileDerivativeCondition = "abs(result - oldResult) > 0.00001 && iteration < 1000";
			String booleanWhileIntegralCondition = "abs(result - oldResult) > 0.00001 && iteration < 1000";

			IfCondition ifCondition = new IfCondition(booleanIfCondition,
					ifMapping);
			IntegralWhileCondition whileIntegralCondition = new IntegralWhileCondition(
					booleanWhileIntegralCondition, wIntegralMapping);
			DerivativeWhileCondition whileDerivativeCondition = new DerivativeWhileCondition(
					booleanWhileDerivativeCondition, wDerivativeMapping);

			Exertion initializeTask = createInitializeTask();
			Exertion derivativeTask = createDerivativeTask();
			Exertion integralTask = createIntegralTask();

			WhileExertion whileDerivativeTask = new WhileExertion(
					whileDerivativeCondition, derivativeTask);
			WhileExertion whileIntegralTask = new WhileExertion(
					whileIntegralCondition, integralTask);

			IfExertion ifTask = new IfExertion(ifCondition,
					whileDerivativeTask, whileIntegralTask);
			Job job = createFnJob();

			// add the exertion to the job
			job.addExertion(initializeTask);
			job.addExertion(ifTask);
			job.setMasterExertion(ifTask);

			// map the output of the
			ifTask.getContext().putInValue("in/value/sentinel", 0);
			Contexts.map("out/value/result", initializeTask.getContext(),
					"in/value/sentinel", ifTask.getContext());

			Jobber jobber = (Jobber) ProviderAccessor.getProvider(
					"SORCER-Jobber", Jobber.class);
			log.info("Jobber Proxy: " + jobber);

			// execute the exertion on the provider using the remote proxy
			Exertion resultJob = jobber.service(job, null);

			// retrieves the service context or service data
			CalcContext resultContext = (CalcContext) ((WhileExertion) ((IfExertion) ((Job) resultJob)
					.getMasterExertion()).getResult()).getContext();

			double derivativeResult = resultContext.getDerivativeFnResult();
			double oldDerivativeResult = resultContext
					.getOldDerivativeFnResult();
			double deltaDerivativeResult = Math.abs(derivativeResult
					- oldDerivativeResult);

			double integralResult = resultContext.getIntegralFnResult();
			double oldIntegralResult = resultContext.getOldIntegralFnResult();
			double deltaIntegralResult = Math.abs(integralResult
					- oldIntegralResult);

			System.out.println("Function: x^3");
			System.out.println("Where: x = " + resultContext.getXValue()
					+ "   h = " + 1 / resultContext.getHValue() + "   n = "
					+ resultContext.getNValue());
			System.out.println("While Condition: ("
					+ booleanWhileDerivativeCondition + ")");
			System.out.println("Derivative Result: "
					+ resultContext.getDerivativeFnResult());
			System.out.println("Derivative Delta Result: "
					+ deltaDerivativeResult);
			System.out.println("Integration Result: "
					+ resultContext.getIntegralFnResult());
			System.out.println("Integration Delta Result: "
					+ deltaIntegralResult);
			System.out.println("Iteration count: "
					+ resultContext.getIterValue());
			System.out.println("Selection: "
					+ (((Job) resultJob).getMasterExertion()).getContext()
							.getValue("in/value/sentinel"));
			System.out.println("If Condition: (" + booleanIfCondition + ")");
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
			log.severe("Transaction problem");
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
	 * @return RemoteServiceTask The elementary exertion for
	 *         Derivative-Evaluater
	 * @see CalcContext
	 * @see RemoteServiceSignature
	 * @see NetTask
	 */
	public NetTask createDerivativeTask() {

		// create the service context of type SimpleContext
		CalcContext context = new CalcContext("DerivativeContext");
		NetTask task = null;

		// create the service method, arguements follows: method name, interface
		// name, and provider name (which is optional)
		NetSignature method;
		try {
			method = new NetSignature("evaluateDerivative",
					DerivativeRemote.class, "Derivative-Evaluator");

			// create the task and insert the service method
			task = new NetTask("DTask", "derivative task example", method);

			// now set the context to the task
			task.setContext(context);

			// assign a unique exertion ID across multiple VM
			task.setId(UuidFactory.generate());

			context.setXValue(3);
			context.setHValue(100);
			context.setIterValue(0);
			context.setFunction(new FunctionImpl());
			context.setOldDerivativeFnResult(1);
			context.setDerivativeFnResult(0);

			context.setAValue(0);
			context.setBValue(1);
			context.setNValue(100);
			context.setOldIntegralFnResult(1);
			context.setIntegralFnResult(0);
		} catch (ContextException ce) {
			log.severe("Problem adding data nodes into the context: " + ce);
			ce.printStackTrace();
		}

		return task;
	}

	/**
	 * This method creates a simple task of type RemoteServiceTask. A
	 * RemoteServiceTask is composed of a RemoteServiceMethod and a
	 * ServiceContext (data). In this case, the type of ServiceContext will be
	 * 
	 * @param taskName
	 *            The name of the ServiceTask and the message passed to the
	 *            provider
	 * @return RemoteServiceTask The elementary exertion for
	 *         Derivative-Evaluater
	 * @see CalcContext
	 * @see RemoteServiceSignature
	 * @see NetTask
	 */
	public NetTask createInitializeTask() {

		// create the service context of type SimpleContext
		Context context = new ServiceContext("InitializeContext");
		NetTask task = null;

		// create the service method, arguements follows: method name, interface
		// name, and provider name (which is optional)
		NetSignature method;
		try {
			method = new NetSignature("initializeCondition",
					InitializeConditionRemote.class,
					"Initialize-Condition");

			// create the task and insert the service method
			task = new NetTask("InitializeTask", "simple initialize task",
					method);

			// now set the context to the task
			task.setContext(context);

			// assign a unique exertion ID across multiple VM
			task.setId(UuidFactory.generate());

			context.putInValue("out/value/result", 0);
		} catch (ContextException e) {
			e.printStackTrace();
		}

		return task;
	}

	/**
	 * This method creates a simple task of type RemoteServiceTask. A
	 * RemoteServiceTask is composed of a RemoteServiceMethod and a
	 * ServiceContext (data). In this case, the type of ServiceContext will be
	 * 
	 * @param taskName
	 *            The name of the ServiceTask and the message passed to the
	 *            provider
	 * @return RemoteServiceTask The elementary exertion for
	 *         Derivative-Evaluater
	 * @see CalcContext
	 * @see RemoteServiceSignature
	 * @see NetTask
	 */
	public NetTask createIntegralTask() {
		// create the service context of type SimpleContext
		CalcContext context = new CalcContext("IntegralContext");
		NetTask task = null;

		// create the service method, arguements follows: method name, interface
		// name, and provider name (which is optional)
		NetSignature method;
		try {
			method = new NetSignature("evaluateIntegral",
					IntegralRemote.class, "Integral-Evaluator");

			// create the task and insert the service method
			task = new NetTask("integralTask", "integral task example",
					method);

			// now set the context to the task
			task.setContext(context);

			// assign a unique exertion ID across multiple VM
			task.setId(UuidFactory.generate());

			context.setXValue(3);
			context.setHValue(100);
			context.setIterValue(0);
			context.setFunction(new FunctionImpl());
			context.setOldDerivativeFnResult(1);
			context.setDerivativeFnResult(0);

			context.setAValue(0);
			context.setBValue(1);
			context.setNValue(100);
			context.setOldIntegralFnResult(1);
			context.setIntegralFnResult(0);
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

		Job job = new NetJob("*** Fn Job 1 ***"); // create a ServcieJob

		// set the ServiceJob attributes
		job.setId(UuidFactory.generate()); // set the exertion id
		((ControlContext) job.getContext())
				.setAccessType(Access.PUSH); // use the catalog
		// to delegate the
		// tasks
		((ControlContext) job.getContext())
				.setFlowType(Flow.SEQ); // either parallel
		// or sequential
		((ControlContext) job.getContext()).setExecTimeRequested(true); // time
		// the
		// job
		// execution
		((ControlContext) job.getContext()).setMonitored(false); // job can
		// be
		// monitored
		((ControlContext) job.getContext()).setWait(true);

		return job;
	}
}