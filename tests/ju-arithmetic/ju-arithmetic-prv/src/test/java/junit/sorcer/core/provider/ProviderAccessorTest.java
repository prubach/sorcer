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

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import sorcer.core.SorcerEnv;
import sorcer.service.Accessor;
import sorcer.service.Jobber;
import sorcer.service.Service;
import sorcer.util.ProviderLocator;
import sorcer.util.ProviderLookup;
import sorcer.util.Stopwatch;

/**
 * @author Mike Sobolewski
 */

public class ProviderAccessorTest {

    public static final net.jini.core.lookup.ServiceTemplate jobberTemplate = Accessor.getServiceTemplate(null, null, new Class[]{Jobber.class}, null);
    private final static Logger logger = Logger
			.getLogger(ProviderAccessorTest.class.getName());

	static {
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/sorcer.policy");
		System.setSecurityManager(new RMISecurityManager());
	}

	public void providerAcessorTest() throws Exception {
		long startTime = System.currentTimeMillis();
		Service provider = Accessor.getService(Jobber.class);
		//logger.info("ProviderAccessor provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}

	public void providerLookupTest() throws Exception {
		long startTime = System.currentTimeMillis();
        Service provider = (Service) new ProviderLookup().getServiceItems(jobberTemplate, 1, 1, null, SorcerEnv.getLookupGroups())[0].service;
        //logger.info("ProviderLookup provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}

	public void providerLookatorTest() throws Exception {
		long startTime = System.currentTimeMillis();
        Service provider = (Service) new ProviderLocator().getServiceItems(jobberTemplate, 1, 1, null, SorcerEnv.getLookupGroups())[0].service;
        //logger.info("ProviderLocator provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}

	public static void main(String[] args) throws Exception {
		ProviderAccessorTest test = new ProviderAccessorTest();
		test.providerAcessorTest();
		test.providerLookatorTest();
		test.providerLookupTest();
	}
}
