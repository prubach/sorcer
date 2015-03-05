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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;
import sorcer.util.*;
import sorcer.util.eval.PropertyEvaluator;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Collections;

import static sorcer.core.SorcerConstants.*;
import static sorcer.util.Collections.i;
import static sorcer.util.JavaSystemProperties.*;

/**
 * @author Rafał Krupiński
 */
public abstract class Launcher implements ILauncher {
    final protected Logger log = LoggerFactory.getLogger(getClass());
    protected File home;
    protected File ext;
    protected File rio;
    protected File logDir;
    protected File configDir;
    protected File workingDir;
    protected List<String> configs = Collections.emptyList();

    /**
     * list of configuration files for the Rio Monitor (opstrings)
     */
    protected List<String> rioConfigs;

    private Set<SorcerListener> sorcerListeners = new HashSet<SorcerListener>();
    protected SorcerListener sorcerListener = new SorcerListenerMultiplexer(sorcerListeners);

    protected Properties properties;
    protected Properties environment;

    final protected static String[] CLASS_PATH = {
            "org.apache.river:start",
            "net.jini:jsk-resources",
            "net.jini:jsk-platform",
            "net.jini:jsk-lib",
            "net.jini.lookup:serviceui",

            "org.rioproject:rio-start",
            "org.rioproject:rio-platform",
            "org.rioproject:rio-logging-support",
            "org.rioproject:rio-lib",
            "org.rioproject:rio-api",
            "org.rioproject:rio-proxy",

            "org.sorcersoft.sorcer:sos-boot",
            "org.sorcersoft.sorcer:sorcer-api",
            "org.sorcersoft.sorcer:sorcer-spi",
            "org.sorcersoft.sorcer:sorcer-container",
            "org.sorcersoft.sorcer:sorcer-launcher",
            "org.sorcersoft.sorcer:sorcer-resolver",
            "org.sorcersoft.sorcer:sorcer-rio-start",
            "org.sorcersoft.sorcer:sorcer-rio-resolver",
            "org.sorcersoft.sorcer:sorcer-loader",
            "org.sorcersoft.sorcer:sos-util",

            // optional: required only in fork mode
            //"org.sorcersoft.sorcer:sorcer-launcher",

            "org.codehaus.groovy:groovy-all",
            "org.apache.commons:commons-lang3",
            "com.google.guava:guava",
            "com.google.inject:guice:4.0-beta4",
            "com.google.inject.extensions:guice-multibindings:4.0-beta4",
            "aopalliance:aopalliance:1.0",
            "javax.inject:javax.inject:1",
            "commons-io:commons-io",
            "commons-cli:commons-cli",
            "org.sorcersoft.sigar:sigar:1.6.4-3",

            "org.slf4j:slf4j-api",
            "org.slf4j:jul-to-slf4j",
            "ch.qos.logback:logback-core",
            "ch.qos.logback:logback-classic"
    };

    protected PropertyEvaluator evaluator;

    @Override
    @SuppressWarnings("unchecked")
    public void preConfigure() {
        ensureDirConfig();

        //needed by Resolver to read repoRoot from sorcer.env
        JavaSystemProperties.ensure(SORCER_HOME, home.getPath());
        JavaSystemProperties.ensure(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());


        // Default JNA library path

        String nativeLibPath = "";
        if (GenericUtil.isWindows64())
            nativeLibPath = "/native/lib/amd64-winnt";
        else if (GenericUtil.isWindows32())
            nativeLibPath = "/native/lib/x86-winnt";
        else if (GenericUtil.isMac())
            nativeLibPath = "/native/lib/universal64-macosx";
        else if (GenericUtil.isLinux64())
            nativeLibPath = "/native/lib/amd64-linux";
        else if (GenericUtil.isLinux32())
            nativeLibPath = "/native/lib/x86-linux";
        else
            System.out.println("OS Architecture not detected, jna.library.path not set!");

        LibraryPathHelper.getLibraryPath().add(home.getPath() + nativeLibPath);

        environment = getEnvironment();
        properties = getProperties();
        evaluator = new PropertyEvaluator();
        evaluator.addSource("sys", properties);
        evaluator.addSource("env", environment);
    }

    abstract protected Properties getEnvironment();

