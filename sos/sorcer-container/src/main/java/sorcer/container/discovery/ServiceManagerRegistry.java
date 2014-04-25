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

package sorcer.container.discovery;

import com.google.common.collect.MapMaker;
import net.jini.core.discovery.LookupLocator;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class ServiceManagerRegistry implements IDiscoveryManagerRegistry {
    private static final Logger log = LoggerFactory.getLogger(ServiceManagerRegistry.class);

    @Inject
    private ILookupManagerRegistry lookupDiscoveryManagerRegistry;

    /**
     * Allow ServiceDiscoveryManagers to expire if unused
     */
    private Map<Set<String>, ServiceDiscoveryManager> registry = new MapMaker().weakValues().makeMap();
    private Map<Set<String>, LookupCache> caches = new MapMaker().weakValues().makeMap();
    private LeaseRenewalManager leaseRenewalManager = new LeaseRenewalManager();

    protected LookupLocator[] lookupLocators;
    protected String[] lookupGroups;

    public ServiceManagerRegistry(LookupLocator[] lookupLocators, String[] lookupGroups) {
        this.lookupLocators = lookupLocators;
        this.lookupGroups = lookupGroups;
    }

    @Override
    public ServiceDiscoveryManager getManager() throws IOException {
        return getManager(lookupGroups, lookupLocators);
    }

    @Override
    public ServiceDiscoveryManager getManager(String[] groups, LookupLocator[] locs) throws IOException {
        Set<String> key = key(groups);
        ServiceDiscoveryManager result = registry.get(key);
        if (result == null) {
            LookupDiscoveryManager ldm = lookupDiscoveryManagerRegistry.getManager(groups, lookupLocators);
            result = new ServiceDiscoveryManager(ldm, leaseRenewalManager);
            registry.put(key, result);
            caches.put(key, result.createLookupCache(null, null, null));
        }
        return result;
    }

    @Override
    public LookupCache getLookupCache() {
        return getLookupCache(lookupGroups);
    }

    @Override
    public LookupCache getLookupCache(String[] groups) {
        return caches.get(key(groups));
    }

    private static Set<String> key(String[] s) {
        if (s == null || s.length == 0)
            s = LookupDiscovery.ALL_GROUPS;
        Set<String> result = new HashSet<String>();
        Collections.addAll(result, s);
        return result;
    }
}
