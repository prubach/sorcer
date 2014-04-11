package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;
import net.jini.config.Configuration;
import net.jini.core.entry.Entry;
import org.aopalliance.intercept.MethodInterceptor;
import sorcer.config.BeanListener;
import sorcer.core.provider.Provider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ActualServiceBuilder implements IServiceBuilder {
    private Set<Module> modules = new HashSet<Module>();

    private Map<Class, Object> beanMap = new HashMap<Class, Object>();

    private IServiceBeanListener beanListener;

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
    public <T> void contributeInterface(T impl, Class<? super T>... iface) {
        for (Class<? super T> type : iface) {
            beanMap.put(type, impl);
        }
    }

    @Override
    public void addAttribute(Entry attribute) {

    }

    @PostConstruct
    public void init() {
    }

    @Inject
    protected void setBeanListeners(Set<BeanListener> beanListeners) {
        beanListener = new ServiceBeanListener(beanListeners);
    }

    @Override
    public Configuration getProviderConfiguration() {
        return null;
    }
}
