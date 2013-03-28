/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
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

package sorcer.core.invoker;

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import sorcer.co.tuple.Parameter;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

/**
 * @author Mike Sobolewski
 */

public class GroovyInvoker extends ServiceInvoker {
		
	private static String defaultName = "gvyIvoker-";
		
	private String name;
	
	// counter for unnamed instances
	protected static int count;

	/** expression to be evaluated */
	protected String expression;

	/** The evaluator */
	transient private GroovyShell shell;

	private File scriptFile = null;
	
	public GroovyInvoker() throws EvaluationException {
		this.name = defaultName + count++;
	}

	public GroovyInvoker(String name) throws EvaluationException {
		if (name == null)
			this.name = defaultName + count++;
		else
			this.name = name;
	}

	public GroovyInvoker(String name, String expression)
	throws EvaluationException {
		this(name, expression, (ServiceInvokerList)null);
	}
	
	public GroovyInvoker(String expression, ServiceInvokerList invokers)
			throws EvaluationException {
		this(defaultName + count++, expression, invokers);
	}

	public GroovyInvoker(File scriptFile, ServiceInvokerList invokers)
			throws EvaluationException {
		this.scriptFile = scriptFile;
		for (ServiceInvoker si : invokers) {
			try {
				variables.putValue(si.getName(), si);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}
	}
	
	public GroovyInvoker(String name, String expression, ServiceInvokerList invokers)
			throws EvaluationException {
		this(name);
		this.expression = expression;
		for (ServiceInvoker si : invokers) {
			try {
				variables.putValue(si.getName(), si);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}
	}
	
	public GroovyInvoker(String expression, ServiceInvoker... invokers)
			throws EvaluationException {
		this.expression = expression;
		for (ServiceInvoker si : invokers) {
			try {
				variables.putValue(si.getName(), si);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		Object result = null;
		try {
			synchronized (shell) {
				if (scriptFile != null)
					try {
						result = shell.evaluate(scriptFile);
					} catch (IOException e) {
						throw new EvaluationException(e);
					}
				else {
					result = shell.evaluate(expression);
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			logger.severe("Error Occurred in Groovy Shell: " + e.getMessage());
			throw new EvaluationException("Groovy Parsing Error: "
					+ e.getMessage());
		}
		return result;
	}
	
	public void clean() {
		shell = null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.invoker.ServiceInvoker#invoke(sorcer.service.Context[])
	 */
	@Override
	public Context invoke(Context context, Parameter... parameters) throws RemoteException,
			EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
