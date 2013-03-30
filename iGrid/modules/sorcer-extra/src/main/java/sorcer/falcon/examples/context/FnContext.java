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

package sorcer.falcon.examples.context;

import java.util.Map;

import sorcer.core.SorcerConstants;
import sorcer.core.context.ServiceContext;
import sorcer.service.ContextException;

/**
 * Example of a class with user defined API methods for the ServiceContext.
 * Simply subclass from the ServiceContextImpl and set the path as member with
 * the appropriate methods for set and get.
 * 
 * @author Michael Alger
 */
public class FnContext extends ServiceContext implements SorcerConstants {

	private static final long serialVersionUID = 1L;
	protected final String IN = "in" + CPS + "value" + CPS;
	protected final String OUT = "out" + CPS + "value" + CPS;
	protected final String FN = IN + "function";
	protected final String RESULT = OUT + "functionResult";
	protected final String MAP = IN + "mapping";

	/**
	 * Default constructor
	 */
	public FnContext() {
		super();
	}

	/**
	 * Overloaded constructor with context name parameter
	 * 
	 * @param name
	 *            String
	 */
	public FnContext(String contextName) {
		super(contextName);
	}

	/**
	 * Overloaded constructor with context name and root name parameters
	 * 
	 * @param contextName
	 *            String
	 * @param rootName
	 *            String
	 */
	public FnContext(String contextName, String rootName) {
		super(contextName, rootName);
	}

	/**
	 * Returns the function
	 * 
	 * @return String
	 */
	public String getFn() throws ContextException {
		return (String) this.get(FN);
	}

	/**
	 * Sets the function to be evaluated in the context
	 * 
	 * @param fn
	 *            String, the function equation
	 */
	public void setFn(String fn) throws ContextException {
		this.putValue(FN, fn);
	}

	/**
	 * Returns the context path of the function equation data node
	 * 
	 * @return String
	 */
	public String getFnPath() throws ContextException {
		return FN;
	}

	/**
	 * Returns the result of the equation
	 * 
	 * @return Object
	 */
	public Double getResult() throws ContextException {
		return (Double) this.get(RESULT);
	}

	/**
	 * Sets the result of the function evaluation
	 * 
	 * @param result
	 */
	public void setResult(Double result) throws ContextException {
		this.putValue(RESULT, result);
	}

	/**
	 * Returns the path of the result data node
	 * 
	 * @return String
	 */
	public String getResultPath() throws ContextException {
		return RESULT;
	}

	/**
	 * Returns the variable data node from the context
	 * 
	 * @return Double
	 */
	public Map getMapReference() throws ContextException {
		return (Map) this.get(MAP);
	}

	/**
	 * Sets the variable value in the context
	 * 
	 * @param var
	 */
	public void setMapReference(Map map) throws ContextException {
		this.putValue(MAP, map);
	}

	/**
	 * Returns the path of the variable data node
	 * 
	 * @return String
	 */
	public String getMapReferencePath() throws ContextException {
		return MAP;
	}
}
