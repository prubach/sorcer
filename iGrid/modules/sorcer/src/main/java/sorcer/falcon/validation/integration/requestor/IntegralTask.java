package sorcer.falcon.validation.integration.requestor;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import sorcer.core.Provider;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.validation.base.IntegralRemote;
import sorcer.falcon.validation.context.CalcContext;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Servicer;
import sorcer.util.Log;
import sorcer.util.ProviderAccessor;

/**
 * The regular service request for the Derivative-Evaluater provider
 * 
 * @author Michael Alger
 */

public class IntegralTask {

	private static Logger log = Log.getTestLog(); // logger framework

	// variables name to data nodes

	/**
	 * Main method for testing only
	 */
	public static void main(String[] args) {

		IntegralTask client = new IntegralTask();

		client.executeTask(); // execute the Task
	}

	/**
	 * The default constructor which sets the RMI Security Manager
	 */
	public IntegralTask() {
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
		Servicer integralEval = ProviderAccessor.getProvider(null,
				IntegralRemote.class);

		log.info("Integral-Evaluator proxy: " + integralEval);

		// create a simple RemoteServiceTask
		Exertion task = createTask();

		try {
			// execute the exertion on the provider using the remote proxy
			Exertion resultTask = integralEval.service(task, null);

			// retrieves the service context or service data
			CalcContext resultContext = (CalcContext) resultTask.getContext();

			log.info("Function: x^3");
			log.info("Where: a = " + resultContext.getAValue() + "   b = "
					+ resultContext.getBValue() + "   N = "
					+ resultContext.getNValue());
			log.info("Integral Result: " + resultContext.getIntegralFnResult());
		} catch (ExertionException ee) {
			log.severe("Exertion problem");
			ee.printStackTrace();
		} catch (RemoteException re) {
			log.severe("Remote problem");
			re.printStackTrace();
		} catch (TransactionException te) {
			log.severe("Transaction problem");
			te.printStackTrace();
		} catch (ContextException e) {
			e.printStackTrace();
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
	 * @return RemoteServiceTask The elementary exertion (task) for the
	 *         Derivative-Evaluater
	 * @see CalcContext
	 * @see RemoteServiceSignature
	 * @see NetTask
	 */
	public NetTask createTask() {

		// create the service context of type SimpleContext
		CalcContext context = new CalcContext("Message");
		NetTask task = null;

		// create the service method, arguments follows: method name, interface
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

			context.setAValue(0);
			context.setBValue(1);
			context.setNValue(100);
			context.setXValue(3);
			context.setHValue(0);
			context.setIterValue(0);
			context.setFunction(new FunctionImpl());
			context.setOldDerivativeFnResult(0);
			context.setDerivativeFnResult(1);
			context.setOldIntegralFnResult(0);
			context.setIntegralFnResult(1);
		} catch (ContextException ce) {
			log.severe("Problem adding data nodes into the context: " + ce);
			ce.printStackTrace();
		}

		return task;
	}
}
