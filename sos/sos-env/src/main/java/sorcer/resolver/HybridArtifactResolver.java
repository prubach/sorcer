package sorcer.resolver;

import sorcer.util.ArtifactCoordinates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
public class HybridArtifactResolver extends AbstractArtifactResolver {

	protected File flatRootDir;
    protected String repoDir;
    MappedFlattenedArtifactResolver flatResolver;
    RepositoryArtifactResolver repoResolver;


	public HybridArtifactResolver(File flatRoot, String repoDir) {
        this.flatRootDir = flatRoot;
        this.repoDir = repoDir;
        flatResolver = new MappedFlattenedArtifactResolver(flatRootDir);
        repoResolver = new RepositoryArtifactResolver(repoDir);
	}

	@Override
	public String resolveAbsolute(ArtifactCoordinates artifactCoordinates) {
        String jar = flatResolver.resolveAbsolute(artifactCoordinates);
        if (jar!=null) return jar;
        return repoResolver.resolveAbsolute(artifactCoordinates);
	}

	@Override
	public String resolveRelative(ArtifactCoordinates coords) {
        String jar = flatResolver.resolveRelative(coords);
        if (jar!=null) return jar;
        return repoResolver.resolveRelative(coords);
	}

    @Override
    public String getRootDir() {
        return flatRootDir.toString();
    }

    @Override
    public String getRepoDir() {
        return repoDir;
    }
}
