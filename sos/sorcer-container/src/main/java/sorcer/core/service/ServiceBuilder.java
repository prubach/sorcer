package sorcer.core.service;

import com.google.inject.Injector;
import com.sun.jini.start.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.InjectionHelper;

import javax.inject.Inject;

/**
 * Thin layer between jini-style ctor(String[], Lifecycle) and javax.inject worlds.
 * Intended to replace sorcer.core.provider.ServiceProvider in service descriptors.
 *
 * @author Rafał Krupiński
 */
public class ServiceBuilder {
    private static final Logger log = LoggerFactory.getLogger(ServiceBuilder.class);
    @Inject
    private Injector injector;

    public ServiceBuilder(String[] args, LifeCycle lifeCycle) {
        InjectionHelper.injectMembers(this);
        ActualServiceBuilder instance = injector.createChildInjector(new ServiceModule(lifeCycle, args)).getInstance(ActualServiceBuilder.class);
        log.debug("Created instance {}", instance);
    }
}
