package sorcer.config;
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

import net.jini.config.ConfigurationException;
import sorcer.core.provider.Provider;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ServiceBeanListener {
    private List<BeanListener> activators;
    private List<BeanListener> destroyers;

    public ServiceBeanListener(Set<BeanListener> platformListeners) {
        activators = new LinkedList<BeanListener>();
        activators.add(new Configurer());
        activators.add(new LoggerConfigurer());
        activators.addAll(platformListeners);

        destroyers = new LinkedList<BeanListener>();
        destroyers.addAll(platformListeners);
        destroyers.add(new ServiceBeanDestroyer());
    }

    public void preProcess(Provider provider) {
        for (BeanListener activator : activators)
            activator.preProcess(provider);
    }

    public void activate(Object[] serviceBeans, Provider provider) throws ConfigurationException {
        for (BeanListener activator : activators) {
            activator.activate(serviceBeans, provider);
        }
    }

    public void destroy(Object[] serviceBeans) {
        for (BeanListener activator : destroyers) {
            activator.destroy(serviceBeans);
        }
    }
}
