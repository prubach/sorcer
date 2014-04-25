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

package sorcer.core.provider.container;

import sorcer.config.BeanListener;
import sorcer.core.service.Configurer;
import sorcer.core.service.IServiceBeanListener;
import sorcer.core.service.IServiceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class ServiceProviderBeanListener implements IServiceBeanListener {
    private IServiceBeanListener parent;
    private List<? extends BeanListener> listeners;

    public ServiceProviderBeanListener(IServiceBeanListener parent) {
        this.parent = parent;
        listeners = Arrays.asList(
                new Configurer(),
                new LegacyInitializer()
        );
    }

    @Override
    public <T> void preProcess(IServiceBuilder<T> serviceBuilder) {
        parent.preProcess(serviceBuilder);
        for (BeanListener listener : listeners)
            listener.preProcess(serviceBuilder);
    }

    @Override
    public <T> void preProcess(IServiceBuilder<T> serviceBuilder, T bean) {
        parent.preProcess(serviceBuilder, bean);
        for (BeanListener listener : listeners)
            listener.preProcess(serviceBuilder, bean);
    }

    @Override
    public <T> void destroy(IServiceBuilder<T> serviceBuilder, T bean) {
        List<? extends BeanListener> destroyers = new ArrayList<BeanListener>(listeners);
        for (BeanListener destroyer : destroyers)
            destroyer.destroy(serviceBuilder, bean);
        parent.destroy(serviceBuilder, bean);
    }
}
