package sorcer.resolver;

import sorcer.util.ArtifactCoordinates;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
abstract public class AbstractArtifactResolver implements ArtifactResolver {

	@Override
	public String resolveAbsolute(String artifactCoordinates) {
		return resolveAbsolute(ArtifactCoordinates.coords(artifactCoordinates));
	}

	@Override
	public String resolveRelative(String artifactCoordinates) {
		return resolveRelative(ArtifactCoordinates.coords(artifactCoordinates));
	}

	public String resolveVersion(String groupId, String artifactId) {
		String resourceName = String.format("META-INF/maven/%1$s/%2$s/pom.properties", groupId, artifactId);
		URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if (resource == null) {
			return null;
		}
		Properties properties = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = resource.openStream();
			properties.load(inputStream);
		} catch (IOException x) {
			throw new RuntimeException("Could not load pom.properties for " + groupId + ":" + artifactId, x);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//igonre
				}
			}
		}
		return properties.getProperty("version");
	}
}
