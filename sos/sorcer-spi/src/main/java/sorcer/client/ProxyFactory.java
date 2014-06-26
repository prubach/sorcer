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

package sorcer.client;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceItemFilter;
import sorcer.container.sdi.IDiscoveryManagerRegistry;
import sorcer.core.SorcerEnv;
import sorcer.river.Filters;
import sorcer.service.Accessor;
import sorcer.util.ClassLoaders;
import sorcer.util.InjectionHelper;

import javax.inject.Provider;
import java.util.concurrent.TimeUnit;

/**
 * @author Rafał Krupiński
 */
public class ProxyFactory<T> implements Provider<T> {
    /**
     * The future default is preferLocal, but currently only requireRemote is supported.
     * This is only informative, as this field is now ignored.
     */
    // private ServiceScope scope = ServiceScope.preferLocal;
    private ServiceScope scope = ServiceScope.requireRemote;

    private ServiceTemplate template;

    private boolean cache = true;

    private ServiceItemFilter filter = Filters.any();

    private long lastCheck;

    private String[] groups = SorcerEnv.getLookupGroups();

    /**
     * Time between Accessor failure and the next retry of Accessor.getService
     */
    private long failureGracePeriod = TimeUnit.MINUTES.toMillis(1);

    public ProxyFactory(Class<T> type) {
        template = new ServiceTemplate(null, new Class[]{type}, new Entry[0]);
    }

    @Override
    public T get() {
        IDiscoveryManagerRegistry registry = InjectionHelper.create(IDiscoveryManagerRegistry.class);
        LookupCache cache = registry.getLookupCache(groups);
        if (cache != null) {
            ServiceItem result = cache.lookup(Filters.and(filter, Filters.serviceTemplateFilter(template)));
            if (result != null)
                return (T) result.service;
        }

        ClassLoaders.Callable<T, RuntimeException> getServiceCallable = new ClassLoaders.Callable<T, RuntimeException>() {
            @Override
            @SuppressWarnings("unchecked")
            public T call() throws RuntimeException {
                return (T) Accessor.getService(template, filter);
            }
        };
        T result;
        if (template.serviceTypes != null && template.serviceTypes.length > 0)
            result = ClassLoaders.doWith(template.serviceTypes[0].getClassLoader(), getServiceCallable);
        else
            result = getServiceCallable.call();

        checkResult(result);
        return result;
    }

    protected void checkResult(T result) {
        if (template.serviceTypes != null)
            for (Class type : template.serviceTypes)
                if (!type.isInstance(result))
                    throw new IllegalArgumentException("Obtained object " + result + " is not a " + type.getName());
    }

    public void setScope(ServiceScope scope) {
        this.scope = scope;
    }

    public void setTemplate(ServiceTemplate template) {
        this.template = template;
    }

    /**
     * Set the service object explicitly, intended for testing purposes
    public void setService(T service) {
        this.service = service;
    }
     */

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public void setFilter(ServiceItemFilter filter) {
        this.filter = filter;
    }
}
