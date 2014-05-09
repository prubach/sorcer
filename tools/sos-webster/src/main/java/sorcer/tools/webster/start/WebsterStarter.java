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

package sorcer.tools.webster.start;

import com.sun.jini.admin.DestroyAdmin;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.Component;
import sorcer.config.ConfigEntry;
import sorcer.core.SorcerEnv;
import sorcer.core.service.Configurer;
import org.rioproject.tools.webster.Webster;
import sorcer.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static sorcer.core.SorcerConstants.*;
import static sorcer.util.Collections.i;
import static sorcer.util.StringUtils.firstInteger;

/**
 * @author Rafał Krupiński
 */
@Component("sorcer.tools.codeserver")
public class WebsterStarter implements DestroyAdmin {
    static final Logger log = LoggerFactory.getLogger(WebsterStarter.class);

    @PostConstruct
    public void init() throws IOException, ConfigurationException {
        configurer.process(this, ConfigurationProvider.getInstance(args));

        boolean local = isLocal(websterAddress);
        log.debug("address = {}, local = {}", websterAddress, local);

        boolean portRanged = !(startPort > endPort || startPort < 0 || endPort < 0);

        if (websterPort < 0 && !portRanged)
            throw new IllegalArgumentException(String.format("No port configured port:%d startPort:%d endPort:%d", websterPort, startPort, endPort));

        if (portRanged && !local)
            throw new IllegalArgumentException("Illegal Webster configuration: port range and remote address defined");

        if (local) {
            startWebster();
            // error if port range is defined and webster didn't start
            if (webster == null && portRanged)
                throw new IllegalStateException("Could not start Webster");
            // if port is defined, use monitor() below to determine if local webster is running in another JVM
        } else {
            log.info("Webster configured on a remote address: " + SorcerEnv.getWebsterUrlURL());
            ping(SorcerEnv.getWebsterUrlURL());
        }

        // if webster didn't start and specific port is configured, check if it's Webster in another JVM on local host
        if (webster == null)
            monitor(local);
    }

    protected void startWebster() throws MalformedURLException {
        // treat {start,end}Port == 0 specially
        if (startPort <= 0 && endPort <= 0) {
            startPort = websterPort;
            endPort = websterPort;
        }

        for (int i = startPort; i < endPort + 1 && webster == null; i++) {
            log.debug("Trying {}:{}", websterAddress, i);
            try {
                webster = new Webster(i, StringUtils.join(roots, ';'), websterAddress);
                SorcerEnv.setWebsterUrl(websterAddress, i);
            } catch (BindException ex) {
                log.debug("Error while starting Webster", ex);
            }
        }
    }

    private void monitor(boolean startOnError) throws IOException {
        URL url = SorcerEnv.getWebsterUrlURL();
        // throw IllegalStateException if the remote server is up but not a Webster
        try {
            ping(url);
            WebsterMonitor websterMonitor = new WebsterMonitor(url, startOnError);
            ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(websterMonitor, 2, 2, TimeUnit.SECONDS);
            websterMonitor.setFuture(scheduledFuture);
        } catch (IOException e) {
            log.debug("Error pinging {}", url, e);
            if (startOnError)
                startWebster();
            else
                throw e;
        } catch (IllegalStateException e) {
            log.debug("Error pinging {}", url, e);
            if (startOnError)
                startWebster();
            else
                throw e;
        }
    }

    protected static void ping(URL url) throws IOException, IllegalStateException {
        log.debug("ping {}", url);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(2000);

        String server = conn.getHeaderField("Server");
        log.debug("ping server = {}", server);
        if (!Webster.class.getName().equals(server) && !sorcer.tools.webster.Webster.class.getName().equals(server)) {
            throw new IllegalStateException("Remote server on " + url + " not a Webster, " + server);
        }
    }

    class WebsterMonitor implements Runnable {
        private URL websterUrl;
        private Future<?> self;
        private boolean start;

        WebsterMonitor(URL websterUrl, boolean start) {
            this.websterUrl = websterUrl;
            this.start = start;
        }

        @Override
        public void run() {
            try {
                ping(websterUrl);
            } catch (IllegalStateException x) {
                log.error("Error", x);
                startInternal();
            } catch (IOException e) {
                log.debug("Error", e);
                startInternal();
            }
        }

        protected void startInternal() {
            try {
                if (start)
                    WebsterStarter.this.startWebster();
                if (self != null)
                    self.cancel(false);
            } catch (MalformedURLException x) {
                throw new RuntimeException(x);
            }
        }

        public void setFuture(Future<?> myFutureSelf) {
            self = myFutureSelf;
        }
    }

/*
    public static org.rioproject.tools.webster.Webster startInternal() throws UnknownHostException, BindException, MalformedURLException {
        org.rioproject.tools.webster.Webster result = new org.rioproject.tools.webster.Webster(0, getWebsterRootsStr(), HostUtil.getInetAddress().getHostAddress());
        SorcerEnv.setWebsterUrl(result.getAddress(), result.getPort());
        return result;
    }
*/

    private static String getWebsterRootsStr() {
        return StringUtils.join(getWebsterRoots(), ';');
    }

    private boolean isLocal(String address) throws SocketException {
        try {
            boolean isLocal = InetAddress.getByName(address).isLoopbackAddress();
            if (isLocal) return true;
        } catch (UnknownHostException ue) {
            log.warn("Problem checking if localhost: " + ue);
        }
        return find(address, NetworkInterface.getNetworkInterfaces()) != null;
    }

    private InetAddress find(String address, Enumeration<NetworkInterface> interfaces) {
        for (NetworkInterface iface : i(interfaces)) {
            for (InetAddress addr : i(iface.getInetAddresses())) {
                log.trace("{}", addr);
                if (address.equals(addr.getHostAddress()))
                    return addr;
            }

            InetAddress result = find(address, iface.getSubInterfaces());
            if (result != null)
                return result;
        }
        return null;
    }

    @Override
    public void destroy() throws RemoteException {
        if (webster != null)
            webster.terminate();
    }

    /**
     * Returns the start port to use for a SORCER code server.
     *
     * @return a port number
     */
    public static int getWebsterStartPort() {
        return firstInteger(
                -1,
                System.getenv("WEBSTER_START_PORT"),
                System.getProperty(S_WEBSTER_START_PORT),
                SorcerEnv.getProperty(P_WEBSTER_START_PORT)
        );
    }

    /**
     * Returns the end port to use for a SORCER code server.
     *
     * @return a port number
     */
    public static int getWebsterEndPort() {
        return firstInteger(-1,
                System.getenv("WEBSTER_END_PORT"),
                System.getProperty(S_WEBSTER_END_PORT),
                SorcerEnv.getProperty(P_WEBSTER_END_PORT)
        );
    }

    public static String[] getWebsterRoots() {
        return SorcerEnv.getWebsterRoots(new String[0]);
    }

    public static String[] getWebsterRoots(String[] additional) {
        return SorcerEnv.getWebsterRoots(additional);
    }

    @Inject
    Configurer configurer;

    @Inject
    String[] args;

    private Webster webster;

    @ConfigEntry
    int websterPort = 0;

    @ConfigEntry(required = true)
    String websterAddress;

    @ConfigEntry
    int startPort = -1;

    @ConfigEntry
    int endPort = -1;

    @ConfigEntry
    boolean isDaemon = false;

    @ConfigEntry
    String[] roots;

    /**
     * Use the default executor service to call AsyncPinger on a remote webster
     */
    @Inject
    private ScheduledExecutorService executor;
}
