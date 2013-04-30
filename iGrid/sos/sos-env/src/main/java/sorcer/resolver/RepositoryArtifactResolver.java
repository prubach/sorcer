package sorcer.resolver;

import java.io.File;

import sorcer.util.ArtifactCoordinates;

/**
 * @author Rafał Krupiński
 */
public class RepositoryArtifactResolver extends AbstractArtifactResolver {
	private static final char SEP = '/';
	private String root;

	public RepositoryArtifactResolver(String repositoryRoot) {
		this.root = repositoryRoot;
	}

	@Override
	public String resolveAbsolute(ArtifactCoordinates artifactCoordinates) {
		return new File(root, resolveRelative(artifactCoordinates)).getAbsolutePath();
	}

	@Override
	public String resolveRelative(ArtifactCoordinates artifactCoordinates) {
		String artifactId = artifactCoordinates.getArtifactId();
		String version = artifactCoordinates.getVersion();
		String groupId = artifactCoordinates.getGroupId();
		if (version == null) {
			version = resolveVersion(groupId, artifactId);
		}
		String classifier = artifactCoordinates.getClassifier();

		StringBuilder result = new StringBuilder().append(SEP);
		result.append(groupId.replace('.', SEP));
		result.append(SEP).append(artifactId).append(SEP).append(version).append(SEP)
				.append(artifactId).append('-').append(version);
		if (classifier != null) {
			result.append('-').append(classifier);
		}
		result.append('.').append(artifactCoordinates.getPackaging());
		return result.toString();

	}
}
