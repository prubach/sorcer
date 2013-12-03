/*
 * Copyright 2008 the original author or authors.
 * Copyright 2005 Sun Microsystems, Inc.
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
package sorcer.provider.boot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;
import java.rmi.MarshalledObject;
import java.rmi.RMISecurityManager;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Policy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.jini.config.Configuration;
import net.jini.export.ProxyAccessor;
import net.jini.security.BasicProxyPreparer;
import net.jini.security.ProxyPreparer;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.security.policy.PolicyFileProvider;

import com.sun.jini.config.Config;
import com.sun.jini.start.AggregatePolicyProvider;
import com.sun.jini.start.ClassLoaderUtil;
import com.sun.jini.start.HTTPDStatus;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.LoaderSplitPolicyProvider;
import com.sun.jini.start.ServiceProxyAccessor;
import sorcer.boot.util.ClassPathVerifier;
import sorcer.boot.util.JarClassPathHelper;
import sorcer.core.SorcerEnv;

/**
 * The SorcerServiceDescriptor class is a utility that conforms to the
 * Jini&trade; technology ServiceStarter framework, and will start a service
 * using the {@link sorcer.provider.boot.CommonClassLoader} as a shared,
 * non-activatable, in-process service. Clients construct this object with the
 * details of the service to be launched, then call <code>create</code> to
 * launch the service in invoking object's VM.
 * <P>
 * This class provides separation of the import codebase (where the server
 * classes are loaded from) from the export codebase (where clients should load
 * classes from for stubs, etc.) as well as providing an independent security
 * policy file for each service object. This functionality allows multiple
 * service objects to be placed in the same VM, with each object maintaining a
 * distinct codebase and policy.
 * <P>
 * Services need to implement the following "non-activatable constructor":
 * <blockquote>
 * 
 * <pre>
 * &lt;impl&gt;(String[] args, LifeCycle lc)
 * </pre>
 * 
 * </blockquote>
 * 
 * where,
 * <UL>
 * <LI>args - are the service configuration arguments
 * <LI>lc - is the hosting environment's {@link LifeCycle} reference.
 * </UL>
 * 
 * @author Dennis Reedy, updated for SORCER by M. Sobolewski
 */
public class SorcerServiceDescriptor extends AbstractServiceDescriptor {
	static String COMPONENT = "sorcer.provider.boot";

	/**
	 * The parameter types for the "activation constructor".
	 */
	private static final Class[] actTypes = { String[].class, LifeCycle.class };
	private String codebase;
	private String policy;
	private String classpath;
	private String implClassName;
	private String[] serverConfigArgs;
	private LifeCycle lifeCycle;
	private static LifeCycle NoOpLifeCycle = new LifeCycle() { // default, no-op
		// object
		public boolean unregister(Object impl) {
			return false;
		}
	};
	private static AggregatePolicyProvider globalPolicy = null;
	private static Policy initialGlobalPolicy = null;

	private static JarClassPathHelper classPathHelper = new JarClassPathHelper();

    /**
	 * Create a SorcerServiceDescriptor, assigning given parameters to their
	 * associated, internal fields.
	 *
	 * @param descCodebase
	 *            location where clients can download required service-related
	 *            classes (for example, stubs, proxies, etc.). Codebase
	 *            components must be separated by spaces in which each component
	 *            is in <code>URL</code> format.
	 * @param policy
	 *            server policy filename or URL
	 * @param classpath
	 *            location where server implementation classes can be found.
	 *            Classpath components must be separated by path separators.
	 * @param implClassName
	 *            name of server implementation class
	 * @param address
	 *            code server address used for the codebase
	 * @param lifeCycle
	 *            <code>LifeCycle</code> reference for hosting environment
	 * @param serverConfigArgs
	 *            service configuration arguments
	 */
	public SorcerServiceDescriptor(String descCodebase, String policy,
			String classpath, String implClassName, String address,
			// Optional Args
			LifeCycle lifeCycle, String... serverConfigArgs) {
//		if (descCodebase == null || policy == null || classpath == null
//				|| implClassName == null)
//			throw new NullPointerException("Codebase, policy, classpath, and "
//					+ "implementation cannot be null");
		if (descCodebase != null && descCodebase.indexOf("http://") < 0) {
			String[] jars = Booter.toArray(descCodebase);
			try {
				if (address == null)
					this.codebase = Booter.getCodebase(jars, "" + Booter.getPort());
				else
					this.codebase = Booter.getCodebase(jars, address, "" + Booter.getPort());
			} catch (UnknownHostException e) {
				logger.error("Cannot get hostanme for: " + codebase);
			}
		}
		else
			this.codebase = descCodebase;
		this.policy = policy;
		this.classpath = setClasspath(classpath);
		this.implClassName = implClassName;
		this.serverConfigArgs = serverConfigArgs;
		this.lifeCycle = (lifeCycle == null) ? NoOpLifeCycle : lifeCycle;
	}

