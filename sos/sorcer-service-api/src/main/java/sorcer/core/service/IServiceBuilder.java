package sorcer.core.service;

import net.jini.core.entry.Entry;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author Rafał Krupiński
 */
public interface IServiceBuilder {
    public void addInterceptor(MethodInterceptor interceptor);

    <T> void contributeInterface(T impl, Class<? super T>... iface);

    public void addAttribute(Entry attribute);
}
