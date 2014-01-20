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

package sorcer.boot;

import org.rioproject.start.RioServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.DestroyAdmin;

import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sorcer.provider.boot.AbstractServiceDescriptor.Service;

/**
 * @author Rafał Krupiński
 */
public class ServiceStopper extends Thread {
    private final List<Service> services;
    private WeakReference<Thread> main;
    private static final Logger log = LoggerFactory.getLogger(ServiceStopper.class);

    public ServiceStopper(String name, List<Service> services) {
        super(name);
        this.main = new WeakReference<Thread>(Thread.currentThread());
        this.services = services;
    }

    static void install(List<Service> services) {
        Runtime.getRuntime().addShutdownHook(new ServiceStopper("SORCER service destroyer", services));
    }

    @Override
    public void run() {
        Thread main = this.main.get();
        if (main != null) {
            log.debug("Interrupting {}", main);
            main.interrupt();
        }
        ArrayList<Service> services = new ArrayList<Service>(this.services);
        Collections.reverse(services);
        for (Service service : services) {
            stop(service);
        }
    }

    private void stop(Service service) {
        Object impl = service.impl;
        if (impl instanceof DestroyAdmin) {
            DestroyAdmin da = (DestroyAdmin) impl;
            try {
                log.debug("Stopping {}", da);
                da.destroy();
            } catch (RemoteException e) {
                log.warn("Error", e);
            }
        } else if (impl instanceof com.sun.jini.admin.DestroyAdmin) {
            com.sun.jini.admin.DestroyAdmin da = (com.sun.jini.admin.DestroyAdmin) impl;
            try {
                log.info("Stopping {}", da);
                da.destroy();
            } catch (RemoteException e) {
                log.warn("Error", e);
            }
        } else if (impl instanceof RioServiceDescriptor.Created) {
            RioServiceDescriptor.Created created = (RioServiceDescriptor.Created) impl;
            stop(new Service(created.impl, created.proxy, service.descriptor, service.exception));
        } else {
            log.warn("Unable to stop {}", service.impl);
        }
    }
}
