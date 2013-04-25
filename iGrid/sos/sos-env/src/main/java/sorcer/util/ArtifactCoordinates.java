package sorcer.util;

/**
 * @author Rafał Krupiński
 */
public class ArtifactCoordinates {
	private static final String DEFAULT_PACKAGING = "jar";
	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String packaging;

	/**
	 * 
	 * @param coords
	 *            artifact coordinates in the form of
	 *            groupId:artifactId[[:packaging[:classifier]]:version]
	 * @throws IllegalArgumentException
	 */
	public static ArtifactCoordinates coords(String coords) {
		String[] coordSplit = coords.split(":");
		int length = coordSplit.length;
		if (length < 2 || length > 5) {
			throw new IllegalArgumentException(
					"Artifact coordinates must be in a form of groupId:artifactId[[:packaging[:classifier]]:version]");
		}
		String groupId = coordSplit[0];
		String artifactId = coordSplit[1];
		String packaging = DEFAULT_PACKAGING;
		String classifier = null;
		//if version is not specified it will be resolved by the Resolver#resolveVersion
		String version = null;

		if (length == 3) {
			version = coordSplit[2];
		} else if (length == 4) {
			packaging = coordSplit[2];
			version = coordSplit[3];
		} else if (length == 5) {
			packaging = coordSplit[2];
			classifier = coordSplit[3];
			version = coordSplit[4];
		}

		return new ArtifactCoordinates(groupId, artifactId, packaging, version, classifier);
	}

	public ArtifactCoordinates(String groupId, String artifactId, String packaging, String version, String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.packaging = packaging;
		this.version = version;
		this.classifier = classifier;
	}

	public static ArtifactCoordinates coords(String groupId, String artifactId, String version) {
		return new ArtifactCoordinates(groupId, artifactId, DEFAULT_PACKAGING, version, null);
	}

	public ArtifactCoordinates(String groupId, String artifactId, String version) {
		this(groupId, artifactId, DEFAULT_PACKAGING, version, null);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(groupId.replace('.', '/'));
		result.append('/').append(artifactId).append('/').append(version).append('/').append(artifactId).append('-')
				.append(version);
		if (classifier != null) {
			result.append('-').append(classifier);
		}
		result.append('.').append(packaging);
		return result.toString();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}
}
