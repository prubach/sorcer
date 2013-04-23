package sorcer.resolver;

import sorcer.util.ArtifactCoordinates;

/**
 * @author Rafał Krupiński
 */
public interface ArtifactResolver {
	String resolveAbsolute(ArtifactCoordinates artifactCoordinates);
	String resolveAbsolute(String artifactCoordinates);
	String resolveRelative(ArtifactCoordinates artifactCoordinates);
	String resolveRelative(String artifactCoordinates);
}
