package com.example.sorcer;

import java.rmi.RemoteException; 
import java.util.logging.Logger;

import sorcer.service.Context;
import sorcer.util.Log;

public class SampleServiceImpl implements SampleService {

	private static Logger logger = Log.getTestLog();
	
	public Context sayHelloWorld(Context context) throws RemoteException {
		try {
			logger.info("SampleService Provider got a message: " + context);
			String input = (String) context.getValue("in/value");
			logger.info("SampleService Input = " + input);
			String output = "Hello there - " + input;
			context.putOutValue("out/value", output);
			logger.info("SampleService Provider sent a message" + context);
		} catch (Exception e) {
			logger.severe("SampleService Provider - problem interpreting message: " + context);
		}
		return context;		
	}
}
