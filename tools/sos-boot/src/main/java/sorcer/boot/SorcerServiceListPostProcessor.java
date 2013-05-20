package sorcer.boot;

import com.sun.jini.start.NonActivatableServiceDescriptor;
import com.sun.jini.start.ServiceDescriptor;
import org.rioproject.start.RioServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.provider.boot.Booter;
import sorcer.provider.boot.SorcerDescriptorUtil;
import sorcer.provider.boot.SorcerServiceDescriptor;
import sorcer.resolver.Resolver;
import sorcer.util.ArtifactCoordinates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class SorcerServiceListPostProcessor implements ServiceListPostProcessor {
	final private static Logger log = LoggerFactory.getLogger(SorcerServiceListPostProcessor.class);
	public static final String WEBSTER = "sorcer.tools.webster.Webster";
	public static final String REGISTRAR = "com.sun.jini.reggie.Registrar";

	@Override
	public ServiceDescriptor[] postProcess(ServiceDescriptor[] services) {
		Map<String, Boolean> definedServices = new HashMap<String, Boolean>();
		definedServices.put(WEBSTER, false);
		definedServices.put(REGISTRAR, false);
		for (ServiceDescriptor service : services) {
			if (service instanceof NonActivatableServiceDescriptor) {
				NonActivatableServiceDescriptor descriptor = (NonActivatableServiceDescriptor) service;
				definedServices.putAll(checkServices(descriptor.getImplClassName()));
			} else if (service instanceof SorcerServiceDescriptor) {
				SorcerServiceDescriptor descriptor = (SorcerServiceDescriptor) service;
				definedServices.putAll(checkServices(descriptor.getImplClassName()));
			} else if(service instanceof RioServiceDescriptor){
				RioServiceDescriptor descriptor= (RioServiceDescriptor) service;
				definedServices.putAll(checkServices(descriptor.getImplClassName()));
			}
		}

		if (!definedServices.containsValue(false)) {
			return services;
		}

		List<ServiceDescriptor> result = new ArrayList<ServiceDescriptor>(Arrays.asList(services));
		String policy = System.getProperty("java.security.policy");
		if (!definedServices.get(WEBSTER)) {
			result.add(0, createWebsterDescriptor(policy));
		}
		if (!definedServices.get(REGISTRAR)) {
			result.add(0, createRegistrarDescriptor(policy));
		}

		return result.toArray(new ServiceDescriptor[result.size()]);
	}

	private Map<String, Boolean> checkServices(String implClassName) {
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		if (implClassName.equals(WEBSTER)) {
			result.put(WEBSTER, true);
		} else if (implClassName.equals(REGISTRAR)) {
			result.put(REGISTRAR, true);
		}
		return result;
	}

	private SorcerServiceDescriptor createRegistrarDescriptor(String policy) {
		log.info("Adding default registrar");
		return new SorcerServiceDescriptor(
				Resolver.resolveCodeBase(ArtifactCoordinates.coords("org.apache.river:reggie-dl")),
				policy,
				Resolver.resolveClassPath(ArtifactCoordinates.coords("org.apache.river:reggie")),
				"com.sun.jini.reggie.TransientRegistrarImpl",
				new File(SorcerEnv.getHomeDir(), "/configs/jini/configs/reggie.config").getPath()
		);
	}

	private ServiceDescriptor createWebsterDescriptor(String policy) {
		log.info("Adding default code server");
		try {
			return SorcerDescriptorUtil.getWebster(policy, 0, Booter.getWebsterHostName(), SorcerEnv.getWebsterRoots());
		} catch (IOException e) {
			throw new RuntimeException("Error while configuring Webster", e);
		}
	}
}
