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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static sorcer.container.discovery.LoggingDiscoveryListener.LOGGING_LISTSER;

/**
 * @author Rafał Krupiński
 */
public class LookupManagerRegistry implements ILookupManagerRegistry {
    static final Logger log = LoggerFactory.getLogger(LookupManagerRegistry.class);
    /**
     * Allow ServiceDiscoveryManagers to expire if unused
     */
    private Map<Set<String>, LookupDiscoveryManager> registry = new MapMaker().weakValues().makeMap();

    protected LookupLocator[] lookupLocators;
    protected String[] lookupGroups;

    public LookupManagerRegistry(LookupLocator[] lookupLocators, String[] lookupGroups) {
        this.lookupLocators = lookupLocators;
        this.lookupGroups = lookupGroups;
    }

    @Override
    public LookupDiscoveryManager getManager() throws IOException {
        return getManager(lookupGroups, lookupLocators);
    }

    @Override
    public LookupDiscoveryManager getManager(String[] groups, LookupLocator[] locs) throws IOException {
        Set<String> key = key(groups);
        LookupDiscoveryManager result = registry.get(key);
        if (result == null) {
            result = new LookupDiscoveryManager(groups, lookupLocators, LOGGING_LISTSER);
            registry.put(key, result);
        }
        return result;

    }

    private static Set<String> key(String[] s) {
        if (s == null || s.length == 0)
            s = LookupDiscovery.ALL_GROUPS;
        Set<String> result = new HashSet<String>();
        Collections.addAll(result, s);
        return result;
    }

}