	public SorcerServiceDescriptor(String descCodebase, String policy,
			String classpath, String implClassName,
			// Optional Args
			LifeCycle lifeCycle, String... serverConfigArgs) {
		this(descCodebase, policy, classpath, implClassName, null, lifeCycle, serverConfigArgs);
	}

	/**
	 * Create a SorcerServiceDescriptor. Equivalent to calling the other
	 * overloaded constructor with <code>null</code> for the
	 * <code>LifeCycle</code> reference.
	 * 
	 * @param codebase
	 *            location where clients can download required service-related
	 *            classes (for example, stubs, proxies, etc.). Codebase
	 *            components must be separated by spaces in which each component
	 *            is in <code>URL</code> format.
	 * @param policy
	 *            server policy filename or URL
	 * @param classpath
	 *            location where server implementation classes can be found.
	 *            Classpath components must be separated by path separators.
	 * @param implClassName
	 *            name of server implementation class
	 * @param serverConfigArgs
	 *            service configuration arguments
	 */
	public SorcerServiceDescriptor(String codebase, String policy,
			String classpath, String implClassName,
			// Optional Args
			String... serverConfigArgs) {
		this(codebase, policy, classpath, implClassName, null, serverConfigArgs);
	}

	/**
	 * Codebase accessor method.
	 * 
	 * @return The codebase string associated with this service descriptor.
	 */
	public String getCodebase() {
		return codebase;
	}

	/**
	 * Policy accessor method.
	 *
	 * @return The policy string associated with this service descriptor.
	 */
	public String getPolicy() {
		return policy;
	}

	/**
	 * <code>LifeCycle</code> accessor method.
	 *
	 * @return The <code>LifeCycle</code> object associated with this service
	 *         descriptor.
	 */
	public LifeCycle getLifeCycle() {
		return lifeCycle;
	}

	/**
	 * LifCycle accessor method.
	 *
	 * @return The classpath string associated with this service descriptor.
	 */
	public String getClasspath() {
		return classpath;
	}

	/**
	 * Implementation class accessor method.
	 *
	 * @return The implementation class string associated with this service
	 *         descriptor.
	 */
	public String getImplClassName() {
		return implClassName;
	}

	/**
	 * Service configuration arguments accessor method.
	 *
	 * @return The service configuration arguments associated with this service
	 *         descriptor.
	 */
	public String[] getServerConfigArgs() {
		return (serverConfigArgs != null) ? serverConfigArgs.clone() : null;
	}

	synchronized void ensureSecurityManager() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

