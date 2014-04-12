package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.EmptyConfiguration;
import net.jini.core.entry.Entry;
import net.sf.cglib.proxy.Mixin;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.BeanListener;
import sorcer.core.provider.Provider;
import sorcer.util.ClassLoaders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class ActualServiceBuilder<T> implements IServiceBuilder<T> {
    private static Logger log = LoggerFactory.getLogger(ActualServiceBuilder.class);

    private Set<Module> modules = new HashSet<Module>();

    private Map<Class, Object> beanMap = new LinkedHashMap<Class, Object>();

    private IServiceBeanListener beanListener;

    private String typeName;

    private ClassLoader serviceClassLoader = ClassLoaders.current();

    // service processor specific configurations
    protected final Map<Object, Object> configurations = new HashMap<Object, Object>();

    protected Configuration configuration = EmptyConfiguration.INSTANCE;

    public ActualServiceBuilder() {
    }

    public ActualServiceBuilder(String typeName) {
        this.typeName = typeName;
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

    @Inject
    public void setConfigArgs(String[] configArgs) throws ConfigurationException {
        configuration = ConfigurationProvider.getInstance(configArgs);
    }

    @Override
    public Configuration getProviderConfiguration() {
        return configuration;
    }

    @PostConstruct
    public void init() {
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
        return "TODO-UNKNOWN";
    }

    @Override
    public Class<T> getType() {
        return null;
    }

    @Override
    public T get() {
        try {
            return doGet();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not create bean of type " + typeName, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not create bean of type " + typeName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not create bean of type " + typeName, e);
        }
    }

    private T doGet() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map<Class, Object> beans = new HashMap<Class, Object>(beanMap);
        Class<T> type = (Class<T>) Class.forName(typeName, false, serviceClassLoader);
        T bean = type.newInstance();
        traverseClass(bean, type, beans);
        Mixin.Generator generator = new Mixin.Generator();
        generator.setClassLoader(serviceClassLoader);
        Set<Class> allTypes = beans.keySet();
        generator.setClasses(allTypes.toArray(new Class[allTypes.size()]));
        generator.setDelegates(beans.values().toArray());

        return (T) generator.create();
    }

    private static void traverseClass(Object o, Class type, Map<Class, Object> result) {
        Class c = type;
        do
            for (Class iface : c.getInterfaces()) {
                traverseIface(o, iface, result);
            }
        while ((c = c.getSuperclass()) != null);
    }

    private static void traverseIface(Object o, Class type, Map<Class, Object> result) {
        if (result.containsKey(type)) {
            Object override = result.get(type);
            log.info("Interface {} of bean {} is overiden by {}", type, o, override);
        } else {
            result.put(type, o);
        }

        for (Class st : type.getInterfaces())
            traverseIface(o, st, result);
    }
}
