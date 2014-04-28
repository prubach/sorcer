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

package sorcer.boot.destroy;

import org.rioproject.servicebean.ServiceBean;

/**
 * @author Rafał Krupiński
 */
public class RioServiceDestroyer implements ServiceDestroyer {
    private Thread thread;

    public RioServiceDestroyer(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void destroy() {
        thread.start();
    }

    public static class Runnable implements java.lang.Runnable {
        private ServiceBean service;

        public Runnable(ServiceBean service) {
            this.service = service;
        }

        @Override
        public void run() {
            service.destroy(true);
        }
    }
}
