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

import com.sun.jini.admin.DestroyAdmin;
import net.jini.admin.Administrable;

import java.rmi.RemoteException;

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
        private DestroyAdmin service;

        public Runnable(Administrable service) throws RemoteException {
            this.service = (DestroyAdmin) service.getAdmin();
        }

        @Override
        public void run() {
            try {
                service.destroy();
            } catch (RemoteException e) {
                Thread c = Thread.currentThread();
                c.getUncaughtExceptionHandler().uncaughtException(c, e);
            }
        }
    }
}
