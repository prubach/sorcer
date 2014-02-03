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

import sorcer.resolver.Resolver;
import sorcer.util.HostUtil;
import sorcer.util.IOUtils;
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
    public WaitMode waitMode;
    protected File home;
    protected File ext;
    protected File rio;
    protected File logDir;
    protected File configDir;
    protected List<String> configs;
    private Profile profile;
    protected SorcerListener sorcerListener;

    final protected static String MAIN_CLASS = "sorcer.boot.ServiceStarter";

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

            "org.codehaus.groovy:groovy-all:2.1.3",
            "org.apache.commons:commons-lang3:3.1",
            "com.google.guava:guava:15.0",
            "commons-io:commons-io",

            "org.slf4j:slf4j-api",
            "org.slf4j:jul-to-slf4j:1.7.5",
            "ch.qos.logback:logback-core:1.0.13",
            "ch.qos.logback:logback-classic:1.0.13"
    };


    public final void start() throws Exception {
        //needed by Resolver to read repoRoot from sorcer.env
        ensureSystemProperty(SORCER_HOME, home.getPath());
        ensureSystemProperty(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());

        if (sorcerListener == null)
            sorcerListener = new NullSorcerListener();

        doStart();
    }

    private void ensureSystemProperty(String key, String value) {
        if (System.getProperty(key) == null)
            System.setProperty(key, value);
    }

    abstract protected void doStart() throws Exception;

    public void setWaitMode(WaitMode waitMode) {
        this.waitMode = waitMode;
    }

    protected Map<String, String> getEnvironment() {
        Map<String, String> sysProps = new HashMap<String, String>();
        sysProps.put(E_RIO_HOME, rio.getPath());
        sysProps.put("RIO_LOG_DIR", logDir.getPath());
        return sysProps;
    }

    protected Map<String, String> getProperties() {
        Map<String, String> sysProps = new HashMap<String, String>();
        sysProps.put(MAX_DATAGRAM_SOCKETS, "1024");
        sysProps.put(PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        sysProps.put(RMI_SERVER_CLASS_LOADER, "org.rioproject.rmi.ResolvingLoader");
        sysProps.put(RMI_SERVER_USE_CODEBASE_ONLY, Boolean.FALSE.toString());
        sysProps.put(SECURITY_POLICY, new File(configDir, "sorcer.policy").getPath());

        sysProps.put(UTIL_LOGGING_CONFIG_FILE, new File(configDir, "sorcer.logging").getPath());
        sysProps.put("logback.configurationFile", new File(configDir, "logback.groovy").getPath());

        sysProps.put(SORCER_HOME, home.getPath());
        sysProps.put(S_WEBSTER_TMP_DIR, new File(home, "databases").getPath());
        sysProps.put(S_KEY_SORCER_ENV, new File(configDir, "sorcer.env").getPath());
        //sysProps.put(S_WEBSTER_INTERFACE, getInetAddress());

        //rio specific
        sysProps.put("org.rioproject.service", "all");

        try {
            sysProps.put("org.rioproject.codeserver", "http://+" + HostUtil.getInetAddress() + ":9010");
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to read host address", e);
        }

        String[] monitorConfigPaths = profile.getMonitorConfigPaths();
        if (monitorConfigPaths != null && monitorConfigPaths.length != 0) {
            sysProps.put(P_MONITOR_INITIAL_OPSTRINGS, StringUtils.join(monitorConfigPaths, File.pathSeparator));
        }

        sysProps.put("RIO_HOME", rio.getPath());
        sysProps.put("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether:5.0-M4-S4"));

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

            PropertyEvaluator evaluator = new PropertyEvaluator();
            evaluator.addDefaultSources();
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
