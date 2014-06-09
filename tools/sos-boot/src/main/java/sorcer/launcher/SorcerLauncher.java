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

import com.sun.jini.start.LifeCycle;
import org.rioproject.logging.ServiceLogEventHandlerHelper;
import org.rioproject.resolver.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.boot.ServiceStarter;
import sorcer.core.SorcerEnv;
import sorcer.resolver.ArtifactResolver;
import sorcer.resolver.Resolver;
import sorcer.util.ConfigurableThreadFactory;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ThreadFactory;

import static sorcer.core.SorcerConstants.P_MONITOR_INITIAL_OPSTRINGS;
import static sorcer.util.Collections.toProperties;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher extends Launcher {
    final private static Logger log = LoggerFactory.getLogger(SorcerLauncher.class);

    private ThreadFactory threadFactory;
    private static ThreadGroup threadGroup = new ThreadGroup("SORCER");
    private ServiceStarter serviceStarter;
    protected Profile profile;

    private List<ConfigResolver> resolvers = new LinkedList<ConfigResolver>();

    {
        try {
            resolvers.add(new ProjectResolver());
            log.debug("Using Project Resolver");
        } catch (InvocationTargetException e) {
            log.debug("Error", e.getCause());
        } catch (Exception e) {
            log.debug("Error", e);
        }
        try {
            resolvers.add(new OptionalResolver("sorcer.resolver.RepositoryArtifactResolver", new Class[]{String.class}, SorcerEnv.getRepoDir()));
            log.debug("Using Repository Resolver");
        } catch (InvocationTargetException e) {
            log.debug("Error", e.getCause());
        } catch (Exception e) {
            log.debug("Error", e);
        }
        resolvers.add(new ConfigResolver() {
            @Override
            public File resolve(String config) {
                return new File(config);
            }
        });
    }

    @Override
    public void preConfigure() {
        if (serviceStarter != null)
            throw new IllegalStateException("This instance has already created an object");

        super.preConfigure();
        updateMonitorConfig();
        configure();
        configureThreadFactory();
    }

    private void configureThreadFactory() {
        if (threadFactory == null)
            threadFactory = getDefaultThreadFactory();
    }

    public static ThreadFactory getDefaultThreadFactory() {
        ConfigurableThreadFactory tf = new ConfigurableThreadFactory();
        tf.setNameFormat("SORCER");
        tf.setThreadGroup(threadGroup);
        return tf;
    }

    public void start() {
        SorcerShutdownHook.instance.add(this);

        Thread thread = threadFactory.newThread(new SorcerRunnable(getConfigs()));
        thread.start();
    }

    protected void configure() {
        //TODO RKR check grant
        System.setProperties(properties);
    }

    private void updateMonitorConfig() {
        if (profile != null && profile.getMonitorConfigPaths() != null)
            if (rioConfigs == null)
                rioConfigs = Arrays.asList(profile.getMonitorConfigPaths());
            else
                Collections.addAll(rioConfigs, profile.getMonitorConfigPaths());

        if (rioConfigs == null || rioConfigs.isEmpty())
            return;

        List<String> paths = new ArrayList<String>(rioConfigs.size());
        for (String path : rioConfigs) {
            path = evaluator.eval(path);
            if (new File(path).exists())
                paths.add(path);
        }
        properties.put(P_MONITOR_INITIAL_OPSTRINGS, StringUtils.join(paths, File.pathSeparator));
    }

    public static boolean checkEnvironment() throws MalformedURLException {
        //we can't simply create another AppClassLoader,
        //because rio CommonClassLoader enforces SystemClassLoader as parent,
        //so all services started with rio would have parallel CL hierarchy

        List<URL> requiredClassPath = new LinkedList<URL>();
        for (String file : CLASS_PATH) {
            requiredClassPath.add(new File(Resolver.resolveAbsolute(file)).toURI().toURL());
        }
        List<URL> actualClassPath = getClassPath(SorcerLauncher.class.getClassLoader());
        List<URL> missingClassPath = new LinkedList<URL>(requiredClassPath);
        missingClassPath.removeAll(actualClassPath);

        // use logger, we won't be able to start in direct mode anyway
        for (URL entry : missingClassPath)
            log.warn("Missing required ClassPath element {}", entry);

        return missingClassPath.isEmpty();
    }

    static List<URL> getClassPath(ClassLoader classLoader) {
        List<URL> result = new ArrayList<URL>();
        ClassLoader cl = classLoader;
        do {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                Collections.addAll(result, urls);
            }
        } while ((cl = cl.getParent()) != null);
        return result;
    }

    protected List<File> getConfigs() {
        List<File> result = new ArrayList<File>(configs.size());
        for (String path : configs)
            for (ConfigResolver resolver : resolvers) {
                File file = resolver.resolve(path);
                if (file != null) {
                    result.add(file);
                    break;
                }
            }

        if (profile != null) {
            String[] sorcerConfigPaths = profile.getSorcerConfigPaths();
            for (String cfg : sorcerConfigPaths) {
                String path = evaluator.eval(cfg);
                File file = new File(path);
                if (file.exists())
                    result.add(file);
            }
        }
        return result;
    }

    @Override
    protected Properties getEnvironment() {
        return toProperties(System.getenv());
    }

    @Override
    public void setProfile(String profileName) {
        try {
            if (profileName.endsWith(".xml"))
                profile = Profile.load(new File(profileName).toURI().toURL());
            else
                profile = Profile.loadBuiltin(profileName);
        } catch (IOException x) {
            throw new IllegalArgumentException("Could not load profile " + profileName, x);
        }
    }

    public static void installSecurityManager() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager()/*{
                @Override
                public void checkPermission(Permission perm) {
                    String name = perm.getName();
                    if (name.startsWith("exitVM") && !(hasCaller(ExitingCallback.class) || hasCaller("sorcer.launcher.Sorcer")))
                        throw new SecurityException("Exit forbidden");
                }

                private boolean hasCaller(Class type){
                    return hasCaller(type.getName());
                }

                private boolean hasCaller(String type){
                    for (Class caller : getClassContext()) {
                        if(type.equals(caller.getName()))
                            return true;
                    }
                    return false;
                }
            }*/);
    }

    public static void installLogging() {
        //redirect java.util.logging to slf4j/logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        ServiceLogEventHandlerHelper.addServiceLogEventHandler();
    }

    private class SorcerRunnable implements Runnable {
        private List<File> configs;

        public SorcerRunnable(List<File> configs) {
            this.configs = configs;
        }

        @Override
        public void run() {
            try {
                serviceStarter = new ServiceStarter(new LifeCycle() {
                    private boolean closing;

                    @Override
                    synchronized public boolean unregister(Object o) {
                        if (closing)
                            return false;
                        sorcerListener.sorcerEnded(null);
                        closing = true;
                        return true;
                    }
                });
                serviceStarter.start(configs);
                sorcerListener.sorcerStarted();
            } catch (Exception e) {
                log.error("Error while starting SORCER", e);
                sorcerListener.sorcerEnded(e);
            }
        }
    }

    @Override
    public void stop() {
        if (serviceStarter == null)
            return;
        serviceStarter.stop();
        serviceStarter = null;

/*
        Thread[] threads = new Thread[Thread.activeCount() * 2];
        Thread current = Thread.currentThread();
        threadGroup.enumerate(threads);
        for (Thread t : threads) {
            if (t == null || current == t) continue;
            log.warn("*** Interrupting leaked thread {}", t.getName());
            t.interrupt();
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                log.warn("Interrupted on join()!", e);
            }
        }
*/
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }
}

interface ConfigResolver {
    File resolve(String config);
}

class OptionalResolver implements ConfigResolver {
    protected ArtifactResolver resolver;

    @SuppressWarnings("unchecked")
    protected OptionalResolver(String name, Class[] argTypes, Object... ctorArgs) throws Exception {
        Class<ArtifactResolver> resolverType = (Class<ArtifactResolver>) Class.forName(name);
        resolver = resolverType.getConstructor(argTypes).newInstance(ctorArgs);
    }

    @Override
    public File resolve(String config) {
        if (!Artifact.isArtifact(config))
            return null;
        String pathname = resolver.resolveAbsolute(config);
        if (pathname == null)
            return null;
        return new File(pathname);
    }
}

class ProjectResolver extends OptionalResolver {
    protected ProjectResolver() throws Exception {
        super("sorcer.resolver.ProjectArtifactResolver", new Class[0]);
    }

    @Override
    public File resolve(String config) {
        if (!config.startsWith(":"))
            return null;
        String pathname = resolver.resolveAbsolute(config.substring(1));
        if (pathname == null)
            return null;
        return new File(pathname);
    }
}
