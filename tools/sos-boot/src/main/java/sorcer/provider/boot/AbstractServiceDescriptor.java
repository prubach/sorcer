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
import net.jini.config.Configuration;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.security.policy.PolicyFileProvider;
import org.rioproject.loader.ClassAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.ServiceDestroyer;
import sorcer.boot.util.ClassPathVerifier;
import sorcer.boot.util.LifeCycleMultiplexer;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Policy;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public abstract class AbstractServiceDescriptor implements ServiceDescriptor {

    /**
     * The parameter types for the "activation constructor".
     */
    protected LifeCycle lifeCycle;

    @Inject
    @Named("globalPolicy")
    protected AggregatePolicyProvider globalPolicy;

    @Inject
    @Named("initialGlobalPolicy")
    protected Policy initialGlobalPolicy;

    @Inject
    protected Injector parentInjector;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @see com.sun.jini.start.ServiceDescriptor#create
     */
    public Service create(Configuration config) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = currentThread.getContextClassLoader();

        ClassAnnotator annotator = null;
        Set<URL> codebase = getCodebase();
        if (codebase != null) {
            annotator = new ClassAnnotator(codebase.toArray(new URL[codebase.size()]));
        }

        Set<URI> classpath = getClasspath();
        ClassLoader classLoader;
        if (classpath != null) {
            classLoader = new ServiceClassLoader(classpath.toArray(new URI[classpath.size()]), annotator, currentClassLoader);
            currentThread.setContextClassLoader(classLoader);
            if (logger.isDebugEnabled())
                ClassLoaderUtil.displayClassLoaderTree(classLoader);
            new ClassPathVerifier().verifyClassPaths(classLoader);
        } else {
            classLoader = currentClassLoader;
        }

        String policyFilePath = getPolicy();
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

        Injector injector = parentInjector;
        List<Module> modules = new LinkedList<Module>();
        Module module = getInjectorModule();
        if (module != null)
            modules.add(module);

        try {
            Class implClass = Class.forName(getImplClassName(), true, classLoader);
            module = createFactoryModule(implClass);
            if (module != null)
                modules.add(module);

            injector = injector.createChildInjector(modules);
            Object impl = injector.getInstance(implClass);
            return (new Service(impl, null, this));
        } finally {
            currentThread.setContextClassLoader(currentClassLoader);
        }
    }

    private static class FactoryModule extends AbstractModule {
        private Class<Object> implClass;
        private java.lang.reflect.Constructor<java.lang.Object> constructor;

        private FactoryModule(Class<Object> implClass, Constructor<Object> constructor) {
            this.implClass = implClass;
            this.constructor = constructor;
        }

        @Override
        protected void configure() {
            bind(implClass).toConstructor(constructor);
        }
    }

    private Module createFactoryModule(Class<Object> implClass) {
        try {
            return new FactoryModule(implClass, implClass.getDeclaredConstructor(new Class[]{String[].class, LifeCycle.class}));
        } catch (NoSuchMethodException ignore) {
            // Class has no required constructor, assume it has the default one
            // or one annotated with @Inject
            return null;
        }
    }

    protected Module getInjectorModule() {
        LifeCycle lc = lifeCycle != null ? lifeCycle : defaultLifeCycle;
        return new ServiceModule(lc, getServiceConfigArgs());
    }

    private static class ServiceModule extends AbstractModule {
        private final LifeCycle lc;
        private String[] serviceConfigArgs;

        public ServiceModule(LifeCycle lc, String[] serviceConfigArgs) {
            this.lc = lc;
            this.serviceConfigArgs = serviceConfigArgs;
        }

        @Override
        protected void configure() {
            bind(String[].class).toInstance(serviceConfigArgs);
            bind(LifeCycle.class).toInstance(lc);
        }
    }

    protected abstract String getImplClassName();

    protected abstract String[] getServiceConfigArgs();

    protected abstract String getPolicy();

    protected abstract Set<URL> getCodebase();

    protected abstract Set<URI> getClasspath();

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

    protected static LifeCycle defaultLifeCycle = new LifeCycle() {
        public boolean unregister(Object impl) {
            return false;
        }
    };
}
