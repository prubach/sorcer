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

package sorcer.container.sdi;

import com.google.common.collect.MapMaker;
import net.jini.core.discovery.LookupLocator;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;

import java.io.IOException;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class DiscoveryManagerRegistry implements IDiscoveryManagerRegistry {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryManagerRegistry.class);

    private static LookupLocator[] lookupLocators = getLookupLocators();

    /**
     * Allow ServiceDiscoveryManagers to expire if unused
     */
    private Map<Set<String>, ServiceDiscoveryManager> registry = new MapMaker().weakValues().makeMap();
    private Map<Set<String>, LookupCache> caches = new MapMaker().weakValues().makeMap();


    @Override
    public ServiceDiscoveryManager getManager() throws IOException {
        return getManager(LookupDiscovery.ALL_GROUPS);
    }

    @Override
    public ServiceDiscoveryManager getManager(String[] groups) throws IOException {
        Set<String> key = key(groups);
        ServiceDiscoveryManager result = registry.get(key);
        if (result == null) {
            LookupDiscoveryManager ldm = new LookupDiscoveryManager(groups, lookupLocators, null);
            result = new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
            registry.put(key, result);
            caches.put(key, result.createLookupCache(null, null, null));
        }
        return result;
    }

    @Override
    public LookupCache getLookupCache() {
        return getLookupCache(LookupDiscovery.ALL_GROUPS);
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

    private static LookupLocator[] getLookupLocators() {
        String[] locURLs = SorcerEnv.getLookupLocators();
        if (locURLs == null || locURLs.length == 0) {
            return null;
        }
        List<LookupLocator> locators = new ArrayList<LookupLocator>(locURLs.length);
        log.debug("ProviderAccessor Locators: {}", locURLs);

        for (String locURL : locURLs)
            try {
                locators.add(new LookupLocator(locURL));
            } catch (Throwable t) {
                log.warn("Invalid Lookup URL: {}", locURL);
            }

        if (locators.isEmpty())
            return null;
        return locators.toArray(new LookupLocator[locators.size()]);
    }
}
