package sorcer.ex0;

import java.rmi.RemoteException; 
import java.util.logging.Logger;

import sorcer.service.Context;
import sorcer.util.Log;

public class HelloWorldImpl implements HelloWorld {

	private static Logger logger = Log.getTestLog();
	
	public Context sayHelloWorld(Context context) throws RemoteException {
		try {
			logger.info("HelloWorld Provider got a message: " + context);
			String input = (String) context.getValue("in/value");
			logger.info("HelloWorld Input = " + input);
			String output = "Hello there - " + input;
			context.putOutValue("out/value", output);
			logger.info("HelloWorld Provider sent a message" + context);
		} catch (Exception e) {
			logger.severe("HelloWorld Provider - problem interpreting message: " + context);
		}
		return context;		
	}
}
