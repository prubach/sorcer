/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013 Sorcersoft.com S.A.
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

package sorcer.jini.jeri;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.security.Permission;
import java.util.*;
import java.util.logging.Logger;

import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicInvocationDispatcher;
import net.jini.jeri.InvocationDispatcher;
import net.jini.jeri.ServerCapabilities;
import net.jini.security.proxytrust.ProxyTrust;
import net.jini.security.proxytrust.ServerProxyTrust;
import net.jini.security.proxytrust.TrustEquivalence;
import org.slf4j.MDC;
import sorcer.core.ContextManagement;
import sorcer.service.ExertionException;
import sorcer.service.Service;

/**
 * A SorcerILFactory can be used with object interfaces as its services. Those
 * services exposed as interfaces do need implement Remote. A
 * {@link sorcer.core.provider.ServiceProvider} using this factory
 * should tell the factory what objects should be exposed as services and the
 * invocation dispatcher created by this factory will manage the delegation of
 * the method calls to the right exposed object. SORCER service beans (objects
 * with methods taking a parameter {@link sorcer.service.Context} and returning
 * {@link sorcer.service.Context} can be used transparently as
 * {@link sorcer.service.Service}s with either
 * {@link sorcer.core.provider.ServiceProvider} or
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SorcerILFactory extends BasicILFactory {
	protected final Logger logger = Logger.getLogger(BasicILFactory.class
			.getName());
	/**
	 * Exposed service type map. A key is an interface and a value its
	 * implementing object.
	 */
	protected Map<Class, Object> serviceBeanMap;

	/**
     * Creates a <code>SorcerILFactory</code> instance with no server
     * constraints, no permission class, a <code>null</code> class
     * loader and service beans.
     **/
    public SorcerILFactory() {
    	super();
    }

	/**
	 * Creates a <code>SorcerILFactory</code> instance with no server
	 * constraints, no permission class, and a <code>null</code> class loader.
	 * 
	 * @param serviceBeans
	 *            the object to be exposed as a service by the dispatcher of
	 *            this ILFactory
	 * 
	 */
	public SorcerILFactory(Map serviceBeans, ClassLoader loader) {
		this(null, null, loader, serviceBeans);
	}

	/**
	 * Creates a <code>SorcerILFactory</code> with the specified server
	 * constraints, permission class, and a <code>null</code> class loader.
	 * 
	 * @param serverConstraints
	 *            the server constraints, or <code>null</code>
	 * @param permissionClass
	 *            the permission class, or <code>null</code>
	 * @param serviceBeans
	 *            the object to be exposed as a service by the dispatcher of
	 *            this ILFactory
	 * @throws IllegalArgumentException
	 *             if the permission class is abstract, is not a subclass of
	 *             {@link Permission}, or does not have a public constructor
	 *             that has either one <code>String</code> parameter or one
	 *             {@link Method}parameter and has no declared exceptions
	 */
	public SorcerILFactory(MethodConstraints serverConstraints,
			Class permissionClass, Map serviceBeans)
			throws IllegalArgumentException {
		this(serverConstraints, permissionClass, null, serviceBeans);
	}

	/**
	 * Creates a <code>SorcerILFactory</code> with the specified server
	 * constraints, permission class, and class loader. The server constraints,
	 * if not <code>null</code>, are used to enforce minimum constraints for
	 * remote calls. The permission class, if not <code>null</code>, is used to
	 * perform server-side access control on incoming remote calls. The class
	 * loader, which may be <code>null</code>, is passed to the superclass
	 * constructor and is used by the {@link #createInstances createInstances}
	 * method.
	 * 
	 * @param serverConstraints
	 *            the server constraints, or <code>null</code>
	 * @param permissionClass
	 *            the permission class, or <code>null</code>
	 * @param loader
	 *            the class loader, or <code>null</code>
	 * @param serviceBeans
	 *            the object to be exposed as a service by the dispatcher of
	 *            this ILFactory
	 * 
	 * @throws IllegalArgumentException
	 *             if the permission class is abstract, is not a subclass of
	 *             {@link Permission}, or does not have a public constructor
	 *             that has either one <code>String</code> parameter or one
	 *             {@link Method}parameter and has no declared exceptions
	 */
	public SorcerILFactory(MethodConstraints serverConstraints,
			Class permissionClass, ClassLoader loader, Map serviceBeans)
			throws IllegalArgumentException {
		super(serverConstraints, permissionClass, loader);
		serviceBeanMap = serviceBeans;
        Map<Class, Object> superIfaces = new HashMap<Class, Object>();
        for (Map.Entry<Class, Object> e : serviceBeanMap.entrySet()) {
            for (Class iface : e.getKey().getInterfaces()) {
                superIfaces.put(iface, e.getValue());
            }
        }
        serviceBeanMap.putAll(superIfaces);
    }

	/**
	 * Returns a new array containing any additional interfaces that the proxy
	 * should implement, beyond the interfaces obtained by passing
	 * <code>impl</code> to the {@link #getRemoteInterfaces getRemoteInterfaces}
	 * method.
	 * 
	 * <p>
	 * <code>SorcerILFactory</code> implements this method to return a new array
	 * containing the interfaces of all services to be exposed and
	 * {@link RemoteMethodControl}and {@link TrustEquivalence}interfaces, in
	 * that order.
	 * 
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	protected Class[] getExtraProxyInterfaces(Remote impl)
			throws NullPointerException {
		if (impl == null) {
			throw new NullPointerException("impl is null");
		}

		List<Class> exposedInterfaces = new ArrayList<Class>();
		Class curr = null;
		Iterator it = serviceBeanMap.keySet().iterator();
		while (it.hasNext()) {
			curr = (Class) it.next();
			if (curr != null && !exposedInterfaces.contains(curr))
				exposedInterfaces.add(curr);
		}
		exposedInterfaces.add(Service.class);
		exposedInterfaces.add(RemoteMethodControl.class);
		exposedInterfaces.add(TrustEquivalence.class);
		// exposedInterfaces.add(net.jini.admin.Administrable.class);
        return exposedInterfaces.toArray(new Class[exposedInterfaces.size()]);
	}

	/** {@inheritDoc} */
	protected InvocationDispatcher createInvocationDispatcher(
			Collection methods, Remote impl, ServerCapabilities caps)
			throws ExportException {
		// pass on the method of the interfaces so as to make the dispatcher
		// aware that our custom methods need to be included
		Method[] additionalMethods;
		Class curr = null;
		Iterator it = serviceBeanMap.keySet().iterator();
		while (it.hasNext()) {
			curr = (Class) it.next();
			additionalMethods = curr.getDeclaredMethods();
			for (int j = 0; j < additionalMethods.length; j++)
				methods.add(additionalMethods[j]);
		}
		return new SorcerInvocationDispatcher(methods, caps,
				getServerConstraints(), getPermissionClass(), getClassLoader(), serviceBeanMap);
	}

	/**
	 * Our custom dispatcher to be used for exporting SORCER service beans.
	 */
	private static class SorcerInvocationDispatcher extends BasicInvocationDispatcher {

        private Map<Class, Object> serviceBeanMap;

        public SorcerInvocationDispatcher(Collection methods,
                                          ServerCapabilities serverCapabilities,
                                          MethodConstraints serverConstraints, Class permissionClass,
                                          ClassLoader loader, Map<Class, Object> serviceBeanMap) throws ExportException {
			super(methods, serverCapabilities, serverConstraints,
					permissionClass, loader);
            this.serviceBeanMap = serviceBeanMap;
        }

		protected Object invoke(Remote impl, Method method, Object[] args,
				Collection context) throws Throwable {
            String key = "SORCER-REMOTE-CALL";
            try{
                MDC.put(key, key);
                return doInvoke(impl, method, args, context);
            }finally {
                MDC.remove(key);
            }
        }

		protected Object doInvoke(Remote impl, Method method, Object[] args,
				Collection context) throws Throwable {
			if (impl == null || args == null || context == null)
				throw new NullPointerException();

			if (!method.isAccessible()
					&& !(Modifier.isPublic(method.getDeclaringClass()
							.getModifiers()) && Modifier.isPublic(method
							.getModifiers())))
				throw new IllegalArgumentException(
						"method not public or set accessible");

			Class decl = method.getDeclaringClass();
			if (decl == ProxyTrust.class
					&& method.getName().equals("getProxyVerifier")
					&& impl instanceof ServerProxyTrust) {
				if (args.length != 0)
					throw new IllegalArgumentException("incorrect arguments");

				return ((ServerProxyTrust) impl).getProxyVerifier();
			}
			Object obj = null;
			try {
				// handle context management by the containing provider
				if (decl == ContextManagement.class) {
					obj = method.invoke(impl, args);
					return obj;
				}
				// Check if the invocation is to be made on provider's service
				// beans
				Object service = serviceBeanMap.get(method.getDeclaringClass());
				if (service != null) {
					obj = method.invoke(service, args);
				} else {
					obj = method.invoke(impl, args);
				}
			} catch (Throwable t) {
				throw new ExertionException(t);
			}
			return obj;
		}
	}
}
