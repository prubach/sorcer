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

package sorcer.core.provider;

import net.jini.config.Configuration;
import net.jini.core.entry.Entry;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.InvocationLayerFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.service.IProviderServiceBuilder;
import sorcer.jini.jeri.SorcerILFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Transitional class bridging ServiceProvider and IServiceBuilder
 *
 * @author Rafał Krupiński
 */
public class ProviderServiceBuilder implements IProviderServiceBuilder {
    private static final Logger log = LoggerFactory.getLogger(ProviderServiceBuilder.class);
    private ServiceProvider serviceProvider;

    public ProviderServiceBuilder(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    private List<MethodInterceptor> interceptors = new LinkedList<MethodInterceptor>();

    @Override
    public void addInterceptor(MethodInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    private Map<Class, Object> serviceContributions = new HashMap<Class, Object>();

    @Override
    public <I> void contributeInterface(I impl, Class<? super I>... iface) {
        for (Class<? super I> type : iface)
            serviceContributions.put(type, impl);
    }

    @Override
    public void addAttribute(Entry attribute) {
        serviceProvider.getDelegate().addExtraLookupAttribute(attribute);
    }

    @Override
    public Configuration getProviderConfiguration() {
        return serviceProvider.getProviderConfiguration();
    }

    @Override
    public Object getConfiguration(Object key) {
        return serviceProvider.configurations.get(key);
    }

    @Override
    public void putConfiguration(Object key, Object data) {
        serviceProvider.configurations.put(key, data);
    }

    @Override
    public String getName() {
        return serviceProvider.getName();
    }

    @Override
    public Class<?> getType() {
        return Provider.class;
    }

    public InvocationLayerFactory getILFactory(Map<Class, Object> serviceComponents, ClassLoader implClassLoader) {
        if (serviceComponents == null || serviceComponents.isEmpty())
            if (interceptors.isEmpty())
                return new BasicILFactory();
            else
                return null;
        else
            if(interceptors.isEmpty())
                return new SorcerILFactory(serviceComponents, implClassLoader);
        return null;
    }

    public void addContributedBeans(Map<Class, Object> serviceBeans) {
        for (Map.Entry<Class, Object> e : serviceContributions.entrySet()) {
            if (serviceBeans.containsKey(e.getKey()))
                log.warn("Bean contribution would override configured service bean {}", e.getValue());
            else
                serviceBeans.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Provider getProvider() {
        return serviceProvider;
    }

    @Override
    public Object get() {
        return getProvider();
    }
}
