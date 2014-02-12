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

import sorcer.installer.Installer;
import sorcer.resolver.Resolver;
import sorcer.util.HostUtil;
import sorcer.util.IOUtils;
import sorcer.util.JavaSystemProperties;
import sorcer.util.StringUtils;
import sorcer.util.eval.PropertyEvaluator;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static sorcer.core.SorcerConstants.*;
import static sorcer.util.JavaSystemProperties.*;

/**
 * @author Rafał Krupiński
 */
public abstract class Launcher {
    final protected static String MAIN_CLASS = "sorcer.boot.ServiceStarter";
    public WaitMode waitMode = WaitMode.no;
    protected File home;
    protected File ext;
    protected File rio;
    protected File logDir;
    protected File configDir;
    protected List<String> configs = Collections.emptyList();
    private Profile profile;
    protected SorcerListener sorcerListener;

    protected Map<String, String> properties;
    protected Map<String, String> environment;

    //TODO RKR remove versions
    final protected static String[] CLASS_PATH = {
            "org.apache.river:start",
            "net.jini:jsk-resources",
            "net.jini:jsk-platform",
            "net.jini:jsk-lib",
            "net.jini.lookup:serviceui",

            "org.rioproject:rio-start",
            "org.rioproject:rio-platform",
            "org.rioproject:rio-logging-support",
            "org.rioproject.resolver:resolver-api",
            "org.rioproject:rio-lib",

            "org.sorcersoft.sorcer:sorcer-api",
            "org.sorcersoft.sorcer:sorcer-resolver",
            "org.sorcersoft.sorcer:sorcer-rio-start",
            "org.sorcersoft.sorcer:sorcer-rio-lib",
            "org.sorcersoft.sorcer:sos-boot",
            "org.sorcersoft.sorcer:sos-util",

            //required for sos.Handler to work
            "org.sorcersoft.sorcer:sos-platform",

            "org.codehaus.groovy:groovy-all:2.1.3",
            "org.apache.commons:commons-lang3:3.1",
            "com.google.guava:guava:15.0",
            "commons-io:commons-io",

            "org.slf4j:slf4j-api",
            "org.slf4j:jul-to-slf4j:1.7.5",
            "ch.qos.logback:logback-core:1.0.13",
            "ch.qos.logback:logback-classic:1.0.13"
    };
    private PropertyEvaluator evaluator;

    public final void start() throws Exception {
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

        if(Installer.isInstallRequired(logDir))
            Installer.install();

        doStart();
    }

    protected void ensureDirConfig() {
        if (home == null) {
            String envHome = System.getProperty(E_SORCER_HOME);
            if (envHome == null)
                throw new IllegalStateException("No SORCER_HOME set");
            home = new File(envHome);
        }

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

    protected void configure() {
    }

    abstract protected void doStart() throws Exception;

    public void setWaitMode(WaitMode waitMode) {
        this.waitMode = waitMode;
    }

    abstract protected Map<String, String> getEnvironment();

    protected Map<String, String> getProperties() {
        File policyPath = new File(configDir, "sorcer.policy");
        String resolverPath = Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether:5.0-M4-S4");

        try {
            IOUtils.ensureFile(policyPath, IOUtils.FileCheck.readable);
            IOUtils.ensureFile(new File(resolverPath), IOUtils.FileCheck.readable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> sysProps = new HashMap<String, String>();
        sysProps.put(MAX_DATAGRAM_SOCKETS, "1024");
        sysProps.put(PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        sysProps.put(RMI_SERVER_CLASS_LOADER, "org.rioproject.rmi.ResolvingLoader");
        sysProps.put(RMI_SERVER_USE_CODEBASE_ONLY, Boolean.FALSE.toString());

        sysProps.put(SECURITY_POLICY, policyPath.getPath());
        sysProps.put(UTIL_LOGGING_CONFIG_FILE, new File(configDir, "sorcer.logging").getPath());
        sysProps.put("logback.configurationFile", new File(configDir, "logback.groovy").getPath());

        sysProps.put(SORCER_HOME, home.getPath());
        sysProps.put(S_WEBSTER_TMP_DIR, new File(home, "databases").getPath());
        sysProps.put(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());
        //sysProps.put(S_WEBSTER_INTERFACE, getInetAddress());

        //rio specific
        sysProps.put("org.rioproject.service", "all");
        sysProps.put("RIO_HOME", rio.getPath());
        sysProps.put("RIO_LOG_DIR", logDir.getPath());
        sysProps.put("org.rioproject.resolver.jar", resolverPath);

        try {
            sysProps.put("org.rioproject.codeserver", "http://" + HostUtil.getInetAddress() + ":9010");
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to read host address", e);
        }

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
            result.add(new File(System.getProperty("JAVA_HOME"), "lib/tools.jar").getPath());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public enum WaitMode {
        no, start, end
    }

    public void setRio(File rio) {
        this.rio = rio;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    public void setExt(File ext) {
        this.ext = ext;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public void setConfigs(List<String> configs) {
        this.configs = configs;
    }

    public void setConfigDir(File config) {
        this.configDir = config;
    }

    public void setSorcerListener(SorcerListener listener) {
        this.sorcerListener = listener;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

}
