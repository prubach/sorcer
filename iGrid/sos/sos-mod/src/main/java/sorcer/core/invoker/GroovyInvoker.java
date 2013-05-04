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
import java.util.Iterator;

import sorcer.service.Parameter;
import sorcer.core.context.model.Par;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

/**
 * @author Mike Sobolewski
 */

public class GroovyInvoker extends ServiceInvoker {
		
	private static String defaultName = "gvyIvoker-";
			
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

	public GroovyInvoker(String expression) throws EvaluationException {
		this.name = defaultName + count++;
		this.expression = expression;
	}
	
	public GroovyInvoker(String expression, ParSet parameters) throws EvaluationException {
		this.name = defaultName + count++;
		this.expression = expression;
		this.pars = parameters;
	}
	
	public GroovyInvoker(String expression, Par... parameters) throws EvaluationException {
		this.name = defaultName + count++;
		this.expression = expression;
		this.pars = new ParSet(parameters);
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
				invokeContext.putValue(si.getName(), si);
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
				invokeContext.putValue(si.getName(), si);
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
				invokeContext.putValue(si.getName(), si);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		}
	}
	
	public Object invoke(Parameter... entries) throws EvaluationException,
			RemoteException {
		Object result = null;
		shell = new GroovyShell();
		try {
			initBindings();
		} catch (ContextException ex) {
			throw new EvaluationException(ex);
		}
		try {
			synchronized (shell) {
				if (scriptFile != null) {
					try {
						result = shell.evaluate(scriptFile);
					} catch (IOException e) {
						throw new EvaluationException(e);
					}
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
	
	private void initBindings() throws RemoteException, ContextException {
		if (invokeContext != null) {
			if (pars != null && pars.size() > 0) {
				for (Par p : pars) {
					if (p.getValue() == null) {
						p.setValue(invokeContext.getValue(p.getName()));
					} else {
						invokeContext.putValue(p.getName(), p.getAsis());
					}
				}
			}
		}
		Iterator<Par> i = pars.iterator();
		Object val = null;
		String key = null;
		while (i.hasNext()) {
			Par entry = i.next();
			val = entry.getValue();
			key = (String) entry.getName();
			if (val instanceof Invoking) {
				val = ((Invoking) val).getValue();
			}
			shell.setVariable(key, val);
		}
	}
	
	public void clean() {
		shell = null;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + name + ":" + expression;
	}

}




