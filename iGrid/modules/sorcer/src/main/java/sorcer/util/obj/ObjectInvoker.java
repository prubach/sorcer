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

package sorcer.util.obj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;
import sorcer.util.SorcerUtil;

public class ObjectInvoker {

	//static final long serialVersionUID = 6802147418392854533L;

	final protected static Logger logger = Logger.getLogger(ObjectInvoker.class.getName());

	protected String className;

	private String selector;

	private Class<?>[] paramTypes;

	private Object[] params;

	private Context context;

	transient private Method m;

	private URLClassLoader meLoader;

	private URL[] exportURL;

	// the object used in the constructor of the target object
	private Object initObject;

	protected Object target;

	//the cached value
	protected Object value;
		
	// name of this invoker
	protected String name;
		
	protected static int count;
	
	public ObjectInvoker() {
		name = "oi-" + count++;
	}

	public ObjectInvoker(String name) {
		if (name != null)
			this.name = name;
		else
			this.name = "oe-" + count++;
	}

	public ObjectInvoker(String name, String methodName) {
		this(name);
		selector = methodName;
	}

	public ObjectInvoker(Object target) {
		this.target = target;
	}

	public ObjectInvoker(Object target, String methodName) {
		this(null, target, methodName);
	}

	public ObjectInvoker(String name, Object target, String methodName) {
		this(name);
		this.target = target;
		selector = methodName;
	}

	public ObjectInvoker(String name, String className, String methodName) {
		this(name, className, methodName, null, null);
	}

	public ObjectInvoker(String name, String className, String methodName,
			Class<?>[] signature) {
		this(name, className, methodName, signature, null);
	}

	public ObjectInvoker(String name, String className, String methodName,
			Class<?>[] paramTypes, String distributionParameter) {
		this(name);
		this.className = className;
		selector = methodName;
		this.paramTypes = paramTypes;
		if (distributionParameter != null)
			params = new Object[] { distributionParameter };
	}

	public ObjectInvoker(String name, URL exportUrl, String className,
			String methodName) {
		this(name);
		this.className = className;
		this.exportURL = new URL[] { exportUrl };
		selector = methodName;
	}

	public ObjectInvoker(String name, URL exportUrl, String className,
			String methodName, Object initObject) {
		this(name, exportUrl, className, methodName);
		this.initObject = initObject;
	}

	public ObjectInvoker(URL[] exportURL, String className, String methodName) {
		this.className = className;
		this.exportURL = exportURL;
		selector = methodName;
	}

	public ObjectInvoker(URL[] exportURL, String className,
			String methodName, Object initObject) {
		this(exportURL, className, methodName);
		this.initObject = initObject;
	}

	public void setArgs(String methodName, Class<?>[] paramTypes,
			Object[] parameters) {
		selector = methodName;
		this.paramTypes = paramTypes;
		params = parameters;
	}

	public void setArgs(Class<?>[] paramTypes, Object[] parameters) {
		this.paramTypes = paramTypes;
		this.params = parameters;
	}

	public void setArgs(Object[] parameters) {
		params = parameters;
	}

	public void setSignatureTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public void setSelector(String methodName) {
		selector = methodName;
	}

	public Object invoke() throws EvaluationException {
		Object[] parameters = getParameters();
		Object val = null;
		Class<?> evalClass = null;

		try {
			if (target == null) {
				if (exportURL != null) {
					target = getInstance();
				} else if (className != null) {
					evalClass = Class.forName(className);
				} else {
					Constructor<?> constructor;
					if (initObject != null) {
						constructor = evalClass
								.getConstructor(new Class[] { Object.class });
						target = constructor
								.newInstance(new Object[] { initObject });
					} else
						target = evalClass.newInstance();
				}
			} else {
				evalClass = target.getClass();
			}
			// if no paramTypes defined assume that the method name 'selector'
			// is unique
			if (paramTypes == null) {
				Method[] mts = evalClass.getDeclaredMethods();
				for (Method mt : mts) {
					if (mt.getName().equals(selector)) {
						m = mt;
						break;
					}
				}
			} else {
				if (selector == null) {
					Method[] mts = evalClass.getDeclaredMethods();
					if (mts.length == 1)
						m = mts[0];
				} else {
					Method[] mts = evalClass.getMethods();
					if (Context.class.isAssignableFrom(paramTypes[0])) {
						for (Method mt : mts) {
							if (mt.getName().equals(selector)) {
								m = mt;
								break;
							}
						}
					}
					else {
						m = evalClass.getMethod(selector, paramTypes);
					}
				}
			}
			// ((ServiceContext)context).setCurrentSelector(selector);			
			val = m.invoke(target, parameters);
		} catch (Exception e) {
			logger.severe("**error in object invoker; target = " + target);
			System.out.println("class: " + evalClass);
			System.out.println("method: " + m);
			System.out.println("selector: " + selector);
			System.out.println("paramTypes: " + (paramTypes == null 
					? "null" : SorcerUtil.arrayToString(paramTypes)));
			e.printStackTrace();
			throw new EvaluationException(e);
		}
		// valueChanged = false;
		value = val;
		return value;
	}

	private Class<?>[] getParameterTypes() {
		return paramTypes;
	}

	@SuppressWarnings("unchecked")
	private Object getInstance() {
		Object instanceObj = null;
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			meLoader = URLClassLoader.newInstance(exportURL, cl);
			final Thread currentThread = Thread.currentThread();
			final ClassLoader parentLoader = (ClassLoader) AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return (currentThread.getContextClassLoader());
						}
					});

			try {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						currentThread.setContextClassLoader(meLoader);
						return (null);
					}
				});
				Class<?> clazz = null;
				try {
					clazz = meLoader.loadClass(className);
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
					throw ex;
				}

				Constructor<?> constructor = clazz
						.getConstructor(new Class[] { Object.class });
				if (initObject != null) {
					instanceObj = constructor
							.newInstance(new Object[] { initObject });
				} else {
					instanceObj = clazz.newInstance();
				}
			} finally {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						currentThread.setContextClassLoader(parentLoader);
						return (null);
					}
				});
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Unable to instantiate method of this oject invoker: "
							+ e.getClass().getName() + ": "
							+ e.getLocalizedMessage());
		}
		return instanceObj;
	}

	public Object getInitObject() {
		return initObject;
	}

	public void setInitObject(Object initObject) {
		this.initObject = initObject;
	}

	public void setParameterTypes(Class<?>[] types) {
		paramTypes = types;
	}

	public void setParameters(Object... args) {
		params = args;
	}

	private Object[] getParameters() throws EvaluationException {
		// logger.info("params: " + SorcerUtil.arrayToString(params));
		// logger.info("paramTypes: " + SorcerUtil.arrayToString(paramTypes));
		if (context != null) {
			return new Object[] { context };
		} else if (params != null) {
			try {
				if (params.length == 1 && params[0] instanceof Context) {
					params = (Object[]) ((ServiceContext) params[0]).getArgs();
				}
			} catch (ContextException e) {
				e.printStackTrace();
				throw new EvaluationException(e);
			}
			return params;
		}
		return params;
	}

	public String getClassName() {
		return className;
	}

	public Object getTarget() {
		return target;
	}

	public String getSelector() {
		return selector;
	}
	
	public String describe() {
		StringBuilder sb = new StringBuilder("\nObjectIvoker");
		sb.append(", class name: " + className);
		sb.append(", selector: " + selector);
		sb.append(", target: " + target);
		sb.append("\nargs: " + params);

		return sb.toString();
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
