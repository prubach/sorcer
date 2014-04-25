package sorcer.core.service;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
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
import sorcer.config.BeanListener;
import sorcer.core.provider.Provider;
import sorcer.core.proxy.ProviderProxy;
import sorcer.util.ClassLoaders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class ActualServiceBuilder<T> implements IServiceBuilder<T> {
    private static Logger log = LoggerFactory.getLogger(ActualServiceBuilder.class);

    private Set<Module> modules = new HashSet<Module>();

    private Map<Class, Object> beanMap = new LinkedHashMap<Class, Object>();

    private IServiceBeanListener beanListener;

    private ClassLoader serviceClassLoader = ClassLoaders.current();

    // service processor specific configurations
    protected final Map<Object, Object> configurations = new HashMap<Object, Object>();

    protected Configuration jiniConfig = EmptyConfiguration.INSTANCE;

    protected ServiceBuilderConfig builderConfig = new ServiceBuilderConfig();

    @Inject
    protected Configurer configurer;
    private T bean;
    private Remote proxy;

    @Inject
    private ServiceRegistrar serviceRegistrar;
    private String[] configArgs;

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
        beanListener.preProcess(this);

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(new AbstractMatcher<Binding<?>>() {
                    @Override
                    public boolean matches(Binding<?> typeLiteral) {
                        return Key.get(getType()).equals(typeLiteral.getKey());
                    }
                }, new ProvisionListener() {
                    @Override
                    public <I> void onProvision(ProvisionInvocation<I> provision) {
                        I injectee = provision.provision();
                        beanListener.preProcess(ActualServiceBuilder.this, (T) injectee);
                    }
                });
            }
        });

        bean = get();

        //beanListener.preProcess(this, bean);

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

    @Inject
    protected void setBeanListeners(Set<BeanListener> beanListeners) {
        beanListener = new ServiceBeanListener(beanListeners);
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
}
