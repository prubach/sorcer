package sorcer.provider.boot;
/**
 *
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


import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.ServiceDescriptor;
import net.jini.config.Configuration;
import org.rioproject.config.PlatformCapabilityConfig;
import org.rioproject.config.PlatformLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.ServiceDestroyer;
import sorcer.boot.load.Activator;
import sorcer.boot.util.LifeCycleMultiplexer;
import sorcer.core.SorcerConstants;
import sorcer.util.ClassPath;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
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
    protected LifeCycle lifeCycle;

    private static Logger logger = LoggerFactory.getLogger(AbstractServiceDescriptor.class);

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
    public Service create(Configuration config) throws Exception {
        try {
            logger.info("Creating service from {}", this);
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

    protected abstract Service doCreate(Configuration config) throws Exception;

    protected CommonClassLoader getCommonClassLoader(Configuration config) throws Exception {
    /* Set common JARs to the CommonClassLoader */
        String defaultDir = null;
        String rioHome = System.getProperty(SorcerConstants.E_RIO_HOME);
        if (rioHome != null) {
            defaultDir = new File(rioHome, "config/platform").getPath();
        } else {
            logger.warn("No RIO_HOME defined, no default platformDir");
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
    public static class Service {
        /**
         * The reference to the proxy of the created service
         */
        public final Object proxy;
        /**
         * The reference to the implementation of the created service
         */
        public final Object impl;

        public final ServiceDescriptor descriptor;

        public Exception exception;

        public ServiceDestroyer destroyer;
        /**
         * Constructs an instance of this class.
         *
         * @param impl  reference to the implementation of the created service
         * @param proxy reference to the proxy of the created service
         * @param descriptor    service descriptor of the service
         */
        public Service(Object impl, Object proxy, ServiceDescriptor descriptor) {
            this.proxy = proxy;
            this.impl = impl;
            this.descriptor = descriptor;
        }

        public Service(Object impl, Object proxy, ServiceDescriptor descriptor, Exception exception) {
            this(impl, proxy, descriptor);
            this.exception = exception;
        }
    }

    public synchronized void addLifeCycle(LifeCycle lifeCycle) {
        if(this.lifeCycle==null)
            this.lifeCycle = lifeCycle;
        else{
            if(this.lifeCycle instanceof LifeCycleMultiplexer)
                ((LifeCycleMultiplexer) this.lifeCycle).add(lifeCycle);
            else
                this.lifeCycle=new LifeCycleMultiplexer(new HashSet<LifeCycle>(Arrays.asList(this.lifeCycle, lifeCycle)));
        }
    }
}
