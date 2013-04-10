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
package sorcer.po;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.co.tuple.Parameter;
import sorcer.core.context.model.Par;
import sorcer.core.context.model.ParModel;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class operator {

	private static int count = 0;

	private static final Logger logger = Logger.getLogger(operator.class.getName());

	public static Par par(String name, Object argumnet) {
		return new Par(name, argumnet);
	}

	public static ParModel parModel(Par... parameters)
			throws EvaluationException, RemoteException, ContextException {
		ParModel pm = new ParModel();
		pm.add(parameters);
		return pm;
	}
	
	public static Object value(ParModel model, String parName,
			Parameter... parameters) throws ContextException {
		return model.getValue(parName, parameters);
	}
	
	public static Object value(ParModel model, Parameter... parameters)
			throws ContextException, RemoteException {
		return model.invoke(parameters);
	}
	
	public static ParModel result(ParModel model, String parname)
			throws ContextException, RemoteException {
		model.setReturnPath(parname);
		return model;
	}
}