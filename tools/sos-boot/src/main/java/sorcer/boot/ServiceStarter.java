/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.boot;

import com.google.inject.*;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.ServiceDescriptor;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.EmptyConfiguration;
import org.rioproject.start.RioServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.destroy.ServiceDestroyer;
import sorcer.boot.platform.PlatformLoader;
import sorcer.boot.util.ServiceDescriptorProcessor;
import sorcer.core.SorcerConstants;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.util.ClassLoaders;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

import static sorcer.boot.destroy.ServiceDestroyerFactory.getDestroyer;
import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.provider.boot.AbstractServiceDescriptor.Service;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter implements LifeCycle {
    final private static Logger log = LoggerFactory.getLogger(ServiceStarter.class);

    private final Deque<Service> services = new LinkedList<Service>();

    //just to keep the references
    private final Set<Service> nonDestroyServices = new HashSet<Service>();

    private volatile boolean bootInterrupted;

    private final LifeCycle exitMonitor;
    protected Injector injector;
    private PlatformLoader platformLoader;

    public ServiceStarter(LifeCycle exitMonitor) {
        this.exitMonitor = exitMonitor;
    }

    /**
     * Start services from the configs
     *
     * @param configs file path or URL of the services.config configuration
     */
    public void start(Collection<File> configs) throws ConfigurationException {
        log.info("******* Starting Sorcersoft.com SORCER *******");

        Injector rootInjector = Guice.createInjector(new CoreModule());

        File rioHome = getRioHome();
        File rioPlatform = new File(rioHome, "config/platform");
        File sorcerPlatform = new File(rioPlatform, "service");
        platformLoader = new PlatformLoader(rootInjector, rioPlatform, sorcerPlatform);
        platformLoader.create();
        injector = platformLoader.getInjector();

        log.debug("Starting from {}", configs);

        ServiceDescriptorFactory descriptorFactory = injector.getInstance(ServiceDescriptorFactory.class);
        List<ServiceDescriptor> descs = new LinkedList<ServiceDescriptor>();
        for (File file : configs) {
            Collection<? extends ServiceDescriptor> e = descriptorFactory.create(file);
            if (e == null)
                throw new IllegalArgumentException("Unrecognized file " + file);
            descs.addAll(e);
        }

        instantiateServices(EmptyConfiguration.INSTANCE, descs);
        log.debug("*** Sorcersoft.com SORCER started ***");
    }

    protected File getRioHome() {
        String rioHomePath = System.getProperty(E_RIO_HOME, System.getenv(E_RIO_HOME));
        if (rioHomePath == null)
            throw new IllegalStateException("No RIO_HOME defined, no platform");
        return new File(rioHomePath);
    }

    public void stop() {
        log.debug("*** Stopping Sorcersoft.com SORCER ***");

        bootInterrupted = true;
        Service service;
        while ((service = services.pollLast()) != null)
            stop(service);

        log.info("******* Sorcersoft.com SORCER stopped *******");
        exitSorcer();
    }

    private void stop(Service service) {
        if (service.destroyer != null) {
            log.info("Stopping {}", service.impl);
            service.destroyer.destroy();
        } else {
            log.debug("Unable to stop {}", service.impl);
        }
    }

    private void exitSorcer() {
        exitMonitor.unregister(this);
    }

    @Override
    public boolean unregister(Object impl) {
        List<Service> copy;
        synchronized (services) {
            copy = new ArrayList<Service>(services);
        }
        boolean result = false;
        for (Service service : copy) {
            if (service.impl == impl) {
                synchronized (services) {
                    services.remove(service);
                }
                result = true;
            }
        }
        if (result) {
            boolean exit;
            synchronized (services) {
                if (log.isInfoEnabled())
                    log.debug("Service count: {}", services.size());
                exit = services.isEmpty();
            }
            if (exit) {
                log.info("No services left; shutting down SORCER");
                exitSorcer();
            }
        }
        return result;
    }

    private static class ServiceStatHolder {
        public int started;
        public int erred;
        public int all;
    }

    protected void instantiateServices(final Configuration config, final Collection<ServiceDescriptor> descriptors) throws ConfigurationException {
        ClassLoaders.doWith(platformLoader.getClassLoader(), new ClassLoaders.Callable<Void, ConfigurationException>() {
            @Override
            public Void call() throws ConfigurationException {
                instantiateServices0(config, descriptors);
                return null;
            }
        });
    }

    /**
     * Create a service for each ServiceDescriptor in the map
     */
    public void instantiateServices0(Configuration config, Collection<ServiceDescriptor> descriptors) throws ConfigurationException {
        Binding<Set<ServiceDescriptorProcessor>> existingBinding = injector.getExistingBinding(Key.get(new TypeLiteral<Set<ServiceDescriptorProcessor>>() {
        }));
        Set<ServiceDescriptorProcessor> processors = null;
        if (existingBinding != null)
            processors = existingBinding.getProvider().get();

        ServiceStatHolder stat = new ServiceStatHolder();
        stat.all = descriptors.size();
        ServiceDescriptor[] descs = descriptors.toArray(new ServiceDescriptor[descriptors.size()]);

        LoginContext loginContext = (LoginContext)
                config.getEntry(SorcerConstants.START_PACKAGE, "loginContext",
                        LoginContext.class, null);
        if (loginContext != null)
            try {
                createWithLogin(descs, config, loginContext, stat, processors);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Error creating service with login context {}", loginContext, e);
            }
        else
            create(descs, config, stat, processors);
    }

    /**
     * Generic service creation method that attempts to start the
     * services defined by the provided <code>ServiceDescriptor[]</code>
     * argument.
     *
     * @param descs  The <code>ServiceDescriptor[]</code> that contains
     *               the descriptors for the services to start.
     * @param config The associated <code>Configuration</code> object
     *               used to customize the service creation process.
     * @see com.sun.jini.start.ServiceStarter.Result
     * @see com.sun.jini.start.ServiceDescriptor
     * @see net.jini.config.Configuration
     */
    public void create(ServiceDescriptor[] descs, Configuration config, ServiceStatHolder stat, Set<ServiceDescriptorProcessor> processors) {
        for (ServiceDescriptor desc : descs) {
            if (bootInterrupted)
                break;
            if (desc == null)
                continue;

            injector.injectMembers(desc);
            log.info("Creating service from {}", desc);

            if (processors != null)
                for (ServiceDescriptorProcessor processor : processors) {
                    processor.process(desc);
                }

            Service service;
            try {
                if (desc instanceof AbstractServiceDescriptor) {
                    ((AbstractServiceDescriptor) desc).addLifeCycle(this);
                    service = (Service) desc.create(config);
                } else if (desc instanceof RioServiceDescriptor) {
                    log.info("Starting RIO service");
                    RioServiceDescriptor.Created created = (RioServiceDescriptor.Created) desc.create(config);
                    service = new Service(created.impl, created.proxy, desc);
                } else {
                    log.info("Starting UNKNOWN service");
                    service = new Service(desc.create(config), null, desc);
                }
                ServiceDestroyer destroyer = getDestroyer(service.impl);
                log.debug("Service destroyer for {} => {}", service.impl, destroyer);
                if (destroyer == null)
                    nonDestroyServices.add(service);
                else {
                    service.destroyer = destroyer;
                    services.add(service);
                }
                ++stat.started;
            } catch (Exception e) {
                log.warn("Error while creating a service from {}", desc, e);
                ++stat.erred;
            } finally {
                log.info("Started {}/{} services; {} errors", stat.started, stat.all, stat.erred);
            }
        }
    }

    /**
     * Generic service creation method that attempts to login via
     * the provided <code>LoginContext</code> and then call the
     * <code>create</code> overload without a login context argument.
     *
     * @param descs        The <code>ServiceDescriptor[]</code> that contains
     *                     the descriptors for the services to start.
     * @param config       The associated <code>Configuration</code> object
     *                     used to customize the service creation process.
     * @param loginContext The associated <code>LoginContext</code> object
     *                     used to login/logout.
     * @throws LoginException If there was a problem logging in/out or
     *                        a problem creating the service.
     * @see com.sun.jini.start.ServiceStarter.Result
     * @see com.sun.jini.start.ServiceDescriptor
     * @see net.jini.config.Configuration
     * @see javax.security.auth.login.LoginContext
     */
    private void createWithLogin(
            final ServiceDescriptor[] descs, final Configuration config,
            final LoginContext loginContext, final ServiceStatHolder stat, final Set<ServiceDescriptorProcessor> processors)
            throws LoginException {
        loginContext.login();

        try {
            Subject.doAsPrivileged(
                    loginContext.getSubject(),
                    new PrivilegedExceptionAction() {
                        public Object run() {
                            create(descs, config, stat, processors);
                            return null;
                        }
                    },
                    null
            );
        } catch (PrivilegedActionException pae) {
            try {
                throw pae.getException();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                loginContext.logout();
            } catch (LoginException le) {
                log.warn("service.logout.exception", le);
            }
        }
    }
}
