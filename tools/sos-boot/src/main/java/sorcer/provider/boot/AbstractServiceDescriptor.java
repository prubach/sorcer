package sorcer.provider.boot;
/**
 *
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jini.start.*;
import edu.emory.mathcs.util.classloader.URIClassLoader;
import net.jini.config.Configuration;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.security.policy.PolicyFileProvider;
import org.rioproject.loader.ClassAnnotator;
import org.rioproject.loader.ServiceClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.destroy.ServiceDestroyer;
import sorcer.boot.util.ClassPathVerifier;
import sorcer.boot.util.LifeCycleMultiplexer;
import sorcer.container.core.ConfiguringModule;
import sorcer.container.core.SingletonModule;
import sorcer.core.service.Configurer;
import sorcer.core.service.ServiceModule;
import sorcer.util.ClassLoaders;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Policy;
import java.util.*;

import static sorcer.container.core.InitializingModule.INIT_MODULE;

/**
 * @author Rafał Krupiński
 */
public abstract class AbstractServiceDescriptor implements ServiceDescriptor {
    /*
    * Injection works after the constructor is called, but before calling create
    */


    private Set<URL> codebase;

    private Set<URI> classpath;

    private String implClassName;

    private List<String> configArgs;

    /**
     * The parameter types for the "activation constructor".
     */
    private LifeCycle lifeCycle;

    private String policyFile;

    @Inject
    @Named("globalPolicy")
    protected AggregatePolicyProvider globalPolicy;

    @Inject
    @Named("initialGlobalPolicy")
    protected Policy initialGlobalPolicy;

    @Inject
    protected Injector parentInjector;

    @Inject
    protected Configurer configurer;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractServiceDescriptor() {
    }

    protected AbstractServiceDescriptor(Set<URL> codebase, Set<URI> casspath, String className, List<String> configArgs, String policyFile) {
        this.codebase = codebase;
        this.classpath = casspath;
        this.implClassName = className;
        this.configArgs = configArgs;
        this.policyFile = policyFile;
    }

    public AbstractServiceDescriptor(String[] serverConfigArgs, LifeCycle lifeCycle) {
        this.configArgs = new ArrayList<String>();
        Collections.addAll(configArgs, serverConfigArgs);
        this.lifeCycle = lifeCycle;
    }

    /**
     * @see com.sun.jini.start.ServiceDescriptor#create
     */
    public Service create(Configuration config) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = currentThread.getContextClassLoader();

