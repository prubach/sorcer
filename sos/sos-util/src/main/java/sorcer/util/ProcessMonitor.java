/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

package sorcer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class ProcessMonitor implements Runnable {
    final private static Logger log = LoggerFactory.getLogger(ProcessMonitor.class);

    private Process process;
    private ProcessDownCallback callback;

    public ProcessMonitor(Process process, ProcessDownCallback callback) {
        this.process = process;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (true)
            try {
                process.waitFor();
                callback.processDown(process);
                break;
            } catch (InterruptedException x) {
                log.warn("interrupted", x);
            }
    }

    public static void install(Process process, ProcessDownCallback callback, boolean daemon) {
        Thread thread = new Thread(new ProcessMonitor(process, callback), "Process Monitor for " + process);
        thread.setDaemon(daemon);
        thread.start();
    }

    public static void install(Process process, Runnable callback, boolean daemon) {
        install(process, new RunnableCallback(callback), daemon);
    }

    public static interface ProcessDownCallback {
        public void processDown(Process process);
    }

    private static class RunnableCallback implements ProcessDownCallback {
        private final Runnable callback;

        public RunnableCallback(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void processDown(Process process) {
            callback.run();
        }
    }
}
