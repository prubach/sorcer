package sorcer.provider.boot;
/**
 *
 * Copyright 2013 Rafał Krupiński.
 * Copyright 2013 Sorcersoft.com S.A.
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


import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.ServiceDescriptor;
import net.jini.config.Configuration;
import org.rioproject.config.PlatformCapabilityConfig;
import org.rioproject.config.PlatformLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.load.Activator;
import sorcer.core.SorcerEnv;
import sorcer.util.ClassPath;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rafał Krupiński
 */
public abstract class AbstractServiceDescriptor implements ServiceDescriptor {
    /**
	 * The parameter types for the "activation constructor".
	 */
	protected static final Class[] actTypes = { String[].class, LifeCycle.class };
    protected static boolean platformLoaded = false;
    protected static AtomicInteger allDescriptors = new AtomicInteger(0);
    protected static AtomicInteger startedServices = new AtomicInteger(0);
    protected static AtomicInteger erredServices = new AtomicInteger(0);
    protected static String COMPONENT = "sorcer.provider.boot";
    private static Activator activator = new Activator();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    {
        allDescriptors.incrementAndGet();
    }

    protected void loadPlatform(Configuration config, String defaultDir, CommonClassLoader commonCL) throws Exception {
        PlatformLoader platformLoader = new PlatformLoader();
        List<URL> urlList = new LinkedList<URL>();

        String platformDir = (String) config.getEntry(COMPONENT, "platformDir",
                String.class, defaultDir);
        logger.debug("Platform dir: {}", platformDir);
        PlatformCapabilityConfig[] caps = platformLoader.parsePlatform(platformDir);

        logger.debug("Capabilities: {}", Arrays.toString(caps));
        for (PlatformCapabilityConfig cap : caps) {
            if (cap.getCommon()) {
                for (URL url : cap.getClasspathURLs()) {
                    if(!ClassPath.contains(commonCL.getParent(), url))
                        urlList.add(url);
                }
            }
        }

        logger.debug("commonJARs = {}", urlList);
        URL[] commonJARs = urlList.toArray(new URL[urlList.size()]);

        commonCL.addCommonJARs(commonJARs);

        activate(commonCL, commonJARs);
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

    /**
     * @see com.sun.jini.start.ServiceDescriptor#create
     */
    public Created create(Configuration config) throws Exception {
        try {
            return doCreate(config);
        } catch (Exception x) {
            erredServices.incrementAndGet();
            logger.error("Error creating service", x);
            throw x;
        } finally {
            int i = startedServices.incrementAndGet();
            logger.info("Started " + i + '/' + allDescriptors.get() + " services; " + erredServices.get() + " errors");
        }
    }

    protected abstract Created doCreate(Configuration config) throws Exception;

    protected CommonClassLoader getCommonClassLoader(Configuration config) throws Exception {
    /* Set common JARs to the CommonClassLoader */
        String defaultDir = null;
        File sorcerHome = SorcerEnv.getHomeDir();
        String rioHome = System.getenv("RIO_HOME");
        if (rioHome == null && sorcerHome != null) {
            rioHome = new File(sorcerHome, "lib/rio").getPath();
        }
        if (rioHome != null) {
            defaultDir = new File(rioHome, "config/platform").getPath();
        } else {
            logger.info("No RIO_HOME nor SORCER_HOME defined, no default platformDir");
        }

        CommonClassLoader commonCL = CommonClassLoader.getInstance();
        // Don't load Platform Class Loader when it was already loaded before
        if (!platformLoaded) {
            loadPlatform(config, defaultDir, commonCL);
            platformLoaded = true;
        }
        return commonCL;
    }

    /**
     * Object returned by
     * {@link sorcer.provider.boot.SorcerServiceDescriptor#create(net.jini.config.Configuration)
     * SorcerServiceDescriptor.create()} method that returns the proxy and
     * implementation references for the created service.
     */
    public static class Created {
        /**
         * The reference to the proxy of the created service
         */
        public final Object proxy;
        /**
         * The reference to the implementation of the created service
         */
        public final Object impl;

        /**
         * Constructs an instance of this class.
         *
         * @param impl  reference to the implementation of the created service
         * @param proxy reference to the proxy of the created service
         */
        public Created(Object impl, Object proxy) {
            this.proxy = proxy;
            this.impl = impl;
        }
    }
}
