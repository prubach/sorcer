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

package sorcer.falcon.validation.differentiation.provider;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.id.UuidFactory;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.validation.base.DerivativeRemote;
import sorcer.falcon.validation.base.FunctionEvaluatorRemote;
import sorcer.falcon.validation.context.CalcContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ServiceExertion;
import sorcer.service.Servicer;
import sorcer.util.Log;
import sorcer.util.ProviderAccessor;

import com.sun.jini.start.LifeCycle;

/**
 * A simple provider that evaluates the derivative of a single variable
 * function.
 * 
 * @author Michael Alger
 */
public class DerivativeImpl extends ServiceProvider implements DerivativeRemote {

	final static Logger testLog = Log.getTestLog(); // logger for testing

	/**
	 * This constructor is needed for the Jini Extensible Remote Invocation
	 * (JERI)
	 * 
	 * @param args
	 *            Array of Strings
	 * @param lifeCycle
	 *            LifeCycle jini 2.x specific
	 * @throws RemoteException
	 */
	public DerivativeImpl(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle); // calls constructor of SorcerProvider
	}

	/**
	 * This method evaluates a single variable function
	 * 
	 * @param context
	 *            ServiceContext
	 * @return ServiceContext
	 */
	public Context evaluateDerivative(Context context) throws RemoteException {
		try {
			double h = 1 / ((CalcContext) context).getHValue();

			Exertion fnEvalTask = createTask(context);
			Servicer functionEval = ProviderAccessor.getProvider(null,
					FunctionEvaluatorRemote.class);
			Exertion resultExertion = functionEval.service(fnEvalTask, null);

			double resultXH = ((CalcContext) resultExertion.getContext())
					.getScalarFnResultXH();
			double resultX = ((CalcContext) resultExertion.getContext())
					.getScalarFnResultX();
			double estimate = (resultXH - resultX) / h;

			double iteration = ((CalcContext) context).getIterValue() + 1;
			double oldResult = ((CalcContext) context).getDerivativeFnResult();

			((CalcContext) context).setOldDerivativeFnResult(oldResult);
			((CalcContext) context).setDerivativeFnResult(estimate);
			((CalcContext) context).setIterValue(iteration);

			testLog.info("Approximation result: H: " + h + " --- Estimate: "
					+ estimate);
		} catch (ContextException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return context; // return the ServiceContext back to the requestor
	}

	/**
	 * Creates a task for the Function-Evaluator provider
	 * 
	 * @return RemoteServiceTask
	 */
	public ServiceExertion createTask(Context context) {

		// create the service method, arguements follows: method name, interface
		// name, and provider name (which is optional)
		NetSignature method;
		ServiceExertion task = null;
		method = new NetSignature("evaluateFunction",
				FunctionEvaluatorRemote.class, "Function-Evaluator");

		// create the task and insert the service method
		task = new NetTask("fnTask", "function evluator task", method);

		// now set the context to the task
		task.setContext(context);

		// assign a unique exertion ID across multiple VM
		task.setId(UuidFactory.generate());

		return task;
	}

}
