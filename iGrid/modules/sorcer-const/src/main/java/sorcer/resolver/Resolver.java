package sorcer.resolver;

import org.apache.commons.lang3.StringUtils;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.util.ArtifactCoordinates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class resolving ArtifactCoordinates to relative or absolute paths
 *
 * @author Rafał Krupiński
 */
public class Resolver {
	//this is the place to put a proper ArtifactResolver implementation.
	//if we're going to use ${SORCER_HOME}/lib or ${SORCER_HOME}/internal-repo either change it, or write a factory
	private static ArtifactResolver resolver = new RepositoryArtifactResolver(SorcerEnv.getRepoDir());

	/**
	 * Resolve artifact coordinates to absolute path
	 *
	 * @param coords artifact coordinates string {@link ArtifactCoordinates#coords(String)}
	 * @return absolute path of file denoted bu the artifact coordinates
	 */
	public static String resolveAbsolute(String coords) {
		return resolveAbsolute(ArtifactCoordinates.coords(coords));
	}

	/**
	 * Resolve artifact coordinates to absolute path
	 *
	 * @return absolute path of file denoted bu the artifact coordinates
	 */
	public static String resolveAbsolute(ArtifactCoordinates coords) {
		return resolver.resolveAbsolute(coords);
	}

	/**
	 * Resolve array of artifacts to a codebase, space-separated list of relative paths
	 *
	 * @param coords array of artifact coordinates
	 */
	public static String resolveCodeBase(ArtifactCoordinates... coords) {
		String[] relatives = new String[coords.length];
		for (int i = 0; i < coords.length; i++) {
			relatives[i] = resolver.resolveRelative(coords[i]);
		}
		return StringUtils.join(relatives, SorcerConstants.CODEBASE_SEPARATOR);
	}

	/**
	 * Resolve array of artifact coordinates to ${File.pathSeparator}-separated list of absolute paths
	 */
	public static String resolveClassPath(ArtifactCoordinates... artifactCoordinatesList) {
		List<String> result = new ArrayList<String>(artifactCoordinatesList.length);
		for (ArtifactCoordinates coords : artifactCoordinatesList) {
			result.add(resolveAbsolute(coords));
		}
		return StringUtils.join(result, File.pathSeparator);
	}
}
