package sorcer.util;

/**
 * This is a tool to make referring to sorcer jars a bit easier.
 *
 * Please avoid using it in examples for other artifacts than core SORCER.
 * 
 * @author Rafał Krupiński
 */
public class Artifact {
	private static final String SORCER_GROUP_ID = "org.sorcersoft.sorcer";

	public static ArtifactCoordinates sorcer(String artifactId) {
		return ArtifactCoordinates.coords(SORCER_GROUP_ID + ":" + artifactId);
	}

	public static ArtifactCoordinates getSosPlatform() {
		return sorcer("sos-platform");
	}

	public static ArtifactCoordinates getSosEnv() {
		return sorcer("sos-env");
	}
}
