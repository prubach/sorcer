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
public class ServiceBeanListener implements BeanListener {
    private static ServiceBeanListener inst = new ServiceBeanListener();

    public static BeanListener getBeanListener() {
        return inst;
    }

    private List<BeanListener> activators;
    private List<BeanListener> destroyers;

    {
        activators = new LinkedList<BeanListener>();
        activators.add(new Configurer());
        activators.add(new LoggerConfigurer());

        destroyers = new LinkedList<BeanListener>();
        destroyers.add(new ServiceBeanDestroyer());
    }

    public void activate(Object[] serviceBeans, ServiceProvider provider) throws ConfigurationException {
        for (BeanListener activator : activators) {
            activator.activate(serviceBeans, provider);
        }
    }

    @Override
    public void destroy(Object[] serviceBeans) {
        for (BeanListener activator : destroyers) {
            activator.destroy(serviceBeans);
        }
    }
}
