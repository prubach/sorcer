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

package sorcer.core.context.model;

import java.rmi.RemoteException;
import java.util.List;

import sorcer.service.Parameter;
import sorcer.co.tuple.Tuple2;
import sorcer.core.context.ServiceContext;
import sorcer.core.invoker.ContextInvoking;
import sorcer.core.invoker.ServiceInvoker;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;

/**
 * @author Mike Sobolewski
 */

/**
 * In service-based modeling, a parameter (for short a par) is a special kind of
 * variable, used in a service dataContext to refer to one of the pieces of data
 * provided as input to the invokers (subroutines of the dataContext). These pieces
 * of data are called arguments. A service dataContext, as a map of pairs (Pars),
 * parameter name and its argument <name, argument> is the definition of a
 * independent and dependent arguments. Arguments that dependent on other
 * arguments are subroutines (invokers), so that, each time the subroutine is
 * called, its arguments for that call can be assigned to the corresponding
 * parameters of invokers.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ServiceModel extends ServiceContext<Object> implements Modeling,
		ContextInvoking<Object> {

	protected boolean contextChanged = false;

	public ServiceModel() {
		super();
	}
	
	public ServiceModel(String name) {
		super(name);
	}

	public Object getValue(String path, Parameter... entries)
			throws ContextException {
		try {
			Object val = super.getValue(path, entries);
			if (val != null && val instanceof ServiceInvoker) {
				return ((ServiceInvoker) val).getValue(entries);
			} else
				return val;
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Evaluation#getValue(sorcer.core.dataContext.Path.Entry[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException {
		try {
			Object val = super.getValue(null, entries);
			if (val != null && val instanceof ServiceInvoker) {
				return ((ServiceInvoker) val).getValue(entries);
			} else
				return val;
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public Object getValue(String path, Object defaultValue)
			throws ContextException {
		Object val;
		try {
			val = super.getValue(path);
			if (val != null && val instanceof ServiceInvoker) {
				return ((ServiceInvoker) val).getValue();
			}
		} catch (Exception e) {
			throw new ContextException(e);
		}
		if (val != null)
			return val;
		else
			return defaultValue;
	}

	@Override
	public Object putValue(String path, Object value) throws ContextException {
		if (value instanceof ServiceInvoker) {
			Context ic = ((ServiceInvoker) value).getInvokeContext();
			if (ic == null)
				((ServiceInvoker) value).setInvokeContext(this);
		}
		super.putValue(path, value);
		contextChanged = true;

		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.invoker.Invoking#invoke(sorcer.service.Parameter[])
	 */
	@Override
	public Object invoke(Parameter... entries) throws RemoteException,
			EvaluationException {
		return getValue(entries);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.invoker.Invoking#invoke(sorcer.service.Context,
	 * sorcer.service.Parameter[])
	 */
	@Override
	public Object invoke(Context context, Parameter... entries)
			throws EvaluationException {
		try {
			appendContext(context);
			return invoke(entries);
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public ServiceModel add(Par... parameters) throws EvaluationException,
			RemoteException, ContextException {
		if (parameters != null && parameters.length > 0) {
			for (Par p : parameters)
				putValue(p.getName(), p.getAsis());
		}
		return this;
	}

	public ServiceModel add(List<Par> parameters) throws EvaluationException,
			RemoteException, ContextException {
		if (parameters != null && parameters.size() > 0) {
			for (Par p : parameters)
				putValue(p.getName(), p.getAsis());
		}
		return this;
	}

	public ServiceInvoker add(ServiceInvoker invoker) throws ContextException {
		putValue(invoker.getName(), invoker);
		return invoker;
	}

	@Override
	public Object getAsis(String path) throws ContextException {
		return super.getValue(path);
	}

	public Par<Object> getPar(String name) throws ContextException {
		return new Par<Object>(name, getAsis(name));
	}

	public boolean isContextChanged() {
		return contextChanged;
	}

	public void setContextChanged(boolean contextChanged) {
		this.contextChanged = contextChanged;
	}

	public ServiceContext substitute(Parameter... entries) throws EvaluationException {
		try {
			for (Parameter e : entries) {
				if (e instanceof Tuple2) {
					Object val = null;
					
					if (((Tuple2) e)._2 instanceof Evaluation)
						val = ((Evaluation) ((Tuple2) e)._2).getValue();
					else
						val = ((Tuple2) e)._2;
			
					if (((Tuple2) e)._1 instanceof String) {
							putValue((String) ((Tuple2) e)._1, val);
					}
				} else if (e instanceof Par) {
					putValue(((Par)e).getName(), ((Par)e).getAsis());
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new EvaluationException(ex);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#evaluate(sorcer.service.Context)
	 */
	@Override
	public Context evaluate(Context modelContext) throws EvaluationException,
			RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#configureEvaluation(sorcer.service.Context)
	 */
	@Override
	public Context configureEvaluation(Context modelContext)
			throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#selectEvaluation(sorcer.service.Context)
	 */
	@Override
	public Context selectEvaluation(Context modelContext)
			throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#updateEvaluation(sorcer.service.Context)
	 */
	@Override
	public Context updateEvaluation(Context modelContext)
			throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#getResult()
	 */
	@Override
	public Object getResult() throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.model.Modeling#writeResult()
	 */
	@Override
	public boolean writeResult() throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
}
