package sorcer.util;

/**
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
