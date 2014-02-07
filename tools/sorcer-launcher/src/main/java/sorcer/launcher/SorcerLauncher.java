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

package sorcer.launcher;

import org.rioproject.start.LogManagementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.core.SorcerConstants;
import sorcer.resolver.Resolver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ThreadFactory;

import static sorcer.util.Collections.i;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher extends Launcher {
    private final static Logger log = LoggerFactory.getLogger(SorcerLauncher.class);
    private ThreadFactory threadFactory;

    public static boolean checkEnvironment() throws MalformedURLException {
        String[] requiredEnv = {
//                SorcerConstants.E_RIO_HOME,
                SorcerConstants.E_SORCER_HOME
        };
        for (String key : requiredEnv) {
            if (System.getenv(key) == null)
                return false;
        }

        //we can't simply create another AppClassLoader,
        //because rio CommonClassLoader enforces SystemClassLoader as parent,
        //so all services started with rio would have parallel CL hierarchy


        List<URL> requiredClassPath = new LinkedList<URL>();
        for (String file : CLASS_PATH) {
            requiredClassPath.add(new File(Resolver.resolveAbsolute(file)).toURI().toURL());
        }
        List<URL> actualClassPath = new ArrayList<URL>();
        for (ClassLoader cl = SorcerLauncher.class.getClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                Collections.addAll(actualClassPath, urls);
                Comparator<URL> c = new Comparator<URL>() {
                    @Override
                    public int compare(URL o1, URL o2) {
                        return o1.toExternalForm().compareTo(o2.toExternalForm());
                    }
                };
                Arrays.sort(urls, c);

                List<URL> commonUrls = new LinkedList<URL>();
                for (URL url : requiredClassPath) {
                    int i = Arrays.binarySearch(urls, url, c);
                    if (i >= 0)
                        commonUrls.add(url);
                }
                requiredClassPath.removeAll(commonUrls);
            }
        }
        return requiredClassPath.isEmpty();

    }

    @Override
    public void doStart() throws MalformedURLException {
        //TODO RKR check grant
        Properties defaults = new Properties();
        defaults.putAll(getProperties());

        Properties overrides = new Properties(defaults);
        overrides.putAll(System.getProperties());

        if (log.isDebugEnabled())
            for(Object key : i(overrides.propertyNames()))
                log.debug("{} = {}", key, overrides.getProperty((String) key));


        System.setProperties(overrides);
        installLogging();

        try {
            Class<?> serviceStarter = getClass().getClassLoader().loadClass(MAIN_CLASS);
            Method start = serviceStarter.getDeclaredMethod("start", List.class);

            SorcerRunnable sorcerRun = new SorcerRunnable(start, getConfigs());

            // last moment
            installSecurityManager();

            if (threadFactory != null) {
                threadFactory.newThread(sorcerRun).start();
            } else {
                sorcerRun.run();
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Incompatible sorcer-launcher and sos-boot modules", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Incompatible sorcer-launcher and sos-boot modules", e);
        }
    }

    private static void installSecurityManager() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    private static void installLogging() {
        //redirect java.util.logging to slf4j/logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManagementHelper.setup();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    private static class SorcerRunnable implements Runnable {
        private final Method start;
        private List<String> configs;

        public SorcerRunnable(Method start, List<String> configs) {
            this.start = start;
            this.configs = configs;
        }

        @Override
        public void run() {
            try {
                start.invoke(null, configs);
            } catch (RuntimeException e) {
                throw e;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                if (cause instanceof Error) throw (Error) cause;
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException("Error while starting SORCER", e);
            }
        }
    }
}
