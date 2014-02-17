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
import sorcer.boot.ServiceStarter;
import sorcer.installer.Installer;
import sorcer.resolver.Resolver;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ThreadFactory;

import static sorcer.core.SorcerConstants.*;
import static sorcer.util.Collections.i;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher extends Launcher {
    final private static Logger log = LoggerFactory.getLogger(SorcerLauncher.class);

    private ThreadFactory threadFactory;
    private ServiceStarter serviceStarter;
    protected Profile profile;

    @Override
    public void preConfigure() {
        if (serviceStarter != null)
            throw new IllegalStateException("This instance has already created an object");

        super.preConfigure();

        try {
            Installer installer = new Installer();
            if (installer.isInstallRequired(logDir))
                installer.install();
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new RuntimeException("Error while installing SORCER", x);
        }

        configure();
    }

    public void start() throws IOException {
        SorcerRunnable sorcerRun = new SorcerRunnable(getConfigs());

        SorcerShutdownHook.instance.add(this);

        if (threadFactory != null) {
            threadFactory.newThread(sorcerRun).start();
        } else {
            sorcerRun.run();
        }
    }

    protected void configure() {
        updateMonitorConfig();

        //TODO RKR check grant
        Properties defaults = new Properties();
        defaults.putAll(properties);

        Properties overrides = new Properties(defaults);
        overrides.putAll(System.getProperties());

        if (log.isDebugEnabled())
            for (Object key : i(overrides.propertyNames()))
                log.debug("{} = {}", key, overrides.getProperty((String) key));


        System.setProperties(overrides);
    }

    private void updateMonitorConfig() {
        if (profile == null) {
            return;
        }
        String[] monitorConfigPaths = profile.getMonitorConfigPaths();
        if (monitorConfigPaths != null && monitorConfigPaths.length != 0) {
            List<String> paths = new ArrayList<String>(monitorConfigPaths.length);
            for (String path : monitorConfigPaths) {
                path = evaluator.eval(path);
                if (new File(path).exists())
                    paths.add(path);
            }
            properties.put(P_MONITOR_INITIAL_OPSTRINGS, StringUtils.join(paths, File.pathSeparator));
        }
    }

    public static boolean checkEnvironment() throws MalformedURLException {
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
        // use logger, we won't be able to start in direct mode anyway
        for (URL entry : requiredClassPath)
            log.warn("Missing required ClassPath element {}", entry);
        return requiredClassPath.isEmpty();
    }

    protected List<String> getConfigs() {
        ArrayList<String> result = new ArrayList<String>();
        if (profile != null) {
            String[] sorcerConfigPaths = profile.getSorcerConfigPaths();
            result.ensureCapacity(sorcerConfigPaths.length + configs.size());

            for (String cfg : sorcerConfigPaths) {
                String path = evaluator.eval(cfg);
                if (new File(path).exists())
                    result.add(path);
            }
        }
        if (!configs.isEmpty())
            result.addAll(configs);
        return result;
    }

    @Override
    protected Map<String, String> getEnvironment() {
        return System.getenv();
    }

    @Override
    public void setProfile(String profileName) {
        try {
            if (profileName.endsWith(".xml"))
                profile = Profile.load(new File(profileName).toURI().toURL());
            else
                profile = Profile.loadBuiltin(profileName);
        } catch (IOException x) {
            throw new IllegalArgumentException("Could not load profile " + profileName);
        }
    }

    public static void installSecurityManager() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    public static void installLogging() {
        //redirect java.util.logging to slf4j/logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManagementHelper.setup();
    }

    private class SorcerRunnable implements Runnable {
        private List<String> configs;

        public SorcerRunnable(List<String> configs) {
            this.configs = configs;
        }

        @Override
        public void run() {
            try {
                serviceStarter = new ServiceStarter();
                serviceStarter.start(configs);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error while starting SORCER", e);
            }
        }
    }

    @Override
    public void stop() {
        if (serviceStarter == null)
            return;
        serviceStarter.stop();
        serviceStarter = null;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

}
