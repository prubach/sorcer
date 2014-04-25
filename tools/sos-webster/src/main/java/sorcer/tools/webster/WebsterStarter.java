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

package sorcer.tools.webster;

import com.sun.jini.admin.DestroyAdmin;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.Component;
import sorcer.config.ConfigEntry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.concurrent.*;

import static sorcer.util.Collections.i;

/**
 * @author Rafał Krupiński
 */
@Component("sorcer.tools.codeserver")
public class WebsterStarter implements DestroyAdmin {
    private static final Logger log = LoggerFactory.getLogger(WebsterStarter.class);

    @PostConstruct
    public void init() throws SocketException {
        boolean start = isLocal(websterAddress);
        log.debug("address = {}, local = {}", websterAddress, start);

        if (!start) {
            log.info("Webster configured on a remote address");
            if (!ping(websterAddress, websterPort))
                throw new IllegalStateException("Remote webster down");
            return;
        }

        int port = websterPort;
        if (port == -1) {
            if (startPort > endPort || startPort < 0 || endPort < 0)
                throw new IllegalArgumentException(String.format("No port configured port:%d startPort:%d endPort:%d", port, startPort, endPort));
        }

        if (port == 0) {
            startPort = port;
            endPort = port;
        }

        for (int i = startPort; i <= endPort && webster != null; i++) {
            try {
                webster = start(i);
            } catch (BindException ex) {
                log.debug("Error while starting Webster", ex);
            }
        }
        if (webster == null)
            throw new IllegalStateException("Could not start Webster");
    }

    private boolean isLocal(String address) throws SocketException {
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

    private Webster start(int port) throws BindException {
        return new Webster(port, roots, websterAddress, true);

    }

    private boolean ping(String address, int port) {
        Future<Boolean> ping = executor.submit(new Pinger(address, port));
        try {
            return ping.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
            return false;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
                throw ((RuntimeException) cause);
            else
                throw ((Error) cause);
        } catch (TimeoutException e) {
            log.debug("Timeout exception", e);
            return false;
        }
    }


    @Override
    public void destroy() throws RemoteException {
        if (webster != null)
            webster.terminate();
    }

    private Webster webster;

    @ConfigEntry
    int websterPort = 9010;

    @ConfigEntry
    String websterAddress;

    @ConfigEntry
    int startPort = -1;

    @ConfigEntry
    int endPort = -1;

    @ConfigEntry
    boolean isDaemon = false;

    @ConfigEntry
    String[] roots;

    @Inject
    private ExecutorService executor;


    private static class Pinger implements Callable<Boolean> {
        private final String address;
        private final int port;

        public Pinger(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public Boolean call() {
            Socket socket = null;
            try {
                socket = new Socket(address, 80);
                return true;
            } catch (IOException e) {
                log.debug("Error pinging {}:{}", address, port, e);
                return false;
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }
    }
}
