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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.ArtifactCoordinates;

/**
 * @author Rafał Krupiński
 */
abstract public class AbstractArtifactResolver implements ArtifactResolver {

	final protected Logger log = LoggerFactory.getLogger(getClass());
    protected final VersionResolver versionResolver = new VersionResolver();

	@Override
	public String resolveAbsolute(String artifactCoordinates) {
		return resolveAbsolute(ArtifactCoordinates.coords(artifactCoordinates));
	}

	@Override
	public String resolveRelative(String artifactCoordinates) {
		return resolveRelative(ArtifactCoordinates.coords(artifactCoordinates));
	}

	/**
	 * Resolve version of artifact using groupversions.properties or pom.properties
	 * from individual artifact jar the jar must be already in the classpath of
	 * current thread context class loader in order to load its pom.version
	 *
	 * @param groupId    maven artifacts groupId
	 * @param artifactId maven artifacts artifactId
	 * @return artifacts version
	 * @throws IllegalArgumentException if version could not be found
	 */
	protected String resolveVersion(String groupId, String artifactId) {
        return versionResolver.resolveVersion(groupId, artifactId);
	}
}
