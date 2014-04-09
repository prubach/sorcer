package sorcer.core.service;

import com.google.inject.AbstractModule;
import com.sun.jini.start.LifeCycle;

/**
* @author Rafał Krupiński
*/
public class ServiceModule extends AbstractModule {
    private final LifeCycle lc;
    private String[] serviceConfigArgs;

    public ServiceModule(LifeCycle lc, String[] serviceConfigArgs) {
        this.lc = lc;
        this.serviceConfigArgs = serviceConfigArgs;
    }

    @Override
    protected void configure() {
        bind(String[].class).toInstance(serviceConfigArgs);
        bind(LifeCycle.class).toInstance(lc);
    }
}