    protected void ensureDirConfig() {
        if (home == null)
            home = FileUtils.getDir(System.getenv(E_SORCER_HOME));
        if (home == null)
            home = FileUtils.getDir(System.getProperty(SORCER_HOME));
        if (home == null)
            throw new IllegalStateException("No SORCER home defined");

        if (ext == null)
            ext = FileUtils.getDir(System.getProperty(S_SORCER_EXT, System.getenv(E_SORCER_EXT)));
        if (ext == null)
            ext = home;

        if (rio == null)
            // note that rio home system property is named just as the env variable - RIO_HOME
            rio = FileUtils.getDir(System.getProperty(E_RIO_HOME, System.getenv(E_RIO_HOME)));
        if (rio == null)
            rio = new File(home, "rio-" + SorcerConstants.RIO_VERSION);

        if (configDir == null)
            configDir = new File(home, "configs");

        if (logDir == null)
            logDir = FileUtils.getDir(System.getenv("RIO_LOG_DIR"));
        if (logDir == null)
            logDir = new File(home, "logs");

        try {
            org.apache.commons.io.FileUtils.forceMkdir(logDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create logs directory", e);
        }
    }

    /**
     * Create a new set of system properties using the result of getDefaultProperties() as he defaults
     * and the system properties as overrides
     * @return Properties to be used as a system properties of the new sorcer
     */
    protected Properties getProperties() {
        Properties overrides = new Properties(getDefaultProperties());
        overrides.putAll(System.getProperties());
        if (properties!=null)
            overrides.putAll(properties);
        if (log.isDebugEnabled())
            for (Object key : i(overrides.propertyNames()))
                log.debug("{} = {}", key, overrides.getProperty((String) key));
        return overrides;
    }

    protected Properties getDefaultProperties() {
        File policyPath = new File(configDir, "sorcer.policy");
        String resolverPath = Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether:" + RIO_VERSION);

        try {
            IOUtils.ensureFile(policyPath, IOUtils.FileCheck.readable);
            IOUtils.ensureFile(new File(resolverPath), IOUtils.FileCheck.readable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Properties sysProps = new Properties();
        //system
        sysProps.put(MAX_DATAGRAM_SOCKETS, "1024");
        sysProps.put(PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.url|org.rioproject.url");
        sysProps.put(RMI_SERVER_CLASS_LOADER, "sorcer.rio.rmi.SorcerResolvingLoader");
        sysProps.put(RMI_SERVER_USE_CODEBASE_ONLY, Boolean.FALSE.toString());
        sysProps.put(SECURITY_POLICY, policyPath.getPath());
        sysProps.put(UTIL_LOGGING_CONFIG_FILE, new File(configDir, "sorcer.logging").getPath());
        sysProps.put(S_SORCER_EXT, ext.getPath());
        if (System.getenv(E_ENG_HOME)!=null) sysProps.put(S_ENG_HOME, System.getenv(E_ENG_HOME));

        try {
            sysProps.put(RMI_SERVER_HOSTNAME, SorcerEnv.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Could not obtain system IP address", e);
        }

        //sorcer
        sysProps.put(SORCER_HOME, home.getPath());
        //sysProps.put(S_WEBSTER_TMP_DIR, System.getProperty(S_WEBSTER_TMP_DIR, new File(home, "data").getPath()));
        sysProps.put(S_WEBSTER_TMP_DIR, new File(home, "data").getPath());
        sysProps.put(S_WEBSTER_PUT_DIR, new File(home, "data").getPath());
        sysProps.put(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());
        //sysProps.put(S_WEBSTER_INTERFACE, getInetAddress());

        //rio
        sysProps.put("org.rioproject.service", "all");
        sysProps.put("RIO_HOME", rio.getPath());
        sysProps.put("rio.home", rio.getPath());
        sysProps.put("RIO_LOG_DIR", logDir.getPath());
        sysProps.put("org.rioproject.resolver.jar", resolverPath);

        //other
        sysProps.put("logback.configurationFile", new File(configDir, "logback.groovy").getPath());

        return sysProps;
    }

    protected Collection<String> getClassPath() {
        Set<String> result = new HashSet<String>(CLASS_PATH.length);
        try {
            for (String artifact : CLASS_PATH) {
                String p = Resolver.resolveAbsolute(artifact);
                IOUtils.ensureFile(new File(p));
                result.add(p);
            }
            result.add(new File(System.getProperty("java.home"), "lib/tools.jar").getPath());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    @Override
    public void setHome(File home) {
        this.home = home;
    }

    @Override
    public void setConfigs(List<String> configs) {
        this.configs = configs;
    }

    @Override
    public void setConfigDir(File config) {
        this.configDir = config;
    }

    @Override
    public void addSorcerListener(SorcerListener listener) {
        this.sorcerListeners.add(listener);
    }

    @Override
    public void removeSorcerListener(SorcerListener listener) {
        this.sorcerListeners.remove(listener);
    }

    @Override
    public void setRioConfigs(List<String> rioConfigs) {
        this.rioConfigs = rioConfigs;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public void setCustomSystemProperties(Properties customSystemProperties) {
        if (properties==null)
            properties = new Properties();
        properties.putAll(customSystemProperties);
    }

    private class SorcerListenerMultiplexer implements SorcerListener {
        private Set<SorcerListener> sorcerListeners;

        public SorcerListenerMultiplexer(Set<SorcerListener> sorcerListeners) {
            this.sorcerListeners = sorcerListeners;
        }

        @Override
        public void processLaunched(Process2 process) {
            for (SorcerListener l : sorcerListeners) l.processLaunched(process);
        }

        @Override
        public void sorcerStarted() {
            for (SorcerListener l : sorcerListeners) l.sorcerStarted();
        }

        @Override
        public void sorcerEnded(Exception e) {
            for (SorcerListener l : sorcerListeners) l.sorcerEnded(e);
        }

        @Override
        public void processDown(Process process) {
            for (SorcerListener l : sorcerListeners) l.processDown(process);
        }
    }
}
