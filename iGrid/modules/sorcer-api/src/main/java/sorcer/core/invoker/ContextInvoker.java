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

/**
 * @author Mike Sobolewski
 */

import java.rmi.RemoteException;
import java.util.Enumeration;

import sorcer.co.tuple.Parameter;
import sorcer.core.context.InvokeContext;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

@SuppressWarnings("rawtypes")
public class ContextInvoker extends ServiceInvoker {

	public ContextInvoker(InvokeContext variables) {
		this.variables = variables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.invoker.ServiceInvoker#invoke(sorcer.service.Context[])
	 */
	@Override
	public Context invoke(Context context, Parameter... parameters)
			throws RemoteException, EvaluationException {
		Context ouContext = new ServiceContext("context/invoker: " + name);
		ouContext.setSubject("result/from/context", variables.getName());
		Enumeration e;
		try {
			e = context.contextPaths();
			String key;
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				ouContext.putValue(key, variables.getValue());
			}
		} catch (ContextException ex) {
			throw new EvaluationException(ex);
		}
		return ouContext;
	}

}
