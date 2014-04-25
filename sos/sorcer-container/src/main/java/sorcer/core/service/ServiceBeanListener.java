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

import sorcer.config.BeanListener;

import javax.inject.Inject;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class ServiceBeanListener implements IServiceBeanListener {
    private List<BeanListener> activators;

    @Inject
    public ServiceBeanListener(Set<BeanListener> platformListeners) {
        activators = new LinkedList<BeanListener>(platformListeners);
    }

    @Override
    public void preProcess(IServiceBuilder provider) {
        for (BeanListener activator : activators)
            activator.preProcess(provider);
    }

    @Override
    public void preProcess(IServiceBuilder serviceBuilder, Object bean) {
        for (BeanListener activator : activators) {
            activator.preProcess(serviceBuilder, bean);
        }
    }

    @Override
    public void destroy(IServiceBuilder serviceBuilder, Object bean) {
        List<BeanListener> destroyers = new ArrayList<BeanListener>(activators);
        Collections.reverse(destroyers);
        for (BeanListener activator : destroyers) {
            activator.destroy(serviceBuilder, bean);
        }
    }
}
