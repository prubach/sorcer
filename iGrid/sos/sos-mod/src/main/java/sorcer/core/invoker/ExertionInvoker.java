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

import net.jini.core.transaction.Transaction;
import sorcer.co.tuple.Entry;
import sorcer.co.tuple.Parameter;
import sorcer.core.context.model.ServiceModel;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Task;

/**
 * @author Mike Sobolewski
 */

@SuppressWarnings("rawtypes")
public class ExertionInvoker extends ServiceInvoker {

	private static final long serialVersionUID = -1372850079238067252L;
	private static String defaultName = "xrtInvoker-";
	private static int count = 0;
	private Exertion exertion;
	private Exertion evaluatedExertion;
	private Transaction txn;
	private Object updatedValue;

	public ExertionInvoker(String name, Exertion exertion) {
		this.name = name;
		this.exertion = exertion;
	}

	public ExertionInvoker(Exertion exertion) {
		name = defaultName + count++;
		this.exertion = exertion;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		try {
			logger.info("ExertionEvaluator.evaluate(): name = " + name 
					+ "; starting, calling exert(); exertion = " + exertion);
			evaluatedExertion = exertion.exert(txn);
			return ((ServiceExertion)evaluatedExertion).getReturnValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new EvaluationException(e);
		}
	}

	public Exertion getExertion() {
		return exertion;
	}

	public Exertion getEvaluatedExertion() {
		return evaluatedExertion;
	}

	public void substitute(Entry... entries) throws EvaluationException,
			RemoteException {
		((ServiceExertion)exertion).substitute(entries);
	}

	public Object getUpdatedValue() {
		return updatedValue;
	}

	public void setUpdatedValue(Object updatedValue) {
		this.updatedValue = updatedValue;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.invoker.ServiceInvoker#invoke(sorcer.service.Context[])
	 */
	@Override
	public Context invoke(Context context, Parameter... parameters) throws RemoteException,
			EvaluationException {
		invokeContext = (ServiceModel)context;
		Object result = getValue(parameters);
		if (result != null)
			try {
				return new ContextResult(result, evaluatedExertion);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		if (evaluatedExertion instanceof Task)
			return evaluatedExertion.getContext();
		else if (evaluatedExertion instanceof Job) 
			return ((Job)evaluatedExertion).getJobContext();
		return null;
	}
}
