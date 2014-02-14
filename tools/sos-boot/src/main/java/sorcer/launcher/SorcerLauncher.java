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

import org.apache.commons.io.FileUtils;
import org.rioproject.start.LogManagementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.boot.ServiceStarter;
import sorcer.installer.Installer;
import sorcer.resolver.Resolver;
import sorcer.util.JavaSystemProperties;
import sorcer.util.StringUtils;
import sorcer.util.eval.PropertyEvaluator;

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

    public void start() throws Exception {
        ensureDirConfig();

        //needed by Resolver to read repoRoot from sorcer.env
        JavaSystemProperties.ensure(SORCER_HOME, home.getPath());
        JavaSystemProperties.ensure(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());

        if (sorcerListener == null)
            sorcerListener = new NullSorcerListener();

        environment = getEnvironment();
        properties = getProperties();
        evaluator = new PropertyEvaluator();
        evaluator.addSource("sys", properties);
        evaluator.addSource("env", environment);

        updateMonitorConfig();

        configure();

        doStart();
    }

    protected void ensureDirConfig() {
        if (home == null)
            home = sorcer.util.FileUtils.getDir(System.getenv(E_SORCER_HOME));
        if (home == null)
            home = sorcer.util.FileUtils.getDir(System.getProperty(SORCER_HOME));
        if (home == null)
            throw new IllegalStateException("No SORCER home defined");

        String envSorcerExt = System.getenv(E_SORCER_EXT);
        if (envSorcerExt != null)
            setExt(new File(envSorcerExt));

        String envRioHome = System.getenv(E_RIO_HOME);
        if (envRioHome != null)
            setRio(new File(envRioHome));

        if (ext == null)
            ext = home;

        if (configDir == null)
            configDir = new File(home, "configs");

        if (rio == null)
            rio = new File(home, "lib/rio");

        if (logDir == null)
            logDir = new File(home, "logs");
    }

    private void updateMonitorConfig() {
        if (profile != null) {
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

    @Override
    protected void configure() throws IOException {
        if (!logDir.exists())
            FileUtils.forceMkdir(logDir);

        //TODO RKR check grant
        Properties defaults = new Properties();
        defaults.putAll(properties);

        Properties overrides = new Properties(defaults);
        overrides.putAll(System.getProperties());

        if (log.isDebugEnabled())
            for (Object key : i(overrides.propertyNames()))
                log.debug("{} = {}", key, overrides.getProperty((String) key));


        System.setProperties(overrides);
        //installLogging();
    }

    @Override
    protected void doStart() throws IOException {
        Installer installer = new Installer();
        if (installer.isInstallRequired(logDir))
            installer.install();

        SorcerRunnable sorcerRun = new SorcerRunnable(getConfigs());

        // last moment
        installSecurityManager();

        SorcerShutdownHook.instance.add(this);

        if (threadFactory != null) {
            threadFactory.newThread(sorcerRun).start();
        } else {
            sorcerRun.run();
        }
    }

    @Override
    protected Map<String, String> getEnvironment() {
        return System.getenv();
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
            throw new IllegalStateException("Sorcer not running");
        serviceStarter.stop();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

}
