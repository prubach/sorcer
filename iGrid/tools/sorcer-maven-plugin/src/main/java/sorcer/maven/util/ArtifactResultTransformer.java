package sorcer.maven.util;

import com.google.common.base.Function;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResult;

import javax.annotation.Nullable;

/**
* @author Rafał Krupiński
*/
public class ArtifactResultTransformer implements Function<ArtifactResult, Artifact> {
	@Nullable
	@Override
	public Artifact apply(@Nullable ArtifactResult input) {
		return input == null ? null : input.getArtifact();
	}
}
