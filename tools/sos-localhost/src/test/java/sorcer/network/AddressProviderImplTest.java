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
package sorcer.network;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sorcer.resolver.Resolver;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.LibraryPathHelper;

import java.io.File;
import java.net.InetAddress;

/**
 * @author Rafał Krupiński
 */
public class AddressProviderImplTest {
	@BeforeClass
	public static void init() {
		ArtifactCoordinates sigar = ArtifactCoordinates.coords("org.sorcersoft.sigar:sigar:zip:native:1.6.4");
		//FIXME move to own class (SigarHelper or sth)
		LibraryPathHelper.updateLibraryPath(new File(new File(Resolver.resolveAbsolute(sigar)).getParentFile(), "lib").getAbsolutePath());
	}

	@Test
	public void testGetLocalAddress() throws Exception {
		AddressProviderImpl a = new AddressProviderImpl();
		InetAddress localAddress = a.getLocalAddress();
		Assert.assertNotNull(localAddress);
	}
}
