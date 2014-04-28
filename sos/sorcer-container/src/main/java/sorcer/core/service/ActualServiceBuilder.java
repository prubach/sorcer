/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.start.LifeCycle;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.EmptyConfiguration;
import net.jini.core.entry.Entry;
import net.jini.lookup.entry.Name;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.container.core.ConfiguringModule;
import sorcer.container.core.SingletonModule;
import sorcer.core.provider.Provider;
import sorcer.core.proxy.ProviderProxy;
import sorcer.util.ClassLoaders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.*;

import static sorcer.container.core.InitializingModule.INIT_MODULE;

/**
 * @author Rafał Krupiński
 */
public class ActualServiceBuilder<T> implements IServiceBuilder<T>, DestroyAdmin {
    private static Logger log = LoggerFactory.getLogger(ActualServiceBuilder.class);

    private Set<Module> modules = new HashSet<Module>();

    private Map<Class, Object> beanMap = new LinkedHashMap<Class, Object>();

    private ClassLoader serviceClassLoader = ClassLoaders.current();

    // service processor specific configurations
    protected final Map<Object, Object> configurations = new HashMap<Object, Object>();

    protected Configuration jiniConfig = EmptyConfiguration.INSTANCE;

    protected ServiceBuilderConfig builderConfig = new ServiceBuilderConfig();

    private T bean;
    private Remote proxy;
    private String[] configArgs;

    @Inject
    protected Configurer configurer;

    @Inject
    private ServiceRegistrar serviceRegistrar;

    @Inject
    protected void setConfigArgs(String[] args) throws ConfigurationException {
        configArgs = args;
        jiniConfig = ConfigurationProvider.getInstance(args, serviceClassLoader);
        configurer.process(builderConfig, jiniConfig);
    }

    @Inject
    private LifeCycle lifeCycle;

    public ActualServiceBuilder() {
    }

    public ActualServiceBuilder(String typeName) throws ClassNotFoundException {
        builderConfig.type = Class.forName(typeName, false, serviceClassLoader);
    }

    @Override
    public void addInterceptor(final MethodInterceptor interceptor) {
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(Matchers.subclassesOf(Provider.class), Matchers.any(), interceptor);
            }
        });
    }

    @Override
    public <I> void contributeInterface(I impl, Class<? super I>... iface) {
        for (Class<? super I> type : iface) {
            beanMap.put(type, impl);
        }
    }

    @Override
    public void addAttribute(Entry attribute) {

    }

    @Override
    public Configuration getProviderConfiguration() {
        return jiniConfig;
    }

    @PostConstruct
    public void init() throws ExportException {
        modules.add(INIT_MODULE);
        modules.add(new ConfiguringModule(jiniConfig, configurer, getType()));
        // request binding in the current injector, so the bean is visible to our bean listeners
        modules.add(new SingletonModule(getType()));

        bean = get();

        if (builderConfig.export) {
            log.debug("Exporting {}", bean);
            proxy = builderConfig.exporter.export((Remote) bean);
        }

        if (builderConfig.register)
            try {
                serviceRegistrar.preProcess(this, bean);
            } catch (RuntimeException x) {
                log.warn("Error registering {}", bean, x);
                builderConfig.exporter.unexport(true);
                throw x;
            }
    }

    @Override
    public Object getConfiguration(Object key) {
        return configurations.get(key);
    }

    @Override
    public void putConfiguration(Object key, Object data) {
        if (configurations.containsKey(key))
            log.warn("Overriding configuration @ {}", key);
        configurations.put(key, data);
    }

    @Override
    public String getName() {
        return builderConfig.type.getSimpleName();
    }

    @Override
    public Class<T> getType() {
        return builderConfig.type;
    }

    @Override
    public Remote getProxy() {
        return ProviderProxy.wrapServiceProxy(proxy, null, null);
    }

    @Override
    public Entry[] getAttributes() {
        return new Entry[]{
                new Name(getName())
        };
    }

    @Inject
    Injector injector;

    @Override
    public T get() {
        return (T) injector.createChildInjector(modules).getInstance(builderConfig.type);
    }

    @Override
    public void destroy() throws RemoteException {
        if(bean instanceof DestroyAdmin)
            ((DestroyAdmin) bean).destroy();
    }
}
