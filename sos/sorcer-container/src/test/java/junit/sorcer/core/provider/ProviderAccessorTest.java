/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package junit.sorcer.core.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static sorcer.core.SorcerConstants.ANY;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import net.jini.core.lookup.ServiceItem;
import sorcer.core.SorcerEnv;
import sorcer.river.Filters;
import sorcer.service.Accessor;
import sorcer.service.DynamicAccessor;
import sorcer.core.provider.Jobber;
import sorcer.service.Service;
import sorcer.util.ProviderAccessor;
import sorcer.util.ProviderLocator;
import sorcer.util.ProviderLookup;
import sorcer.util.Stopwatch;

/**
 * @author Mike Sobolewski
 */

public class ProviderAccessorTest {

    public static final net.jini.core.lookup.ServiceTemplate jobberTemplate = Accessor.getServiceTemplate(null, ANY, new Class[]{Jobber.class}, null);
    private final static Logger logger = Logger
			.getLogger(ProviderAccessorTest.class.getName());

	static {
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/sorcer.policy");
		System.setSecurityManager(new RMISecurityManager());
	}

	public void providerAccessorTest() throws Exception {
        checkAccessor(new ProviderAccessor());
	}

	public void providerLookupTest() throws Exception {
        checkAccessor(new ProviderLookup());
	}

	public void providerLocatorTest() throws Exception {
        checkAccessor(new ProviderLocator());
	}

    public void checkAccessor(DynamicAccessor accessor){
        long startTime = System.currentTimeMillis();
        ServiceItem[] serviceItems = accessor.getServiceItems(jobberTemplate, 1, 1, Filters.any(), SorcerEnv.getLookupGroups());
        assertTrue(serviceItems.length > 0);
        Service provider = (Service) serviceItems[0].service;
        //logger.info("ProviderLocator provider: " + provider);
        logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
        assertNotNull(provider);
    }

	public static void main(String[] args) throws Exception {
		ProviderAccessorTest test = new ProviderAccessorTest();
		test.providerAccessorTest();
        test.providerLocatorTest();
		test.providerLookupTest();
	}
}
