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
import sorcer.util.PropertiesLoader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
abstract public class AbstractArtifactResolver implements ArtifactResolver {

	final private Logger log = LoggerFactory.getLogger(getClass());

	// groupId_artifactId -> version
	protected Map<String, String> versions = new HashMap<String, String>();

	private static PropertiesLoader propertiesLoader = new PropertiesLoader();

	{
		versions = propertiesLoader.loadAsMap("META-INF/maven/versions.properties", Thread.currentThread()
				.getContextClassLoader());
	}

	@Override
	public String resolveAbsolute(String artifactCoordinates) {
		return resolveAbsolute(ArtifactCoordinates.coords(artifactCoordinates));
	}

	@Override
	public String resolveRelative(String artifactCoordinates) {
		return resolveRelative(ArtifactCoordinates.coords(artifactCoordinates));
	}

	/**
	 * Resolve version of artifact using versions.properties or pom.properties
	 * from individual artifact jar the jar must be already in the classpath of
	 * current thread context class loader in order to load its pom.version
	 *
	 * @param groupId    maven artifacts groupId
	 * @param artifactId maven artifacts artifactId
	 * @return artifacts version
	 * @throws IllegalArgumentException if version could not be found
	 */
	public String resolveVersion(String groupId, String artifactId) {
		String version = resolveCachedVersion(groupId, artifactId);
		if (version != null && checkFileExists(groupId, artifactId, version)) {
			return version;
		}

		version = loadVersionFromPomProperties(groupId, artifactId);
		if (version == null) {
			throw new IllegalArgumentException("Could not load version " + groupId + ':' + artifactId);
		}
		versions.put(key(groupId, artifactId), version);
		return version;
	}

	private boolean checkFileExists(String groupId, String artifactId, String version) {
		String path = resolveAbsolute(ArtifactCoordinates.coords(groupId, artifactId, version));
		return new File(path).exists();
	}

	private String key(String groupId, String artifactId) {
		return groupId + "_" + artifactId;
	}

	private String loadVersionFromPomProperties(String groupId, String artifactId) {
		String resourceName = String.format("META-INF/maven/%1$s/%2$s/pom.properties", groupId, artifactId);
		Properties properties;
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			properties = propertiesLoader.loadAsProperties(resourceName, contextClassLoader);
		} catch (IllegalArgumentException x) {
			log.debug("Could not find pom.properties for {}:{}", groupId, artifactId);
			return null;
		}

		String version = properties.getProperty("version");
		if (version == null) {
			throw new IllegalArgumentException("Could not load version " + groupId + ':' + artifactId
					+ " from versions.properties");
		}
		return version;
	}

	/**
	 * @return cached version, may be null
	 */
	private String resolveCachedVersion(String groupId, String artifactId) {
		if (versions.containsKey(groupId)) {
			return versions.get(groupId);
		}
		// may be null
		return versions.get(key(groupId, artifactId));
	}

	protected void close(Closeable inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// igonre
			}
		}
	}
}
