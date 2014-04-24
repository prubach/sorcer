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

package sorcer.launcher.process;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.launcher.NullSorcerListener;

/**
 * Shutdown application by calling System.exit(0) when sorcerEnded is called
 *
 * @author Rafał Krupiński
 */
public class ExitingCallback extends NullSorcerListener {
    private static final Logger log = LoggerFactory.getLogger(ExitingCallback.class);
    private volatile boolean closing;

    @Override
    public synchronized void sorcerEnded(Exception e) {
        if (e != null)
            log.error("Exception while starting SORCER", e);
        if (isShuttingDown()) {
            closing = true;
            log.warn("ExitingCallback called when JVM is already shutting down");
            return;
        }
        if (!closing) {
            closing = true;
            log.info("Closing this SORCER JVM process");
            Runtime.getRuntime().exit(0);
        } else
            log.warn("Exiting callback called again");
    }

    public static boolean isShuttingDown() {
        try {
            Thread thread = new Thread();
            Runtime runtime = Runtime.getRuntime();
            runtime.addShutdownHook(thread);
            runtime.removeShutdownHook(thread);
        } catch (IllegalStateException ignored) {
            return true;
        }

        return false;
    }
}
