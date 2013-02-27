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

package sorcer.falcon.validation.condition;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.nfunk.jep.JEP;

import sorcer.core.context.ServiceContext;
import sorcer.falcon.base.Conditional;
import sorcer.falcon.core.exertion.IfExertion;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.util.Log;

/**
 * The FnIfConditional is an example of the Condtional component that handles
 * the condition of the IfExertion.
 * 
 * @author Michael Alger
 */
public class IfCondition implements Conditional, Serializable {
	protected Context context;
	protected String expression;
	protected Map mapReference;
	protected final transient static Logger testLog = Log.getTestLog(); // logger
	
	/**
	 * Overloaded constructor which takes in the expression for the condition and
	 * a Map for the variable name and path reference
	 * @param function String	
	 * @param map Map
	 */
	public IfCondition(String expression, Map map) {
		this.expression = expression;
		this.mapReference = map;
	}

    /**
     * Returns true if the condition is true
     * @see sorcer.falcon.base.Conditional#isTrue()
     */
    public boolean isTrue() throws ExertionException {
    	double result = evalCondition();

		if (result == 0 || result == 1.0) {
			testLog.finest("isTrue(): " + (result == 1.0));
			return (result == 1.0);
		} else {
			throw new ExertionException(
					"Boolean expression is not a valid condition "
							+ IfExertion.class.getClass().getName());
		}
    }
    
    /**
     * Sets the ServiceContext of the condition for the new reference 
     * to be evaluated
     * @param context ServiceContext
     * @see ServiceContext
     */
    public void setConditionalContext(Context context) {
    	this.context = context;
    }
    
	/**
	 * Evaluates the string expression from the given variables that corolates
	 * to a path, which then points to the actual data nodes in the context
	 * 
	 * @param dispatchSearchContext
	 *            ServiceContext where the data nodes will be evaluated from
	 * @return Object result from the expression
	 */
	protected double evalCondition() {
		JEP jepParser = new JEP();
		Iterator iter = mapReference.entrySet().iterator();
		double result = 0;

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String varName = (String) entry.getKey();
			if (!varName.startsWith("in/") && (varName.indexOf("/") == -1)) {
				String path = (String) entry.getValue();
				Object value = null;
				try {
					value = context.getValue(path);
				} catch (ContextException e) {
					testLog.finest("evalCondition ContextException " + e);
					e.printStackTrace();
				}

				testLog.finest("***Condition Variable: " + varName
						+ "  \tpath: " + path + "  \tvalue: " + value);
				jepParser.addVariable(varName, value);
			}
		}

		testLog.finest("Evaluating Condition: (" + expression + ")");
		jepParser.parseExpression(expression);
		result = jepParser.getValue();
		jepParser = null;
		return result;
	}	
	
	/**
	 * Returns the Map reference of variable names and their associated context path
	 * @return Map
	 * @see Map
	 */
	public Map getReferencingMap() {
		return mapReference;
	}
	
	/**
	 * Return condition Expression
	 * @return String
	 */
	public String getExpression() {
		return expression;
	}
	
}
