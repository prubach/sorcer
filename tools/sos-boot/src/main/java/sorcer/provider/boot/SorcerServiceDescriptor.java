/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 * Copyright 2008 the original author or authors.
 * Copyright 2005 Sun Microsystems, Inc.
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

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.jini.start.LifeCycle;
import sorcer.boot.util.JarClassPathHelper;
import sorcer.util.StringUtils;

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
 * @author Rafał Krupiński (SorcerSoft version)
 */
public class SorcerServiceDescriptor extends AbstractServiceDescriptor {
	private String codebase;
	private String policy;
	private String classpath;
	private String implClassName;
	private String[] serverConfigArgs;

    @Inject
    private JarClassPathHelper classPathHelper;

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
        if (descCodebase != null)
            if (!descCodebase.contains("http://")) {
                String[] jars = Booter.toArray(descCodebase);
                try {
                    if (address == null)
                        address = Booter.getHostAddress();
                    this.codebase = Booter.getCodebase(jars, address, Integer.toString(Booter.getPort()));
    			} catch (UnknownHostException e) {
                    logger.warn("Cannot get hostname for: {}", codebase);
                }
            } else {
		    	this.codebase = descCodebase;
            }
		this.policy = policy;
		this.classpath = classpath;
		this.implClassName = implClassName;
		this.serverConfigArgs = serverConfigArgs;
		this.lifeCycle = lifeCycle;
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
	public Set<URL> getCodebase() {
        if (codebase == null)
            return null;
        String[] codebaseArray = StringUtils.tokenizerSplit(codebase, " ");
        Set<URL> result = new HashSet<URL>();
        for (String aCodebaseArray : codebaseArray) {
            try {
                result.add(new URL(aCodebaseArray));
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Malformed URL in configuration: " + aCodebaseArray, e);
            }
        }
        return result;
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
	 * LifCycle accessor method.
	 *
	 * @return The classpath string associated with this service descriptor.
	 */
    public Set<URI> getClasspath() {
        if (classpath == null)
            return null;
        Set<String>paths = new HashSet<String>();
        for (String s : StringUtils.tokenizerSplit(classpath, File.pathSeparator)) {
            paths.add(s);
            paths.addAll(classPathHelper.getClassPathFromJar(new File(s)));
        }

        Set<URI> result = new HashSet<URI>();
        for (String aClasspathArray : paths)
            result.add(new File(aClasspathArray).toURI());
        return result;
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
    public String[] getServiceConfigArgs() {
		return (serverConfigArgs != null) ? serverConfigArgs.clone() : null;
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
