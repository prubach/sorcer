package sorcer.boot;

import com.sun.jini.start.ServiceDescriptor;

/**
 * @author Rafał Krupiński
 */
public interface ServiceListPostProcessor {
	public ServiceDescriptor[] postProcess(ServiceDescriptor[] services);
}
