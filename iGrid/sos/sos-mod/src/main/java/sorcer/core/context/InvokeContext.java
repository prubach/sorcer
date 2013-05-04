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

package sorcer.core.context;

import java.rmi.RemoteException;

import sorcer.service.Parameter;
import sorcer.core.invoker.ServiceInvoker;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

/**
 * @author Mike Sobolewski
 *
 */
@SuppressWarnings({ "serial", "unchecked" })
public class InvokeContext extends ServiceContext<Object> {

	
	public Object getValue(String path, Parameter... entries)
			throws ContextException {
		try {
			 Object val = super.getValue(path, entries);
			 if (val != null && val instanceof ServiceInvoker) {
				 return ((ServiceInvoker)val).invoke(entries);
			 }
			 else return val;
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
	public Object getValue(Parameter... entries) throws EvaluationException, RemoteException {
		try {
			 Object val = super.getValue(null, entries);
			 if (val != null && val instanceof ServiceInvoker) {
				 return ((ServiceInvoker)val).invoke(entries);
			 }
			 else return val;
		} catch (ContextException e) {
			throw new EvaluationException(e);
		}
	}

	public Object getValue(String path, Object defaultValue)
			throws ContextException {
		Object obj;
		try {
			obj = getValue(path);
		} catch (Exception e) {
			throw new ContextException(e);
		}
		if (obj != null)
			return obj;
		else
			return defaultValue;
	}
}
