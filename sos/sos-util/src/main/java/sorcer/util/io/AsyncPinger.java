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

package sorcer.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.IOUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

/**
* @author Rafał Krupiński
*/
public class AsyncPinger implements Callable<Boolean> {
    private final static Logger log = LoggerFactory.getLogger(AsyncPinger.class);

    private final String address;
    private final int port;

    public AsyncPinger(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public Boolean call() {
        Socket socket = null;
        try {
            socket = new Socket(address, port);
            return true;
        } catch (IOException e) {
            log.debug("Error pinging {}:{}", address, port, e);
            return false;
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    public static boolean ping(String address, int port, ExecutorService executor, long timeout, TimeUnit timeUnit){
        Future<Boolean> ping = executor.submit(new AsyncPinger(address, port));
        try {
            return ping.get(timeout, timeUnit);
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
}
