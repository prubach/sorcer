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

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import sorcer.co.tuple.Entry;
import sorcer.co.tuple.Parameter;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.Par;
import sorcer.core.context.model.ServiceModel;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.util.observable.Observable;
import sorcer.util.observable.Observer;

/**
 * @author Mike Sobolewski
 */

/**
 * The ServiceInvoker defines context driven invocations on a invoke context
 * (service context) containing its parameter names (paths) and arguments
 * (values). The requested invocation is specified by the own invoke context and
 * eventual context of parameters (Par).
 * 
 * The semantics for how parameters can be declared and how the arguments get
 * passed to the parameters of callable unit are defined by the language, but
 * the details of how this is represented in any particular computing system
 * depend on the calling conventions of that system. A context-driven computing
 * system defines callable unit called invokers used within a scope of service
 * contexts, data structures defined in SORCER.
 * 
 * An invoke context is dictionary (associative array) composed of a collection
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
public abstract class ServiceInvoker<T> extends Observable implements Invoking<T>, Observer {

	//the cached value
	protected T value;
		
	// indication that value has been calculated with recent arguments
	private boolean valueIsValid = false;
		
	
	protected ServiceModel invokeContext;
	
	// set of dependent variables for this evaluator
	protected ParSet pars = new ParSet();

	/** Logger for logging information about instances of this type */
	static final Logger logger = Logger.getLogger(ServiceInvoker.class
			.getName());

	/**
	 * <p>
	 * Returns a set of parameters (pars) of this invoker that are a a subset of
	 * parameters of its invokeContext.
	 * </p>
	 * 
	 * @return the pars of this invoker
	 */
	public ParSet getPars() {
		return pars;
	}

	/**
	 * <p>
	 * Assigns a set of parameters (pars) for this invoker. 
	 * </p>
	 * 
	 * @param pars
	 *            the pars to set
	 */
	public void setPars(ParSet pars) {
		this.pars = pars;
	}

	/**
	 * <p>
	 * Return the current valid value
	 * </p>
	 * 
	 * @return the valid value
	 * @throws EvaluationException 
	 * @throws RemoteException 
	 */
	@Override
	public T getValue(Parameter... entries) throws EvaluationException, RemoteException {
		if (entries != null & entries.length > 0)
			valueIsValid = false;
		
		if (invokeContext.isContextChanged()) {
			valueIsValid = false;
			pars.clearPars();
		}
		
		if (valueIsValid)
			return value;
		else {
			value = invoke();
			valueChanged();
			valueValid(true);
		}
		invokeContext.substitute(entries);
		return value;
	}

	protected void valueValid(boolean state) {
		valueIsValid = state;
	}
	
	public void valueChanged() throws EvaluationException {
		setChanged();
		try {
			notifyObservers(this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new EvaluationException(e.toString());
		}
	}
	
	@Override 
	public void update(Observable observable, Object obj) throws EvaluationException, RemoteException {
		// one of my dependent pars changed
		// the 'observable' is the dependent invoker that has changed as indicated by 'obj'
		// ignore updates from itself
		valueValid(false);
		
		// set value to null so getValueAsIs returns null
		value = null;
		setChanged();
		notifyObservers(this);
	}
	
	/**
	 * Adds a new par to the invoker. This must be done before calling
	 * {@link #invoke} so the invoker is aware that the new par may be added to
	 * the model.
	 * 
	 * @param name
	 *            Name of the variable to be added
	 * @param value
	 *            Initial value or new value for the variable
	 * @throws RemoteException
	 * @throws EvaluationException
	 * @throws VarException
	 */
	public ServiceInvoker addPar(Par par) throws EvaluationException,
			RemoteException {
		if (par.getAsis() instanceof ServiceInvoker) {
			((ServiceInvoker) par.getValue()).addObserver(this);
			pars.add(par);
			value = null;
			setChanged();
			notifyObservers(this);
			valueValid(false);
		}
		return this;
	}

	synchronized public void addPars(ParSet parSet) throws EvaluationException,
			RemoteException {
		for (Par p : parSet) {
			addPar(p);
		}
	}
	
	synchronized public void addPars(List<Par> parList)
			throws EvaluationException, RemoteException {
		for (Par p : parList) {
			addPar(p);
		}
	}
	
	synchronized public void addPars(Par... pars) throws EvaluationException,
			RemoteException {
		for (Par p : pars) {
			addPar(p);
		}
	}

	synchronized public void addPars(ParList args) throws EvaluationException,
			RemoteException {
		if (args != null)
			for (Par p : args) {
				addPar(p);
			}
	}

	synchronized public void addPars(ParList... parLists)
			throws EvaluationException, RemoteException {
		for (ParList pl : parLists) {
			addPars(pl);
		}
	}
	
	public T invoke(Context context, Parameter... entries) throws RemoteException,
			EvaluationException {
		invokeContext = (ServiceModel)context;
		return invoke(entries);
		
	}

	public T invoke(Parameter... entries) throws RemoteException,
		EvaluationException {
		if (entries != null && entries.length > 0)
			substitute( entries);
		return  getValue(entries);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public T getAsis() throws EvaluationException, RemoteException {
		return getValue();
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
					invokeContext.putValue(((Entry<String, Object>) e)._1,
							((Entry<String, Object>) e)._2);
				} catch (ContextException ex) {
					throw new EvaluationException(ex);
				}
			}

		}
		return this;
	}

	public Context getInvokeContext() {
		return invokeContext;
	}

	public void setInvokeContext(ServiceModel invokeContext) {
		this.invokeContext = (ServiceModel)invokeContext;
	}

	public void clearPars() {
		for (Par p : pars) {
			p.setValue(null);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + name;
	}
}
