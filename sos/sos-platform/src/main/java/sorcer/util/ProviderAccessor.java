/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.util;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.core.provider.Cataloger;
import sorcer.core.provider.Provider;
import sorcer.river.Filters;
import sorcer.service.Accessor;

import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * A utility class that provides access to SORCER services and some
 * infrastructure services. It extends the <code>ServiceAccessor</code>
 * functionality.
 *
 * The <code>getService</code> methods directly use ServiceAccessor calls while
 * the <code>getService</code> methods use a SORCER Cataloger's cached services
 * with round robin load balancing.
 *
 * The individual SORCER services should be accessed using this utility since it
 * uses local cached proxies for frequently used SORCER infrastructure services,
 * for example: Cataloger, JavaSpace, Jobber. ProviderAccessor normally uses
 * Cataloger if available, otherwise it uses Jini lookup services as implemented
 * by <code>ServiceAccessor</code>.
 *
 * @see ServiceAccessor
 */

public class ProviderAccessor extends ServiceAccessor {

	private static final Logger logger = LoggerFactory.getLogger(ProviderAccessor.class);

    /**
	 * Used for local caching to speed up getting frequently needed service
	 * providers. Calls to discover JavaSpace takes a lot of time.
	 */
	protected Cataloger cataloger;

	public ProviderAccessor() {
		// Nothing to do, uses the singleton design pattern
	}

    /**
	 * Returns a SORCER Cataloger Service.
	 *
	 * This method searches for either a JINI or a RMI Cataloger service.
	 *
	 * @return a Cataloger service proxy
     * @see sorcer.core.provider.Cataloger
	 */
	protected Cataloger getCataloger() {
        return getCataloger(providerNameUtil.getName(Cataloger.class)) ;
	}

	/**
	 * Returns a SORCER Cataloger service provider using JINI discovery.
	 *
	 * @return a SORCER Cataloger
	 */
    protected Cataloger getCataloger(String serviceName) {
        boolean catIsOk;
		try {
            catIsOk = Accessor.isAlive((Provider) cataloger);
            if (catIsOk) {
				return cataloger;
			} else {
                ServiceItem[] serviceItems = getServiceItems(Accessor.getServiceTemplate(null, serviceName, new Class[]{Cataloger.class}, null), 1, 1, Filters.any(), SorcerEnv.getLookupGroups());
                cataloger = serviceItems.length == 0 ? null : (Cataloger) serviceItems[0].service;
                if (Accessor.isAlive((Provider)cataloger))
                    return cataloger;
                else
                    return null;
            }
		} catch (Exception e) {
            logger.warn("getService", e);
			return null;
		}
	}

    @Override
    public ServiceItem[] getServiceItems(ServiceTemplate template, int minMatches, int maxMatches, ServiceItemFilter filter, String[] groups) {
        assert template != null;

        // cataloger throws NPE if attributeSetTemplates is null
        assert template.attributeSetTemplates != null;
        assert filter != null;
        assert minMatches <= maxMatches;

        if(!Arrays.asList(template.serviceTypes).contains(Cataloger.class)){
            Cataloger cataloger = getCataloger();
            if (cataloger != null) {
                try {
                    ServiceMatches matches = cataloger.lookup(template, maxMatches);
                    ServiceItem[] matching = Filters.matching(matches.items, filter);
                    if (matching.length > 0) return matching;
                } catch (RemoteException e) {
                    logger.warn("Problem with Cataloger, falling back", e);
                }
            }
        }
        return super.getServiceItems(template, minMatches, maxMatches, filter, groups);
    }
}
