/*
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

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.service.DynamicAccessor;

import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service discovery and management utility allowing to access services by
 * matching service templates and passing filters provided by clients. The
 * optional usage of a service lookup cache is provided for clients that require
 * lookup for multiple services frequently.
 * <p>
 * A similar service discovery and management functionality is provided by a
 * SORCER Cataloger service provider with round robin load balancing.
 * <p>
 * The continuous discovery of SORCER services is usually delegated to
 * Catalogers while other SORCER service providers can query effectively
 * Cataloger's cached services with round robin load balancing.
 * <p>
 * The individual SORCER services should be accessed using the
 * {@link sorcer.util.ProviderAccessor} subclass, that uses local cached proxies
 * for frequently used SORCER infrastructure services. ProviderAccessor normally
 * uses Cataloger if available, otherwise uses Jini lookup services as
 * implemented by the ServiceAccessor.
 *
 * @author Mike Sobolewski
 */
public class ServiceAccessor implements DynamicAccessor {

	static Logger logger = Logger.getLogger(ServiceAccessor.class.getName());

	static private boolean cacheEnabled = SorcerEnv.isLookupCacheEnabled();

	protected static long WAIT_FOR = SorcerEnv.getLookupWaitTime();

    protected final static int LUS_REAPEAT = 3;

	protected DiscoveryManagement ldManager = null;

	protected ServiceDiscoveryManager sdManager = null;

	protected LookupCache lookupCache = null;

    protected Map<String, Object> cache = new HashMap<String, Object>();

    protected ProviderNameUtil providerNameUtil = new SorcerProviderNameUtil();

    public ServiceAccessor() {
        openDiscoveryManagement(SorcerEnv.getLookupGroups());
    }


	/**
	 * Creates a service lookup and discovery manager with a provided service
	 * template, lookup cache filter, and list of jini groups.
	 *
	 * @param groups River group names
	 */
	protected void openDiscoveryManagement(String[] groups) {
		if (sdManager == null) {
			LookupLocator[] locators = getLookupLocators();
			try {
				logger.finer("[openDiscoveryManagement]\n"
						+ "\tSORCER Group(s): "
						+ StringUtils.arrayToString(groups) + "\n"
						+ "\tLocators:        "
						+ StringUtils.arrayToString(locators));

				ldManager = new LookupDiscoveryManager(groups, locators, null);
				sdManager = new ServiceDiscoveryManager(ldManager,
						new LeaseRenewalManager());
			} catch (Throwable t) {
				logger.throwing(ServiceAccessor.class.getName(),
						"openDiscoveryManagement", t);
			}
		}
		// Opening a lookup cache
		openCache();
	}

	/**
	 * Terminates lookup discovery and service discovery mangers.
	 */
	private void closeDiscoveryManagement() {
		if (cacheEnabled) {
			return;
		}
		if (ldManager != null) {
			ldManager.terminate();
		}
		if (sdManager != null) {
			sdManager.terminate();
		}
		closeLookupCache();
		ldManager = null;
		sdManager = null;
	}

	/**
	 * Creates a lookup cache for the existing service discovery manager
	 */
	private void openCache() {
		if (cacheEnabled && lookupCache == null) {
			try {
				lookupCache = sdManager.createLookupCache(null,
						null, null);
			} catch (RemoteException e) {
				closeLookupCache();
			}
		}
	}

	/**
	 * Terminates a lookup cache used by this ServiceAccessor.
	 */
	private void closeLookupCache() {
		if (lookupCache != null) {
			lookupCache.terminate();
			lookupCache = null;
		}
	}

	/**
	 * Returns a service matching serviceType, service attributes (entries), and
	 * passes a provided filter.
	 *
	 * @param attributes   attributes of the requested provider
	 * @param serviceType type of the requested provider
	 * @return a SORCER provider
	 */
    @SuppressWarnings("unchecked")
	public <T>T getService(Class<T> serviceType, Entry[] attributes, ServiceItemFilter filter) {
		if (serviceType == null) {
			throw new RuntimeException("Missing service type for a ServiceTemplate");
		}

		ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { serviceType }, attributes);

