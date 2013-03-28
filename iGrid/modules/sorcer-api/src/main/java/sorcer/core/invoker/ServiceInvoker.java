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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.co.tuple.Entry;
import sorcer.co.tuple.Parameter;
import sorcer.core.context.InvokeContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Identity;

/**
 * @author Mike Sobolewski
 */

/**
 * The invoker interface defines context driven invocations on a service context
 * containing its parameters (paths) and arguments (values). The requested
 * invocation is specified by the array of contexts.
 * 
 * The semantics for how parameters can be declared and how the arguments get
 * passed to the parameters of callable unit are defined by the language, but
 * the details of how this is represented in any particular computing system
 * depend on the calling conventions of that system. A context-driven computing
 * system defines callable unit called invokers used within a scope of service
 * contexts, data structures defined in SORCER.
 * 
 * A service context is dictionary (associative array) composed of a collection
 * of (key, value) pairs, such that each possible key appears at most once in
 * the collection. Keys are considered as parameters and values as arguments of
 * the service invokers accepting service contexts as their input data. A key is
 * expressed by a path of attributes like directories in paths of a file system.
 * Paths define a namespace of the context parameters. A context argument is any
 * object referenced by its path or returned by a context invoker referenced by
 * its path inside the context. An ordered list of parameters is usually
 * included in the definition of an invoker, so that, each time the invoker is
 * called, the context arguments for that call can be assigned to the
 * corresponding parameters of the invoker. The context values for all paths
 * inside the context are defined explicitly by corresponding objects or
 * calculated by corresponding invokers. Thus, requesting a value for a path in
 * a context is a computation defined by a invoker composition within the scope
 * of the context.
 */
@SuppressWarnings("rawtypes")
public abstract class ServiceInvoker extends Identity implements Evaluation,
		Serializable {

	protected InvokeContext variables;

	protected Context invokeContext;
	
	/** Logger for logging information about instances of this type */
	static final Logger logger = Logger.getLogger(ServiceInvoker.class
			.getName());

	public abstract Context invoke(Context context, Parameter... entries) throws RemoteException,
			EvaluationException;

	public Context invoke(Parameter... entries) throws RemoteException,
		EvaluationException {
		if (entries != null && entries.length > 0)
			substitute( entries);
		return  invoke();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public Object getAsis() throws EvaluationException, RemoteException {
		return getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		substitute(entries);
		return invoke(entries);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#substitute(sorcer.co.tuple.Parameter[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Evaluation substitute(Parameter... entries)
			throws EvaluationException, RemoteException {
		for (Parameter e : entries) {
			if (e instanceof Entry<?, ?>) {
				try {
					variables.putValue(((Entry<String, Object>) e)._1,
							((Entry<String, Object>) e)._2);
				} catch (ContextException ex) {
					throw new EvaluationException(ex);
				}
			}

		}
		return this;
	}
}
