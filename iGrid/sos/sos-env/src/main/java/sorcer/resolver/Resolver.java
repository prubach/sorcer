package sorcer.resolver;

import org.apache.commons.lang3.StringUtils;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.util.ArtifactCoordinates;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class resolving ArtifactCoordinates to relative or absolute paths
 *
 * @author Rafał Krupiński
 */
public class Resolver {
	private static ArtifactResolver resolver = new ArtifactResolverFactory().createResolver();

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

	public static String resolveRelative(ArtifactCoordinates coords){
		return resolver.resolveRelative(coords);
	}

	public static String resolveRelative(String coords){
		return resolveRelative(ArtifactCoordinates.coords(coords));
	}

	public static String resolveAbsolute(String baseUri, ArtifactCoordinates coords){
		return URI.create(baseUri).resolve(resolveRelative(coords)).toString();
	}

	/**
	 * This is helper method for use in *.config files. The resulting string is
	 * passed to SorcerServiceDescriptor constructor as a codebase string.
	 *
	 * FIXME It's the constructors responsibility to prepend URL prefix of the webster. This is a bug and should be fixed.
	 * 
	 * @param coords
	 *            array of artifact coordinates
	 */
	@SuppressWarnings("unused")
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
