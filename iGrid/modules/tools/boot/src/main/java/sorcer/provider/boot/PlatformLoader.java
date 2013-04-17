/*
 * Copyright 2008 the original author or authors.
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
package sorcer.provider.boot;

import org.rioproject.config.PlatformCapabilityConfig;
import org.rioproject.resolver.ResolverException;

import java.net.URL;
import java.util.Collection;

/**
 * Parses platform configuration documents
 *
 * @author Dennis Reedy
 */
public class PlatformLoader extends org.rioproject.config.PlatformLoader {

	public PlatformLoader() throws ResolverException {
	}

	/**
	 * Get the default platform configuration
	 *
	 * @return An array of PlatformCapabilityConfig objects returned from parsing the
	 *         default configuration META-INF/platform.xml
	 * @throws Exception if there are errors parsing and/or processing the
	 *                   default configuration
	 */
	public PlatformCapabilityConfig[] getDefaultPlatform() throws Exception {
		URL platformConfig =
				PlatformLoader.class.getClassLoader().getResource("META-INF/platform.xml");
		if (platformConfig == null) {
			logger.warn("META-INF/platform.xml not found");
			return new PlatformCapabilityConfig[0];
		}
		Collection<PlatformCapabilityConfig> c = parsePlatform(platformConfig);
		return (c.toArray(new PlatformCapabilityConfig[c.size()]));
	}

}
