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

package sorcer.falcon.validation.initializeCondition.provider;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.logging.Logger;

import sorcer.service.ContextException;
import sorcer.service.Context;
import sorcer.core.provider.ServiceProvider;
import sorcer.falcon.validation.base.InitializeConditionRemote;
import sorcer.util.Log;

import com.sun.jini.start.LifeCycle;

/**
 * A simple provider that evaluates the scalar value of a function.
 * 
 * @author Michael Alger
 */
public class InitializeConditionImpl extends ServiceProvider implements InitializeConditionRemote {
	
	final static Logger testLog = Log.getTestLog(); //logger for testing

	/**
	 * This constructor is needed for the Jini Extensible Remote Invocation (JERI)
     * 
	 * @param args Array of Strings
	 * @param lifeCycle LifeCycle jini 2.x specific
	 * @throws RemoteException
	 */
	public InitializeConditionImpl(String [] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle); //calls constructor of SorcerProvider
	}
	
	/**
	 * Service Method that randomize
	 * @param context ServiceContext
	 * @return ServiceContext
	 */
	public Context initializeCondition(Context context) throws RemoteException {
		
		try {
			int value = randomize();
			context.putOutValue("out/value/result", value);
			testLog.info("Condition value: " + value);
		}
		catch (ContextException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return context;
	}
	
	/**
	 * Creates a random selection between integration and differentiation
	 * @return String the greeting for the requestor
     * @see Random
	 */
	private int randomize() {
		Random randomGen = new Random();
		return randomGen.nextInt(10);
	}
}
	
	