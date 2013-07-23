package ${package};

import java.rmi.RemoteException;

import sorcer.service.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ${providerClass} implements ${providerInterface} {

	private static Logger logger = LoggerFactory.getLogger(${providerClass}.class);
	
	public Context sayHelloWorld(Context context) throws RemoteException {
		try {
			logger.info("${providerInterface} Provider got a message: {}", context);
			String input = (String) context.getValue("in/value");
			logger.info("${providerInterface} Input = {}", input);
			String output = "Hello there - " + input;
			context.putOutValue("out/value", output);
			logger.info("${providerInterface} Provider sent a message\n{}", context);
		} catch (Exception e) {
			logger.error("${providerInterface} Provider - problem interpreting message: " + context, e);
		}
		return context;
	}
}
