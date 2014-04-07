/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.boot.platform;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jini.start.ServiceDescriptor;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.jini.config.EmptyConfiguration;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.rioproject.config.PlatformCapabilityConfig;
import org.rioproject.loader.CommonClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.load.Activator;
import sorcer.core.ServiceActivator;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.util.ClassPath;
import sorcer.util.InjectionHelper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class PlatformLoader {
    private static Logger logger = LoggerFactory.getLogger(PlatformLoader.class);

    private org.rioproject.config.PlatformLoader platformLoader = new org.rioproject.config.PlatformLoader();
    private Activator activator;
    private Injector injector;

    private File platformRoot;
    private File servicePlatformRoot;
    private final List<Module> modules = new LinkedList<Module>();
    protected Injector platformInjector;
    private CommonClassLoader platformClassLoader;

    public PlatformLoader(Injector injector, File platformRoot, File servicePlatformRoot) {
        this.platformRoot = platformRoot;
        this.servicePlatformRoot = servicePlatformRoot;
        this.injector = injector;
        activator = new Activator();
        activator.setInjector(new InjectionHelper.Injector() {
            @Override
            public <T> T create(Class<T> c) {
                return PlatformLoader.this.injector.getInstance(c);
            }

            @Override
            public void injectMembers(Object target) {
            }
        });
        activator.entryHandlers.add(0, activator.new EntryHandlerEntry("Sorcer-Activation-Module", activator.new AbstractClassEntryHandler() {
            public void handle(Object instance) {
                if (instance instanceof Module)
                    modules.add((Module) instance);
            }
        }));
    }

    public Injector getInjector() {
        return platformInjector;
    }

    public ClassLoader getClassLoader() {
        return platformClassLoader;
    }

    public void create() {
        platformClassLoader = CommonClassLoader.getInstance();
        loadPlatform(platformRoot, platformClassLoader);

        Thread current = Thread.currentThread();
        ClassLoader original = current.getContextClassLoader();
        current.setContextClassLoader(platformClassLoader);
        try {
            List<ServiceActivator> activators = loadPlatformServices(servicePlatformRoot);
            modules.add(new PlatformModule());

            platformInjector = injector.createChildInjector(modules);
            InjectionHelper.setInstance(new PlatformInjector(platformInjector));

            activate(activators);

        } finally {
            current.setContextClassLoader(original);
        }
    }

    private void activate(List<ServiceActivator> activators) {
        for (ServiceActivator activator : activators)
            try {
                activator.activate();
            } catch (Exception e) {
                logger.warn("Error during platform service activation", e);
            }
    }

    private static class PlatformInjector implements InjectionHelper.Injector {
        private Injector platformInjector;

        private PlatformInjector(Injector platformInjector) {
            this.platformInjector = platformInjector;
        }

        @Override
        public void injectMembers(Object target) {
            platformInjector.injectMembers(target);
        }

        @Override
        public <T> T create(Class<T> type) {
            return platformInjector.getInstance(type);
        }
    }

    private List<ServiceActivator> loadPlatformServices(File dir) {
        if (dir == null)
            throw new IllegalArgumentException("directory is null");
        if (!dir.exists())
            throw new IllegalArgumentException("Platform path " + dir + " does not exist");
        if (!dir.isDirectory())
            throw new IllegalArgumentException("Platform path " + dir + " is not a directory");
        if (!dir.canRead())
            throw new IllegalArgumentException("No read permissions for platform directory " + dir + " is not a directory");

        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass(PlatformDescriptor.class.getName());
        GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), new Binding(), cfg);

        File[] files = dir.listFiles();
        Arrays.sort(files);
        List<ServiceActivator> activators = new LinkedList<ServiceActivator>();
        for (File file : files) {
            if (!file.getName().endsWith("groovy")) {
                logger.debug("Ignoring {}", file);
                continue;
            }

            try {
                PlatformDescriptor platform = (PlatformDescriptor) shell.parse(file);
                for (ServiceDescriptor descriptor : platform.getPlatformServices()) {
                    injector.injectMembers(descriptor);
                    Object service = descriptor.create(EmptyConfiguration.INSTANCE);
                    if (service instanceof AbstractServiceDescriptor.Service) {
                        AbstractServiceDescriptor.Service srvc = (AbstractServiceDescriptor.Service) service;
                        if (srvc.exception != null) {
                            logger.warn("Error while creating platform service {}", descriptor, srvc.exception);
                            continue;
                        } else
                            service = srvc.impl;
                    }
                    if (service instanceof Module)
                        modules.add((Module) service);
                    if (service instanceof ServiceActivator)
                        activators.add((ServiceActivator) service);
                }
            } catch (Exception e) {
                Throwable t = e.getCause() == null ? e : e.getCause();
                throw new IllegalStateException("Could not load platform: The " + file + " file is in error.", t);
            }
        }
        return activators;
    }

    protected void loadPlatform(File platformDir, CommonClassLoader commonCL) {
        List<URL> urlList = new LinkedList<URL>();

        try {
            PlatformCapabilityConfig[] caps = platformLoader.parsePlatform(platformDir.getPath());

            logger.debug("Capabilities: {}", caps);
            for (PlatformCapabilityConfig cap : caps) {
                if (cap.getCommon()) {
                    for (URL url : cap.getClasspathURLs()) {
                        if (!ClassPath.contains(commonCL.getParent(), url))
                            urlList.add(url);
                    }
                }
            }

            logger.debug("commonJARs = {}", urlList);
            URL[] commonJARs = urlList.toArray(new URL[urlList.size()]);

            commonCL.addCommonJARs(commonJARs);

            activate(commonCL, commonJARs);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during platform build", e);
        }
    }

    private void activate(CommonClassLoader commonCL, URL[] commonJARs) throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(commonCL);
            activator.activate(commonJARs);
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }
}
