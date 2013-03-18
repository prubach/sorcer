package sorcer.ex1.requestor.bean;

import java.net.InetAddress;

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.requestor.ExertionRunner;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Signature;
import sorcer.service.Task;
import sorcer.util.Sorcer;

public class WhoIsItBeanRunner1 extends ExertionRunner {

	public Exertion getExertion(String... args) throws ExertionException {
		String ipAddress;
		InetAddress inetAddress = null;
		String providerName = null;
		if (args.length == 2)
			providerName = Sorcer.getSuffixedName(args[1]);
		logger.info("providerName: " + providerName);
		// define requestor data
		Task task = null;
		try {
			inetAddress = InetAddress.getLocalHost();

			ipAddress = inetAddress.getHostAddress();

			Context context = new ServiceContext("Who Is It?");
			context.putValue("requestor/address", ipAddress);

			NetSignature signature = new NetSignature("getHostName",
					sorcer.ex1.WhoIsIt.class, providerName != null ? providerName : null);

			task = new NetTask("Who Is It?", signature, context);
		} catch (Exception e) {
			throw new ExertionException("Failed to create exertion", e);
		}
		return task;
	}

	public void postprocess() {
		logger.info("<<<<<<<<<< Exceptions: \n" + exertion.getThrowables());
		logger.info("<<<<<<<<<< Trace list: \n" + exertion.getControlContext().getTrace());
		logger.info("<<<<<<<<<< Ouput context: \n" + exertion.getContext());
	}
}
