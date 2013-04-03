package sorcer.ex5.provider;

import java.net.InetAddress;
import java.rmi.RemoteException;

import sorcer.core.dispatch.sla.SlaDispatcher;
import sorcer.core.provider.qos.QosServiceProvider;
import sorcer.core.provider.qos.sla.slaprioritizer.SlaCostTimeModeler;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

import com.sun.jini.start.LifeCycle;

public class Worker1Provider extends QosServiceProvider implements Worker1 {
	private String hostName = InetAddress.getLocalHost().getHostName();
	private String hostAddress = InetAddress.getLocalHost().getHostAddress();
	
	public Worker1Provider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
		SlaDispatcher slaDispatcher = new SlaDispatcher();
		slaDispatcher.init();
		SlaCostTimeModeler slaCostTimeModeler = new SlaCostTimeModeler();
		slaCostTimeModeler.init();
	}

	public Context sayHi(Context context) throws RemoteException,
			ContextException, EvaluationException {
		context.putValue("provider/host/name", hostName);
		String reply = "Hi" + " " + context.getValue("reqestor/name") + "!";
		context.putValue("provider/message", reply);
		return context;
	}

	public Context sayBye(Context context) throws RemoteException,
			ContextException, EvaluationException {
		context.putValue("provider/host/name", hostName);
		String reply = "Bye" + " " + context.getValue("reqestor/name") + "!";
		context.putValue("provider/message", reply);
		return context;
	}

	public Context doIt(Context context) throws InvalidWork, RemoteException,
			ContextException, EvaluationException {
		context.putValue("provider/host/name", hostName);
		int result = (Integer) context.getValue("requestor/operand/1")
				* (Integer) context.getValue("requestor/operand/2");
		context.putValue("provider/result", result);
		String reply = "Multiplication done by: " + hostAddress;
		context.putValue("provider/message", reply);
		
		//simulate longer execution time based on the value in configs/worker-prv.properties
		String sleep = getProperty("provider.sleep.time");
		logger.info("sleep=" + sleep);
		if (sleep != null)
			try {
				Thread.sleep(Integer.parseInt(sleep));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		return context;
	}
}
