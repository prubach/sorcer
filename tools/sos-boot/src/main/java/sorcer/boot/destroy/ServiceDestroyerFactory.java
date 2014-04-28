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

import net.jini.admin.Administrable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.DestroyAdmin;
import sorcer.util.reflect.Classes;

import java.rmi.RemoteException;

/**
 * Factory for ServiceDestroyer instances. Special care is given to Rio services which call System.exit()
 *
 * @author Rafał Krupiński
 */
public class ServiceDestroyerFactory {
    private static final Logger log = LoggerFactory.getLogger(ServiceDestroyerFactory.class);

    public static ServiceDestroyer getDestroyer(Object service) {
        if (Classes.isInstanceOf("org.rioproject.servicebean.ServiceBean", service))
            try {
                return new RioServiceDestroyer(new Thread(new RioServiceDestroyer.Runnable((Administrable) service)));
            } catch (RemoteException e) {
                throw new IllegalStateException("Remote exception while calling local object", e);
            }
        if (service instanceof DestroyAdmin) {
            return new SorcerServiceDestroyer((DestroyAdmin) service);
        } else if (service instanceof com.sun.jini.admin.DestroyAdmin) {
            return new RiverServiceDestroyer((com.sun.jini.admin.DestroyAdmin) service);
        } else if (service instanceof Administrable)
            try {
                return getDestroyer(((Administrable) service).getAdmin());
            } catch (RemoteException e) {
                log.warn("Error while calling local object {}", service, e);
            }
        return null;
    }
}
