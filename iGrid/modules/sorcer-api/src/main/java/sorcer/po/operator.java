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

import static sorcer.po.operator.pars;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.co.tuple.Parameter;
import sorcer.core.context.model.Par;
import sorcer.core.context.model.ServiceModel;
import sorcer.core.invoker.GroovyInvoker;
import sorcer.core.invoker.ParSet;
import sorcer.core.invoker.ServiceInvoker;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class operator {

	private static int count = 0;

	private static final Logger logger = Logger.getLogger(operator.class.getName());

	public static Par par(String name, Object argumnet) {
		return new Par(name, argumnet);
	}

	public static Par par(ServiceModel pm, String name) throws ContextException {
		Par parameter = new Par(name, pm.getAsis(name));
		parameter.setScope(pm);
		return parameter;
	}
	
	public static ServiceModel model(Par... parameters)
			throws EvaluationException, RemoteException, ContextException {
		ServiceModel pm = new ServiceModel();
		pm.add(parameters);
		return pm;
	}
	
	public static Object value(ServiceModel model, String parName,
			Parameter... parameters) throws ContextException {
		return model.getValue(parName, parameters);
	}
	
	public static Object value(Par par) throws EvaluationException,
			RemoteException {
		return par.getValue();
	}
	
	public static Object asis(Par par) throws EvaluationException,
			RemoteException {
		return par.getAsis();
	}
	
	public static Object asis(ServiceModel model, String name)
			throws ContextException, RemoteException {
		return model.getAsis(name);
	}
	
	public static void clearPars(Object invoker) {
		if (invoker instanceof ServiceInvoker)
			((ServiceInvoker)invoker).clearPars();
	}
	
	public static Object value(ServiceModel model, Parameter... parameters)
			throws ContextException, RemoteException {
		return model.getValue(parameters);
	}
	
	public static ServiceModel result(ServiceModel model, String parname)
			throws ContextException, RemoteException {
		model.setReturnPath(parname);
		return model;
	}

	public static void add(ServiceModel parModel, Par... parameters)
			throws RemoteException, ContextException {
		for (int i = 0; i < parameters.length; i++) {
			Par par = parameters[i];
			parModel.add(parameters);
		}
	}
	
	public static void put(ServiceModel parModel, Par... parameters)
			throws RemoteException, ContextException {
		for (int i = 0; i < parameters.length; i++) {
			Par par = parameters[i];
			parModel.putValue(par.getName(), par.getAsis());
		}
	}

	public static Par put(ServiceModel parModel, String name, Object value) throws ContextException {
		parModel.putValue(name, value);
		return par(parModel, name);
	}
	
	public static Par set(Par par, Object value)
			throws ContextException {
		par.setValue(value);
		if (par.getScope() != null) {
			par.getScope().putValue(par.getName(), value);
		}
		return par;
	}
	
	public static ParSet pars(Object invoker) {
		if (invoker instanceof ServiceInvoker)
			return ((ServiceInvoker) invoker).getPars();
		else
			return null;
	}
	
	public static ParSet pars(String... parnames) {
		ParSet ps = new ParSet();
		for (String name : parnames) {
			ps.add(new Par(name));
		}
		return ps;
	}
	
	public static GroovyInvoker groovy(String expression, ParSet pars)
			throws EvaluationException {
		return new GroovyInvoker(expression, pars);
	}
}