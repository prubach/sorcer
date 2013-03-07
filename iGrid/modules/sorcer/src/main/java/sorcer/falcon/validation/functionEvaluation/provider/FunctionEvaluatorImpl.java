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

package sorcer.falcon.validation.functionEvaluation.provider;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.core.provider.ServiceProvider;
import sorcer.falcon.validation.base.Function;
import sorcer.falcon.validation.base.FunctionEvaluatorRemote;
import sorcer.falcon.validation.context.CalcContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.Log;

import com.sun.jini.start.LifeCycle;

/**
 * A simple provider that evaluates the scalar value of a function.
 * 
 * @author Michael Alger
 */
public class FunctionEvaluatorImpl extends ServiceProvider implements FunctionEvaluatorRemote {
	
	final static Logger testLog = Log.getTestLog(); //logger for testing

	/**
	 * This constructor is needed for the Jini Extensible Remote Invocation (JERI)
     * 
	 * @param args Array of Strings
	 * @param lifeCycle LifeCycle jini 2.x specific
	 * @throws RemoteException
	 */
	public FunctionEvaluatorImpl(String [] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle); //calls constructor of SorcerProvider
	}
	
	/**
	 * Service Method that evaluates the function in the Service Context.
	 * @param context ServiceContext
	 * @return ServiceContext
	 */
	public Context evaluateFunction(Context context) throws RemoteException {
		
		try {
			Function func = (Function) ((CalcContext)context).getFunction();
			
			double x = ((CalcContext)context).getXValue();
			double h = ((CalcContext)context).getHValue();
			
			double resultXH = evalFunction(func, x + (1/h));
			double resultX = evalFunction(func, x);
			
			((CalcContext)context).setScalarFnResultXH(resultXH);
			((CalcContext)context).setScalarFnResultX(resultX);
			
			testLog.info("function result (x+h): " + resultXH);
			testLog.info("function result (x): " + resultX);
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
	 * Returns the scalar value of the function
	 * @param func Function type
	 * @param value double
	 * @return	double
	 * @throws Exception
	 */
	public double evalFunction(Function func, double value) throws Exception {
		return func.evaluate(value);
	}
}
	
	