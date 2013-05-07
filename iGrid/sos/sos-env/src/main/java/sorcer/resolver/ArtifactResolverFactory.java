package sorcer.resolver;

import java.io.File;

import sorcer.core.SorcerEnv;
import sorcer.util.Artifact;
import sorcer.util.ArtifactCoordinates;

/**
 * Creates an artifact resolver instance. If there is a maven repository (its
 * path may be configured in sorcer.env; ~/.m2/repository is the default) it is
 * checked against org.sorcersoft.sorcer:sos-env artifact. If such an artifact
 * exists this repository is used. Otherwise we use ${sorcer.home}/lib as a root
 * of flattened jar repository.
 * 
 * @author Rafał Krupiński
 */
public class ArtifactResolverFactory {

	public ArtifactResolver createResolver() {
        File repoRoot = null;
        String repoDir = SorcerEnv.getRepoDir();
        if (repoDir !=null) {
            repoRoot = new File(repoDir);
            ArtifactResolver artifactResolver = new RepositoryArtifactResolver(repoRoot.getPath());
            if (tryResolve(Artifact.getSosEnv(), artifactResolver)) {
                return artifactResolver;
            }
        }
		repoRoot = new File(SorcerEnv.getHomeDir(), "lib");
		return new MappedFlattenedArtifactResolver(repoRoot);
	}

	private boolean tryResolve(ArtifactCoordinates coords, ArtifactResolver resolver) {
		String artifactPath = resolver.resolveAbsolute(coords);
		return new File(artifactPath).exists();
	}
}
