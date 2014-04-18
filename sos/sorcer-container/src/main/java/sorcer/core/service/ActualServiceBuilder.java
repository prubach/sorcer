package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.EmptyConfiguration;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.entry.Name;
import net.sf.cglib.proxy.Mixin;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.BeanListener;
import sorcer.core.UEID;
import sorcer.core.provider.Provider;
import sorcer.core.proxy.ProviderProxy;
import sorcer.jini.lookup.entry.SorcerServiceInfo;
import sorcer.service.*;
import sorcer.util.ClassLoaders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.security.auth.Subject;
import java.rmi.Remote;
import java.rmi.RemoteException;
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
        return "TODO-UNKNOWN";
    }

    @Override
    public Class<T> getType() {
        return null;
    }

    @Override
    public Remote getProxy() {
        return ProviderProxy.wrapServiceProxy(proxy, null, null);
    }

    @Override
    public Entry[] getAttributes() {
        return new Entry[]{
                //new SorcerServiceInfo(),
                new Name(getName())
        };
    }

    @Override
    public T get() {
        try {
            return doGet();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not create bean of type " + builderConfig.type, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not create bean of type " + builderConfig.type, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not create bean of type " + builderConfig.type, e);
        }
    }

    private T doGet() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map<Class, Object> beans = new HashMap<Class, Object>(beanMap);
        T bean = (T) builderConfig.type.newInstance();

        beans.put(Provider.class, new ProviderImpl(jiniConfig, proxy));

        traverseClass(bean, builderConfig.type, beans);
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

class ProviderImpl implements Provider {
    private Configuration configuration;
    private Object proxy;

    ProviderImpl(Configuration configuration, Object proxy) {
        this.configuration = configuration;
        this.proxy = proxy;
    }

    @Override
    public ServiceID getProviderID() throws RemoteException {
        return new ServiceID(1,2);
    }

    @Override
    public String getProviderName() throws RemoteException {
        return "provider-name";
    }

    @Override
    public List<Object> getProperties() throws RemoteException {
        return new ArrayList<Object>(Arrays.asList("getProperties"));
    }

    @Override
    public Configuration getProviderConfiguration() throws RemoteException {
        return configuration;
    }

    @Override
    public boolean mutualExclusion() throws RemoteException {
        return false;
    }

    @Override
    public String getDescription() throws RemoteException {
        return "description";
    }

    @Override
    public boolean isBusy() throws RemoteException {
        return false;
    }

    @Override
    public void destroy() throws RemoteException {

    }

    @Override
    public Properties getJavaSystemProperties() throws RemoteException {
        return System.getProperties();
    }

    @Override
    public Object getProxy() throws RemoteException {
        return proxy;
    }

    @Override
    public boolean isContextValid(Context<?> dataContext, Signature forSignature) throws RemoteException {
        return true;
    }

    @Override
    public java.util.logging.Logger getLogger() throws RemoteException {
        return null;
    }

    @Override
    public Object getAdmin() throws RemoteException {
        return proxy;
    }

    @Override
    public void stop(UEID ref, Subject subject) throws RemoteException, UnknownExertionException, AccessDeniedException {

    }

    @Override
    public void suspend(UEID ref, Subject subject) throws RemoteException, UnknownExertionException, AccessDeniedException {

    }

    @Override
    public void resume(Exertion ex) throws RemoteException, ExertionException {

    }

    @Override
    public void step(Exertion ex) throws RemoteException, ExertionException {

    }

    @Override
    public Exertion service(Exertion exertion, Transaction txn) throws TransactionException, ExertionException, RemoteException {
        return null;
    }
}
