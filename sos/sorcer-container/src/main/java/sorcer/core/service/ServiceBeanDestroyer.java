package sorcer.core.service;
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.AbstractBeanListener;

import java.rmi.RemoteException;

/**
 * @author Rafał Krupiński
 */
public class ServiceBeanDestroyer extends AbstractBeanListener {
    private final static Logger log = LoggerFactory.getLogger(ServiceBeanDestroyer.class);

    /**
     * Destroy beans if they implements any known destroyable interface
     *
     * @param o bean to destroy
     */
    @Override
    public void destroy(IServiceBuilder ierviceBuilder, Object o) {
        if (o == null) return;

        try {
            if (o instanceof com.sun.jini.admin.DestroyAdmin)
                ((com.sun.jini.admin.DestroyAdmin) o).destroy();
            else if (o instanceof sorcer.core.DestroyAdmin)
                ((sorcer.core.DestroyAdmin) o).destroy();
        } catch (RemoteException e) {
            log.warn("Unexpected RemoteException while destroying local service bean " + o, e);
        }
    }
}
