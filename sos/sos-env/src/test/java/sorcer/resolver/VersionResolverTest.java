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
package sorcer.resolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafał Krupiński
 */
public class VersionResolverTest {
	VersionResolver resolver;

	@Before
	public void init() {
		resolver = new VersionResolver();
	}

	@Test
	public void testResolveVersionProper() throws Exception {
		String version = resolver.resolveVersion("org.slf4j", "slf4j-api");
		Assert.assertNotNull(version);
	}

	@Test
	public void testResolveVersionRiver() throws Exception {
		String version = resolver.resolveVersion("org.apache.river", "outrigger");
		Assert.assertNotNull(version);
	}

    @Test(expected = IllegalArgumentException.class)
	public void testResolveVersionImproper() throws Exception {
		String version = resolver.resolveVersion("---mxbnvksghlk", "asdasdasd");
		Assert.assertNull(version);
	}


}
