package sorcer.ex2.provider;

import java.net.InetAddress;
import java.rmi.RemoteException;

import sorcer.core.provider.ServiceTasker;
import sorcer.service.Context;
import sorcer.service.ContextException;

import com.sun.jini.start.LifeCycle;

public class WorkerProvider extends ServiceTasker implements Worker {
	
	private String hostName;
	
	public WorkerProvider() throws Exception {
		hostName = InetAddress.getLocalHost().getHostName();
	}
	
	public WorkerProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
		hostName = InetAddress.getLocalHost().getHostName();
	}

	public Context sayHi(Context context) throws RemoteException,
			ContextException {
		context.putValue("provider/host/name", hostName);
		String reply = "Hi" + " " + context.getValue("requestor/name") + "!";
		setMessage(context, reply);
		return context;
	}

	public Context sayBye(Context context) throws RemoteException,
			ContextException {
		context.putValue("provider/host/name", hostName);
		String reply = "Bye" + " " + context.getValue("requestor/name") + "!";
		setMessage(context, reply);
		return context;
	}

	public Context doWork(Context context) throws InvalidWork, RemoteException,
			ContextException {		
		context.putValue("provider/host/name", hostName);
		int result = (Integer) context.getValue("requestor/operand/1")
				* (Integer) context.getValue("requestor/operand/2");
		context.putValue("provider/result", result);
		String reply = "Done work: " + result;
		setMessage(context, reply);

		// simulate longer execution time based on the value in
		// configs/worker-prv.properties
		String sleep = getProperty("provider.sleep.time");
		logger.info("sleep=" + sleep);
		if (sleep != null)
			try {
				context.putValue("provider/slept/ms", sleep);
				Thread.sleep(Integer.parseInt(sleep));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		return context;
	}
	
	private String setMessage(Context context, String reply)
			throws ContextException {
		String previous = (String) context.getValue("provider/message");
		String message = "";
		if (previous != null && previous.length() > 0)
			message = previous + "; " + reply;
		else
			message = reply;
		context.putValue("provider/message", message);
		return message;
	}
	
}
