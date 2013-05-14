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
package junit.sorcer.util;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;
import net.jini.space.JavaSpace05;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.util.ProviderAccessor;
import sorcer.util.ServiceAccessor;
import sorcer.util.Sorcer;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mike Sobolewski
 */

public class UtilTest {
	private final static Logger logger = Logger.getLogger(UtilTest.class
			.getName());


	@BeforeClass
	public static void envSettingsTest() throws ExertionException, ContextException,
			SignatureException {
		try {
			assertNotNull(System.getenv("SORCER_HOME"));
			logger.info("SORCER_HOME: " + Sorcer.getHomeDir());
		} catch (AssertionError ae) {
			logger.severe("SORCER_HOME must be set and point to the Sorcer root directory!!!");
		}		
	}
	
	@Test
	public void spaceSuffixTest() throws ExertionException, ContextException,
			SignatureException {

		/*logger.info("space name: " + Sorcer.getSpaceName());
		logger.info("group space name: " + Sorcer.getSpaceGroup());
		
		logger.info("suffixed space name: "
				+ Sorcer.getSuffixedName(Sorcer.getSpaceName()));
		logger.info("actual space name: " +
				Sorcer.getActualSpaceName());*/
		
		
		assertEquals(Sorcer.getSuffixedName(Sorcer.getSpaceName()),
				Sorcer.getSpaceName() + "-" + Sorcer.getNameSuffix());

		assertEquals(Sorcer.getSuffixedName(Sorcer.getSpaceName()), Sorcer.getActualSpaceName());
	}

	@Ignore
	@Test
	public void getSpaceTest() throws ExertionException, ContextException,
			SignatureException {
		logger.info("exert space:\n" + ProviderAccessor.getSpace());
		
		ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { JavaSpace05.class }, new Entry[] { new Name(Sorcer.getActualSpaceName())});
		ServiceItem si = ServiceAccessor.getServiceItem(tmpl, null, new String[] { Sorcer.getSpaceGroup() });
		logger.info("got service: serviceID=" + si.serviceID + " template="
				+ tmpl + " groups=" + Sorcer.getSpaceGroup());
	}
		
}