        ClassAnnotator annotator = null;
        Set<URL> codebase = getCodebase();
        if (codebase != null && !codebase.isEmpty()) {
            annotator = new ClassAnnotator(codebase.toArray(new URL[codebase.size()]));
        }
        ClassLoader classLoader = null;
        try {
            Set<URI> classpath = getClasspath();
            if (classpath != null && !classpath.isEmpty()) {
                classLoader = getServiceClassLoader(currentClassLoader, annotator, classpath);
                currentThread.setContextClassLoader(classLoader);
                if (logger.isDebugEnabled())
                    try {
                        ClassLoaderUtil.displayClassLoaderTree(classLoader);
                    } catch (ArrayIndexOutOfBoundsException ignore) {
                    }
                new ClassPathVerifier().verifyClassPaths(classLoader);
            } else {
                classLoader = currentClassLoader;
            }

            String policyFilePath = getPolicyFile();
            if (policyFilePath != null && classpath != null)
                synchronized (AbstractServiceDescriptor.class) {
            /*
             * Grant "this" code enough permission to do its work under the
			 * service policy, which takes effect (below) after the context
			 * loader is (re)set.
			 */
                    DynamicPolicyProvider service_policy = new DynamicPolicyProvider(
                            new PolicyFileProvider(policyFilePath));
                    LoaderSplitPolicyProvider splitServicePolicy = new LoaderSplitPolicyProvider(
                            classLoader, service_policy, new DynamicPolicyProvider(
                            initialGlobalPolicy)
                    );
                    splitServicePolicy.grant(AbstractServiceDescriptor.class, null,
                            new Permission[]{new AllPermission()});
                    globalPolicy.setPolicy(classLoader, splitServicePolicy);
                }

            List<Module> modules = new LinkedList<Module>();

            Class implClass = Class.forName(getImplClassName(), true, classLoader);
            modules.add(new ConfiguringModule(config, configurer, implClass));
            modules.add(getInjectorModule());
            modules.add(INIT_MODULE);
            modules.add(createFactoryModule(implClass));

            Injector injector = parentInjector.createChildInjector(modules);
            Object impl = injector.getInstance(implClass);
            return (new Service(impl, null, this));
        } catch (NoClassDefFoundError cnfe) {
            if (classLoader instanceof URLClassLoader) {
                StringBuilder sb = new StringBuilder("Tried loading from: \n");
                for (URL url : ((URLClassLoader) classLoader).getURLs())
                    sb.append(url + "\n");
                logger.error(sb.toString());
            }
            throw cnfe;
        } finally {
            currentThread.setContextClassLoader(currentClassLoader);
        }
    }

    private ClassLoader getServiceClassLoader(ClassLoader parentLoader, ClassAnnotator annotator, Set<URI> classpath) {
        URI[] classpathArr = classpath.toArray(new URI[classpath.size()]);
        if (annotator != null)
            return new ServiceClassLoader(classpathArr, annotator, parentLoader);
        else
            return new URIClassLoader(classpathArr, parentLoader);
    }

    private static class FactoryModule<T> extends AbstractModule {
        private Class<T> implClass;
        private java.lang.reflect.Constructor<T> constructor;

        private FactoryModule(Class<T> implClass, Constructor<T> constructor) {
            this.implClass = implClass;
            this.constructor = constructor;
        }

        @Override
        protected void configure() {
            bind(implClass).toConstructor(constructor);
        }
    }

    @SuppressWarnings("unchecked")
    private Module createFactoryModule(Class<?> implClass) {
        try {
            return new FactoryModule(implClass, implClass.getDeclaredConstructor(new Class[]{String[].class, LifeCycle.class}));
        } catch (NoSuchMethodException ignore) {
            // Class has no required constructor, assume it has the default one
            // or one annotated with @Inject
            // Also make sure the type is bound to current injector
            return new SingletonModule(implClass);
        }
    }

    protected Module getInjectorModule() {
        LifeCycle lc = getLifeCycle();
        if (lc == null)
            lc = defaultLifeCycle;
        List<String> serviceConfigArgs = getServiceConfigArgs();

        String[] args = serviceConfigArgs == null ? new String[0] : serviceConfigArgs.toArray(new String[serviceConfigArgs.size()]);
        return new ServiceModule(lc, args);
    }

    protected void setImplClassName(String className) {
        this.implClassName = className;
    }

    public String getImplClassName() {
        return implClassName;
    }

    protected List<String> getServiceConfigArgs() {
        return configArgs;
    }

    protected void setServiceConfigArgs(List<String> configFile) {
        this.configArgs = configFile;
    }

    protected String getPolicyFile() {
        return policyFile;
    }

    protected void setPolicyFile(String policyFile) {
        this.policyFile = policyFile;
    }

    public Set<URL> getCodebase() {
        return codebase;
    }

    protected void setCodebase(Set<URL> codebase) {
        this.codebase = codebase;
    }

    protected Set<URI> getClasspath() {
        return classpath;
    }

    protected void setClasspath(Set<URI> classpath) {
        this.classpath = classpath;
    }

    /**
     * Object returned by
     * {@link sorcer.provider.boot.SorcerServiceDescriptor#create(net.jini.config.Configuration)
     * SorcerServiceDescriptor.create()} method that returns the proxy and
     * implementation references for the created service.
     */
    public static class Service {
        /**
         * The reference to the proxy of the created service
         */
        public final Object proxy;
        /**
         * The reference to the implementation of the created service
         */
        public final Object impl;

        public final ServiceDescriptor descriptor;

        public Exception exception;

        public ServiceDestroyer destroyer;

        /**
         * Constructs an instance of this class.
         *
         * @param impl       reference to the implementation of the created service
         * @param proxy      reference to the proxy of the created service
         * @param descriptor service descriptor of the service
         */
        public Service(Object impl, Object proxy, ServiceDescriptor descriptor) {
            this.proxy = proxy;
            this.impl = impl;
            this.descriptor = descriptor;
        }

        public Service(Object impl, Object proxy, ServiceDescriptor descriptor, Exception exception) {
            this(impl, proxy, descriptor);
            this.exception = exception;
        }
    }

    public synchronized void addLifeCycle(LifeCycle lifeCycle) {
        if (this.lifeCycle == null)
            this.lifeCycle = lifeCycle;
        else {
            if (this.lifeCycle instanceof LifeCycleMultiplexer)
                ((LifeCycleMultiplexer) this.lifeCycle).add(lifeCycle);
            else
                this.lifeCycle = new LifeCycleMultiplexer(new HashSet<LifeCycle>(Arrays.asList(this.lifeCycle, lifeCycle)));
        }
    }

    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    protected static LifeCycle defaultLifeCycle = new LifeCycle() {
        public boolean unregister(Object impl) {
            return false;
        }
    };

    public String toString() {
        List<String> _configArgs = getServiceConfigArgs();
        return getClass().getSimpleName()+"{codebase='"
                + getCodebase()
                + '\''
                + ", policy='"
                + getPolicyFile()
                + '\''
                + ", classpath='"
                + getClasspath()
                + '\''
                + ", implClassName='"
                + getImplClassName()
                + '\''
                + ", serverConfigArgs="
                + (_configArgs == null ? null : _configArgs)
                + ", lifeCycle=" + getLifeCycle()
                + '}';
    }
}
