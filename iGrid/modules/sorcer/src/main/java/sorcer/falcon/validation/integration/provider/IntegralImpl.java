package sorcer.falcon.validation.integration.provider;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.id.UuidFactory;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.validation.base.Function;
import sorcer.falcon.validation.base.FunctionEvaluatorRemote;
import sorcer.falcon.validation.base.IntegralRemote;
import sorcer.falcon.validation.context.CalcContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ServiceExertion;
import sorcer.util.Log;

import com.sun.jini.start.LifeCycle;

/**
 * A simple provider that evaluates the integral of a single variable function.
 * 
 * @author Michael Alger
 */
public class IntegralImpl extends ServiceProvider implements IntegralRemote {

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
	public IntegralImpl(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle); // calls constructor of SorcerProvider
	}

	/**
	 * This method evaluates a single variable function
	 * 
	 * @param context
	 *            ServiceContext
	 * @return ServiceContext
	 */
	public Context evaluateIntegral(Context context) throws RemoteException {
		try {
			double a = ((CalcContext) context).getAValue();
			double b = ((CalcContext) context).getBValue();
			double n = ((CalcContext) context).getNValue();
			double h = (b - a) / n;
			double x = 0;
			double estimate = 0;
			Function func = (Function) ((CalcContext) context).getFunction();

			// Exertion resultExertion = null;
			// Exertion fnEvalTask = createTask(context);
			// Provider functionEval = ProviderAccessor.getProvider(null,
			// FunctionEvaluatorRemote.class.getName());

			for (x = a; x < b - (h / 2); x += h) {
				// ((CalcContext)context).setXValue(x);
				// resultExertion = functionEval.service(fnEvalTask);
				// estimate = estimate +
				// ((CalcContext)resultExertion.getContext()).getScalarFnResultX();
				estimate = estimate + evaluateFunction(func, x);
			}

			estimate = estimate * h;

			double iteration = ((CalcContext) context).getIterValue() + 1;
			double oldResult = ((CalcContext) context).getIntegralFnResult();

			((CalcContext) context).setOldIntegralFnResult(oldResult);
			((CalcContext) context).setIntegralFnResult(estimate);
			((CalcContext) context).setIterValue(iteration);

			testLog.info("Approximation result: N: " + h + " --- Estimate: "
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

	/**
	 * Returns the scalar value of the function
	 * 
	 * @param func
	 *            Function type
	 * @param value
	 *            double
	 * @return double
	 * @throws Exception
	 */
	public double evaluateFunction(Function func, double value)
			throws Exception {
		return func.evaluate(value);
	}
}
