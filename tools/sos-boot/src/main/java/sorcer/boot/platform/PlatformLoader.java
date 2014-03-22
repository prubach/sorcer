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
import com.sun.jini.start.ServiceDescriptor;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.jini.config.EmptyConfiguration;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.rioproject.config.PlatformCapabilityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.load.Activator;
import sorcer.provider.boot.CommonClassLoader;
import sorcer.tools.ActivationProcessor;
import sorcer.util.ClassPath;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class PlatformLoader implements Provider<ClassLoader> {
    private static Logger logger = LoggerFactory.getLogger(PlatformLoader.class);

    private org.rioproject.config.PlatformLoader platformLoader = new org.rioproject.config.PlatformLoader();

    private Activator activator;

    @Inject
    protected Injector injector;

    private File platformRoot;
    private File servicePlatformRoot;

    public PlatformLoader(File platformRoot, File servicePlatformRoot) {
        //this.injector = injector;
        this.platformRoot = platformRoot;
        this.servicePlatformRoot = servicePlatformRoot;
        activator = new Activator();
        activator.setActivationProcessor(new ActivationProcessor() {
            @Override
            public void process(Object o) {
                PlatformLoader.this.injector.injectMembers(o);
            }
        });
    }

    public ClassLoader get() {
        CommonClassLoader commonCL = CommonClassLoader.getInstance();
        loadPlatform(platformRoot, commonCL);

        loadPlatformServices(servicePlatformRoot, commonCL);

        return commonCL;
    }

    private void loadPlatformServices(File dir, ClassLoader parent) {
        if (dir == null)
            throw new IllegalArgumentException("directory is null");
        if (!dir.exists()) {
            logger.warn("Platform directory [{}] not found", dir);
            return;
        }
        if (!dir.isDirectory()) {
            logger.warn("Platform directory [{}] is not a directory", dir);
            return;
        }
        if (!dir.canRead()) {
            logger.warn("No read permissions for platform directory [{}]", dir);
            return;
        }

        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass(PlatformDescriptor.class.getName());
        GroovyShell shell = new GroovyShell(parent, new Binding(), cfg);

        File[] files = dir.listFiles();
        Arrays.sort(files);
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
                    injector.injectMembers(service);
                }
            } catch (Exception e) {
                Throwable t = e.getCause() == null ? e : e.getCause();
                throw new IllegalStateException("Could not load platform: The " + file + " file is in error.", t);
            }
        }

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
