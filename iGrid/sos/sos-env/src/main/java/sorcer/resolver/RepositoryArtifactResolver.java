package sorcer.resolver;

import sorcer.util.ArtifactCoordinates;
import sorcer.util.GenericUtil;

import java.io.File;

/**
 * @author Rafał Krupiński
 */
public class RepositoryArtifactResolver extends AbstractArtifactResolver {
	private static final char FILE_SEPARATOR = '/';
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
        //if (version==null) version = resolveVersion(artifactCoordinates.getGroupId(), artifactCoordinates.getArtifactId());
        if (version==null) version = resolveVersion(artifactCoordinates.getGroupId());
		String classifier = artifactCoordinates.getClassifier();

		StringBuilder result = new StringBuilder(FILE_SEPARATOR);
        result.append(artifactCoordinates.getGroupId().replace('.', FILE_SEPARATOR));
		result.append(FILE_SEPARATOR).append(artifactId).append(FILE_SEPARATOR).append(version).append(FILE_SEPARATOR).append(artifactId).append('-')
				.append(version);
		if (classifier != null) {
			result.append('-').append(classifier);
		}
		result.append('.').append(artifactCoordinates.getPackaging());
		return result.toString();

	}
}
