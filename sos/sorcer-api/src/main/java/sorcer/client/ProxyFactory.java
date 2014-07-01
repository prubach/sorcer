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
import net.jini.lookup.ServiceItemFilter;
import sorcer.core.SorcerEnv;
import sorcer.river.Filters;
import sorcer.service.DynamicAccessor;
import sorcer.util.ClassLoaders;

import javax.inject.Provider;

/**
 * @author Rafał Krupiński
 */
public class ProxyFactory<T> implements Provider<T> {
    private ServiceTemplate template;

    private ServiceItemFilter filter;

    private DynamicAccessor accessor;

    public ProxyFactory(Class<T> type, DynamicAccessor accessor) {
        this(new ServiceTemplate(null, new Class[]{type}, new Entry[0]), Filters.any(), accessor);
    }

    public ProxyFactory(ServiceTemplate template, ServiceItemFilter filter, DynamicAccessor accessor) {
        this.template = template;
        this.filter = filter;
        this.accessor = accessor;
    }

    @Override
    public T get() {
        ClassLoaders.Callable<T, RuntimeException> getServiceCallable = new ClassLoaders.Callable<T, RuntimeException>() {
            @Override
            @SuppressWarnings("unchecked")
            public T call() throws RuntimeException {
                ServiceItem[] serviceItems = accessor.getServiceItems(template, 1, 1, filter, SorcerEnv.getLookupGroups());
                return (T) (serviceItems.length > 0 ? serviceItems[0].service : null);
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

    public void setTemplate(ServiceTemplate template) {
        this.template = template;
    }

    public void setFilter(ServiceItemFilter filter) {
        this.filter = filter;
    }
}
