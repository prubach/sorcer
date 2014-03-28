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

package sorcer.protocol;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;

/**
 * @author Rafał Krupiński
 */
public class TypedServiceItem<T> extends ServiceItem {
    private static final long serialVersionUID = -1708635265611202587L;

    public TypedServiceItem(ServiceID serviceID, T service, Entry[] attrSets) {
        super(serviceID, service, attrSets);
    }

    @SuppressWarnings("unchecked")
    public TypedServiceItem(ServiceItem serviceItem) {
        this(serviceItem.serviceID, (T) serviceItem.service, serviceItem.attributeSets);
    }

    @SuppressWarnings("unchecked")
    public T getService() {
        return (T) service;
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj == this || (obj instanceof ServiceItem && equals((ServiceItem) obj)));
    }

    protected boolean equals(ServiceItem serviceItem) {
        return serviceID.equals(serviceItem.serviceID);
    }
}
