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

package sorcer.falcon.examples.provider.fnEval;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

import sorcer.core.provider.ServiceProvider;
import sorcer.falcon.examples.context.FnContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.Log;

import com.sun.jini.start.LifeCycle;

/**
 * Provider to evaluate functions using Java Mathematical Expression Parser
 * (JEP)
 * 
 * @author Michael Alger
 */
public class FnEvalImpl extends ServiceProvider implements FnEvalRemote {

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
	public FnEvalImpl(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle); // calls constructor of SorcerProvider
	}

	/**
	 * This method evaluates the given function using the user defined Map
	 * reference containing the variable names in the functions to the path in
	 * the context.
	 * 
	 * @param context
	 *            ServiceContext which is the service data
	 * @return context ServiceContext with the modified data nodes by the
	 *         provider
	 * @throws RemoteException
	 * @see Map
	 * @see Djep
	 */
	public Context evaluateFunction(Context context) throws RemoteException {

		FnContext fnContext = (FnContext) context;
		Map mapReference = null;
		Map.Entry entry = null;
		String varName = null;
		String path = null;
		String expression = null;
		Object value = null;
		Iterator iter = null;
		Double result = null;
		Node node2 = null;
		Node processed = null;
		Node simplified = null;
		DJep fnParser = new DJep();

		fnParser.addStandardConstants();
		fnParser.addStandardFunctions();
		fnParser.addComplex();
		fnParser.setAllowUndeclared(true);
		fnParser.setAllowAssignment(true);
		fnParser.setImplicitMul(true);
		// Sets up standard rules for differentiating sin(x) etc.
		fnParser.addStandardDiffRules();

		try {
			mapReference = fnContext.getMapReference(); // retrieve the user
														// defined Map from the
														// context
			expression = fnContext.getFn(); // retrieve the string function
											// expression from the context
			iter = mapReference.entrySet().iterator();

			while (iter.hasNext()) {
				entry = (Map.Entry) iter.next(); // retreive each entry in the
													// Map
				varName = (String) entry.getKey(); // gets the key (variable
													// name)
				path = (String) entry.getValue(); // gets the value (path of the
													// data node respect to the
													// variable name)
				value = context.getValue(path); // retrieve the actually value
												// of the data node respect to
												// the path

				testLog.info("var: " + varName + "\tpath: " + path
						+ "\tvalue: " + value);
				fnParser.addVariable(varName, value); // add the variables to
														// the JEP object
			}

			testLog.info("Evaluating Function: " + expression);
			node2 = fnParser.parse(expression);

			// To actually make diff do its work the equation needs to be
			// preprocessed
			processed = fnParser.preprocess(node2);

			// finally simplify
			simplified = fnParser.simplify(processed);

			// print the simplified node
			// testLog.info("Simplified Function: ");
			// fnParser.println(simplified);

			// evaluate it for the result
			result = (Double) fnParser.evaluate(simplified); // determine the
																// result of the
																// expression
			testLog.info("Result: " + result);

			fnContext.setResult(result); // update the context with the result
		} catch (ContextException ce) {
			logger
					.severe("problem in getting/setting data nodes in SimpleContext"
							+ ce);
			ce.printStackTrace();
		} catch (ParseException pe) {
			logger.severe("problem parsing the expression" + pe);
			pe.printStackTrace();
		}

		return (Context) fnContext; // return the ServiceContext back to the
									// requestor
	}

}
