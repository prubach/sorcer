package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
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

    @Inject
    protected void setConfigArgs(String[] args) throws ConfigurationException {
        jiniConfig = ConfigurationProvider.getInstance(args, serviceClassLoader);
        configurer.process(builderConfig, jiniConfig);
    }

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
        bean = get();
        beanListener.preProcess(this, bean);

        if (builderConfig.export) {
            log.debug("Exporting {}", bean);
            proxy = builderConfig.exporter.export((Remote) bean);
        }

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

    @Override
    public T get() {
        try {
            return (T) builderConfig.type.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not create bean of type " + builderConfig.type, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not create bean of type " + builderConfig.type, e);
        }
    }
}
