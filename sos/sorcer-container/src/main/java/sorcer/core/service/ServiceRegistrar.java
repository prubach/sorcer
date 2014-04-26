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

package sorcer.core.service;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import sorcer.config.AbstractBeanListener;
import sorcer.container.discovery.ILookupManagerRegistry;

import javax.inject.Inject;

/**
 * @author Rafał Krupiński
 */
public class ServiceRegistrar extends AbstractBeanListener {
    @Inject
    protected ILookupManagerRegistry lookupManagerRegistry;

    @Inject
    protected LeaseRenewalManager leaseRenewalManager;

    @Override
    public <T> void preProcess(IServiceBuilder<T> serviceBuilder, T bean) {
        try {
            Entry[] serviceAttributes = serviceBuilder.getAttributes();
            LookupDiscoveryManager ldmgr = lookupManagerRegistry.getManager();
            Object proxy = serviceBuilder.getProxy();

            JoinManager joinManager = new JoinManager(proxy, serviceAttributes, new ServiceIDListener() {
                @Override
                public void serviceIDNotify(ServiceID serviceID) {
                    log.info("{}", serviceID);
                }
            }, ldmgr, leaseRenewalManager);
            serviceBuilder.putConfiguration(getClass().getName(), joinManager);
        }catch (RuntimeException e){
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> void destroy(IServiceBuilder<T> serviceBuilder, T bean) {
        JoinManager joinManager = (JoinManager) serviceBuilder.getConfiguration(getClass().getName());
        joinManager.terminate();
    }
}