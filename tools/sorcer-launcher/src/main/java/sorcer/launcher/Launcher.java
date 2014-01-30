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

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    protected File config;
    protected List<String> configs;
    protected SorcerFlavour sorcerFlavour;

    protected SorcerListener sorcerListener;

    private Flavour flavour;

    public final void start() throws Exception {
        if (sorcerListener == null)
            sorcerListener = new NullSorcerListener();

        if (flavour != null && sorcerFlavour == null) {
            if (flavour == Flavour.rio)
                sorcerFlavour = new RioSorcerFlavour();
            else
                sorcerFlavour = new SorcerSorcerFlavour(home, ext);
        }

        doStart();
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
        sysProps.put(SECURITY_POLICY, new File(config, "sorcer.policy").getPath());

        sysProps.put(UTIL_LOGGING_CONFIG_FILE, new File(config, "sorcer.logging").getPath());
        sysProps.put("logback.configurationFile", new File(config, "logback.groovy").getPath());

        sysProps.put(SORCER_HOME, home.getPath());
        sysProps.put(S_WEBSTER_TMP_DIR, new File(home, "databases").getPath());
        sysProps.put(S_KEY_SORCER_ENV, new File(config, "sorcer.env").getPath());
        //sysProps.put(S_WEBSTER_INTERFACE, getInetAddress());

        //rio specific
        sysProps.put("org.rioproject.service", "all");

        try {
            sysProps.put("org.rioproject.codeserver", "http://+" + HostUtil.getInetAddress() + ":9010");
        } catch (UnknownHostException e) {
            System.err.println("Error" + e);
        }

        return sysProps;
    }

    protected Collection<String> resolveClassPath() {
        List<String> artifacts = sorcerFlavour.getClassPath();
        Set<String> result = new HashSet<String>(artifacts.size());
        try {
            for (String artifact : artifacts) {
                String p = Resolver.resolveAbsolute(artifact);
                IOUtils.ensureFile(new File(p));
                result.add(p);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<String> getConfigs() {
        return this.configs.isEmpty() ? sorcerFlavour.getDefaultConfigs() : this.configs;
    }

    public enum WaitMode {
        no, start, end
    }

    public enum Flavour {
        sorcer,
        rio
    }

    public void setRio(File rio) {
        this.rio = rio;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public void setFlavour(SorcerFlavour flavour) {
        this.sorcerFlavour = flavour;
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
        this.config = config;
    }

    public void setSorcerListener(SorcerListener listener) {
        this.sorcerListener = listener;
    }
}
