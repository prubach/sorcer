package sorcer.config;
/**
 *
 * Copyright 2013 Rafał Krupiński.
 * Copyright 2013 Sorcersoft.com S.A.
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


import net.jini.config.ConfigurationException;
import sorcer.core.provider.ServiceProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class ServiceBeanActivator implements ServiceActivator {
    private static ServiceBeanActivator inst = new ServiceBeanActivator();

    public static ServiceActivator getServiceBeanActivator() {
        return inst;
    }

    private List<ServiceActivator> activators;

    {
        activators = new LinkedList<ServiceActivator>();
        activators.add(new Configurer());
        activators.add(new LoggerConfigurer());
    }

    public void activate(Object[] serviceBeans, ServiceProvider provider) throws ConfigurationException {
        for (ServiceActivator activator : activators) {
            activator.activate(serviceBeans, provider);
        }
    }
}
