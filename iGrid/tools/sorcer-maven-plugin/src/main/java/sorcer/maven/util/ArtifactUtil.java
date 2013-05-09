package sorcer.maven.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.ArtifactUtils;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @author Rafał Krupiński
 */
public class ArtifactUtil {
	public static String toJavaClassPath(List<Artifact> artifacts) {
		return StringUtils.join(toString(artifacts), File.pathSeparator);
	}

	public static Collection<String> toString(Collection<Artifact> artifacts) {
		Set<String> cp = new HashSet<String>(artifacts.size());
		for (Artifact artifact : artifacts) {
			cp.add(artifact.getFile().getPath());
		}
		return cp;
	}

	public static String key(Artifact artifact) {
		return ArtifactUtils.key(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
	}

	public static String key(org.apache.maven.artifact.Artifact artifact) {
		return key(toAetherArtifact(artifact));
	}

	public static Artifact toAetherArtifact(org.apache.maven.artifact.Artifact artifact) {
		return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
				artifact.getType(), artifact.getVersion());
	}
}