		ServiceItem si = getServiceItem(tmpl, filter);
		if (si != null)
			return (T)si.service;
		else
			return null;
	}


	/**
	 * Returns a service matching serviceName and serviceType using Jini lookup
	 * service.
	 *
     * @param serviceName name of the requested provider
     * @param serviceType type of the requested provider
	 * @return a service provider
	 */
	public <T> T getService(String serviceName, Class<T> serviceType) {
		T proxy = null;
		if (serviceName != null && serviceName.equals(SorcerConstants.ANY))
			serviceName = null;
		int tryNo = 0;
		while (tryNo < LUS_REAPEAT) {
			logger.info("trying to get service: " + serviceType + ":" + serviceName + "; attempt: "
					+ tryNo + "...");
			try {
				tryNo++;
				proxy = getService(serviceType, new Entry[] { new Name(
						serviceName) }, null);
				if (proxy != null)
					break;

				Thread.sleep(WAIT_FOR);
			} catch (Exception e) {
				logger.throwing("" + ServiceAccessor.class, "getService", e);
			}
		}
		logger.info("got LUS service [type=" + serviceType + " name=" + serviceName + "]: " + proxy);

		return proxy;
	}

    /**
     * Implements DynamicAccessor interface - provides compatibility with ProviderAccessor
     */
    public <T> T getProvider(String serviceName, Class<T> serviceType) {
        return getService(serviceName, serviceType);
    }

    /**
	 * Returns a list of lookup locators with the URLs defined in the SORCER
	 * environment
	 *
	 * @see sorcer.util.Sorcer
	 *
	 * @return a list of locators for unicast lookup discovery
	 */
	private LookupLocator[] getLookupLocators() {
		String[] locURLs = SorcerEnv.getLookupLocators();
        if (locURLs == null || locURLs.length == 0) {
            return null;
        }
        List<LookupLocator> locators = new ArrayList<LookupLocator>(locURLs.length);
        logger.finer("ProviderAccessor Locators: " + Arrays.toString(locURLs));

		for (String locURL : locURLs)
			try {
				locators.add(new LookupLocator(locURL));
			} catch (Throwable t) {
				logger.warning(
						"Invalid Lookup URL: " + locURL);
			}

		if (locators.isEmpty())
			return null;
        return locators.toArray(new LookupLocator[locators.size()]);
	}

    public ServiceItem getServiceItem(ServiceTemplate template, ServiceItemFilter filter) {
        try {
            return sdManager.lookup(template, filter, WAIT_FOR);
        } catch (InterruptedException e) {
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override
    public ServiceItem[] getServiceItems(ServiceTemplate template, int minMatches, int maxMatches, ServiceItemFilter filter, String[] groups) {
        if (groups != null) {
            Set<String> defaultGroups = new HashSet<String>(Arrays.asList(SorcerEnv.getLookupGroups()));
            Set<String> userGroups = new HashSet<String>(Arrays.asList(groups));
            if (!defaultGroups.equals(userGroups)) {
                throw new IllegalArgumentException("User requested River group other than default, this is currently unsupported");
            }
        }
        for (int tryNo = 0; tryNo < LUS_REAPEAT; tryNo++) {
            ServiceItem[] result = doGetServiceItems(template, minMatches, maxMatches, filter);
            if (result != null && result.length > 0)
                return result;

            try {
                Thread.sleep(ServiceAccessor.WAIT_FOR);
            } catch (InterruptedException ignored) {
                //ignore
            }
        }
        return new ServiceItem[0];
    }

    private ServiceItem[] doGetServiceItems(ServiceTemplate template, int minMatches, int maxMatches, ServiceItemFilter filter) {
        try {
            return sdManager.lookup(template, minMatches, maxMatches, filter, WAIT_FOR);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error while getting service", e);
            return null;
        }
    }

}
