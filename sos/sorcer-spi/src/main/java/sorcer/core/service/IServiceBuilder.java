package sorcer.core.service;

import net.jini.config.Configuration;
import net.jini.core.entry.Entry;
import org.aopalliance.intercept.MethodInterceptor;

import javax.inject.Provider;
import java.rmi.Remote;

/**
 * @author Rafał Krupiński
 */
public interface IServiceBuilder<T> extends Provider<T> {
    public void addInterceptor(MethodInterceptor interceptor);

    <I> void contributeInterface(I impl, Class<? super I>... iface);

    public void addAttribute(Entry attribute);

    public Configuration getProviderConfiguration();

    Object getConfiguration(Object key);

    void putConfiguration(Object key, Object data);

    String getName();

    Class<?> getType();

    Remote getProxy();

    Entry[] getAttributes();
}
