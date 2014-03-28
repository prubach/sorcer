package sorcer.protocol;
/**
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

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.river.protocol.ProtocolEntry;
import sorcer.river.protocol.ProtocolHandlerEntry;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rafał Krupiński
 */
public class ProtocolHandlerRegistry implements URLStreamHandlerFactory, ServiceDiscoveryListener {
    private final static Logger log = LoggerFactory.getLogger(ProtocolHandlerRegistry.class);

    private Map<TypedServiceItem<URLStreamHandler>, Set<String>> remoteHandlers = new ConcurrentHashMap<TypedServiceItem<URLStreamHandler>, Set<String>>();

    private Map<String, TypedServiceItem<URLStreamHandler>> cachedRemoteHandlers = new ConcurrentHashMap<String, TypedServiceItem<URLStreamHandler>>();

    private Map<String, URLStreamHandler> localHandlers = new ConcurrentHashMap<String, URLStreamHandler>();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler result = localHandlers.get(protocol);
        if (result != null) {
            return result;
        }
        TypedServiceItem<URLStreamHandler> serviceItem = cachedRemoteHandlers.get(protocol);
        if (serviceItem == null) {
            serviceItem = findFirstByValue(remoteHandlers, protocol);
            if (serviceItem != null) {
                cachedRemoteHandlers.put(protocol, serviceItem);
                result = serviceItem.getService();
            }
        }
        return result;
    }

    private <K, V> K findFirstByValue(Map<K, Set<V>> map, V value) {
        for (Map.Entry<K, Set<V>> e : map.entrySet()) {
            if (e.getValue().contains(value))
                return e.getKey();
        }
        return null;
    }

    private static boolean installed;

    public void start() throws Exception {
        if (!installed)
            try {
                ProtocolHandlerRegistry registry = new ProtocolHandlerRegistry();
                URL.setURLStreamHandlerFactory(registry);
                installed = true;
            } catch (Error e) {
                throw new IllegalStateException("Could not install URLStreamHandlerFactory", e);
            }


        LookupDiscoveryManager lookupDiscoveryManager = new LookupDiscoveryManager(SorcerEnv.getLookupGroups(), null, null);
        ServiceDiscoveryManager serviceDiscoveryManager = new ServiceDiscoveryManager(lookupDiscoveryManager, new LeaseRenewalManager());
        LookupCache lookupCache = serviceDiscoveryManager.createLookupCache(new ServiceTemplate(null, null, null), new ServiceItemFilter() {
            @Override
            public boolean check(ServiceItem item) {
                return hasEntry(item.attributeSets, ProtocolEntry.class, ProtocolHandlerEntry.class);
            }
        }, this);

    }

    protected static boolean hasEntry(Entry[] attributeSets, Class<? extends Entry>... entryClasses) {
        for (Entry entry : attributeSets)
            for (Class<? extends Entry> entryClass : entryClasses)
                if (entryClass.isInstance(entry))
                    return true;
        return false;
    }

    protected static <T extends Entry> Collection<T> getEntries(Entry[] attributeSets, Class<T> entryClass) {
        List<T> result = new LinkedList<T>();
        for (Entry entry : attributeSets)
            if (entryClass.isInstance(entry))
                result.add((T) entry);
        return result;
    }

    @Override
    public void serviceAdded(ServiceDiscoveryEvent event) {
        ServiceItem serviceItem = event.getPostEventServiceItem();
        Set<String> protocols = new HashSet<String>();
        for (ProtocolEntry entry : getEntries(serviceItem.attributeSets, ProtocolEntry.class))
            Collections.addAll(protocols, entry.protocols);

        Object service = serviceItem.service;
        if (!protocols.isEmpty())
            if ((service instanceof URLStreamHandler)) {
                addHandler(new TypedServiceItem<URLStreamHandler>(serviceItem), protocols);
            } else {
                log.warn("Service {} annotated with {} not a {}", serviceItem.serviceID, ProtocolEntry.class, URLStreamHandler.class);
            }

        for (ProtocolHandlerEntry entry : getEntries(serviceItem.attributeSets, ProtocolHandlerEntry.class)) {
            Set<String> myProtocols = new HashSet<String>();
            for (String protocol : entry.protocols)
                if (!localHandlers.containsKey(protocol))
                    myProtocols.add(protocol);
            if (!myProtocols.isEmpty())
                try {
                    URLStreamHandler handler = entry.handler.get();
                    for (String protocol : myProtocols)
                        localHandlers.put(protocol, handler);
                } catch (Exception e) {
                    log.warn("Error", e);
                }
        }
    }

    private void addHandler(TypedServiceItem<URLStreamHandler> handler, Set<String> protocols) {
        remoteHandlers.put(handler, protocols);
    }

    @Override
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        ServiceItem serviceItem = event.getPostEventServiceItem();
        if (serviceItem.service instanceof URLStreamHandler) {
            TypedServiceItem<URLStreamHandler> key = new TypedServiceItem<URLStreamHandler>(serviceItem);
            remoteHandlers.remove(key);
            removeAllByValue(cachedRemoteHandlers, key);
        }
        // consider marshalled objects local, don't remove
    }

    protected static <V> boolean removeAllByValue(Map<?, V> map, V value) {
        if (!map.containsValue(value))
            return false;
        boolean result = false;
        Iterator<? extends Map.Entry<?, V>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<?, V> e = i.next();
            if (value.equals(e.getValue())) {
                i.remove();
                result = true;
            }
        }
        return result;
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        log.info("Service change of {} unsupported", event.getPreEventServiceItem());
    }
}