    @Override
    protected Object doCreate(Configuration config) throws Exception {
		ensureSecurityManager();
		Object proxy = null;

        {
        /* Warn user of inaccessible codebase(s) */
            String codebase = getCodebase();
            if (codebase != null && codebase.startsWith("http"))
                HTTPDStatus.httpdWarning(codebase);
        }
        CommonClassLoader commonCL = getCommonClassLoader(config);

        Thread currentThread = Thread.currentThread();
		ClassLoader currentClassLoader = currentThread.getContextClassLoader();


		ClassAnnotator annotator = null;
        if (getCodebase() != null) {

            annotator = new ClassAnnotator(ClassLoaderUtil
                    .getCodebaseURLs(getCodebase()), new Properties());
        }

		ServiceClassLoader jsbCL = new ServiceClassLoader(ServiceClassLoader
				.getURIs(ClassLoaderUtil.getClasspathURLs(getClasspath())),
				annotator, commonCL);
		if (logger.isDebugEnabled())
			ClassLoaderUtil.displayClassLoaderTree(jsbCL);
		new ClassPathVerifier().verifyClassPaths(jsbCL);

		/*
		 * ServiceClassLoader jsbCL = new
		 * ServiceClassLoader(ClassLoaderUtil.getClasspathURLs(getClasspath()),
		 * annotator, commonCL);
		 */
		currentThread.setContextClassLoader(jsbCL);
		/* Get the ProxyPreparer */
		ProxyPreparer servicePreparer = (ProxyPreparer) Config.getNonNullEntry(
				config, COMPONENT, "servicePreparer", ProxyPreparer.class,
				new BasicProxyPreparer());
		synchronized (SorcerServiceDescriptor.class) {
			/* supplant global policy 1st time through */
			if (globalPolicy == null) {
				initialGlobalPolicy = Policy.getPolicy();
				globalPolicy = new AggregatePolicyProvider(initialGlobalPolicy);
				Policy.setPolicy(globalPolicy);
                logger.debug("Global policy set: {}",
                    globalPolicy);
			}
			DynamicPolicyProvider service_policy = new DynamicPolicyProvider(
					new PolicyFileProvider(getPolicy()));
			LoaderSplitPolicyProvider splitServicePolicy = new LoaderSplitPolicyProvider(
					jsbCL, service_policy, new DynamicPolicyProvider(
							initialGlobalPolicy));
			/*
			 * Grant "this" code enough permission to do its work under the
			 * service policy, which takes effect (below) after the context
			 * loader is (re)set.
			 */
			splitServicePolicy.grant(SorcerServiceDescriptor.class, null,
					new Permission[] { new AllPermission() });
			globalPolicy.setPolicy(jsbCL, splitServicePolicy);
		}
		Object impl;
		try {

			Class implClass;
			implClass = Class.forName(getImplClassName(), false, jsbCL);
            logger.debug("Attempting to get implementation constructor");
			Constructor constructor = implClass
					.getDeclaredConstructor(actTypes);
            logger.debug("Obtained implementation constructor: ",
                    constructor);
            constructor.setAccessible(true);
			impl = constructor.newInstance(getServerConfigArgs(), lifeCycle);

            logger.debug("Obtained implementation instance: {}", impl);
			if (impl instanceof ServiceProxyAccessor) {
				proxy = ((ServiceProxyAccessor) impl).getServiceProxy();
			} else if (impl instanceof ProxyAccessor) {
				proxy = ((ProxyAccessor) impl).getProxy();
			} else {
				proxy = null; // just for insurance
			}
			if (proxy != null) {
				proxy = servicePreparer.prepareProxy(proxy);
			}
            logger.debug("Proxy =  {}", proxy);
			// TODO - factor in code integrity for MO
            proxy = (new MarshalledObject(proxy)).get();
            currentThread.setContextClassLoader(currentClassLoader);
        } finally {
			currentThread.setContextClassLoader(currentClassLoader);
		}
		return (new Created(impl, proxy));
	}

    /*
	 * Iterate through the classpath, for each jar see if there is a ClassPath
	 * manifest setting. If there is, append the settings to the classpath
	 */
	private String setClasspath(String cp) {
		String[] inClassPathArr = cp.split(File.pathSeparator);
		Set<String> paths = new HashSet<String>();
		for (String s : inClassPathArr) {
			paths.add(s);
			paths.addAll(classPathHelper.getClassPathFromJar(new File(s)));
		}
		return Booter.getClasspath(paths.toArray(new String[paths.size()]));
	}

    public String toString() {
		return "SorcerServiceDescriptor{"
				+ "codebase='"
				+ codebase
				+ '\''
				+ ", policy='"
				+ policy
				+ '\''
				+ ", classpath='"
				+ classpath
				+ '\''
				+ ", implClassName='"
				+ implClassName
				+ '\''
				+ ", serverConfigArgs="
				+ (serverConfigArgs == null ? null : Arrays
						.asList(serverConfigArgs)) + ", lifeCycle=" + lifeCycle
				+ '}';
	}
}
