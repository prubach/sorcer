package sorcer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.ArtifactUtils;
import org.sonatype.aether.artifact.Artifact;

/**
 * @author Rafał Krupiński
 */
public class ArtifactUtil {
	public static String toJavaClassPath(List<Artifact> artifacts) {
		List<String> cp = new ArrayList<String>(artifacts.size());
		for (Artifact artifact : artifacts) {
			cp.add(artifact.getFile().getPath());
		}
		return StringUtils.join(cp, File.pathSeparator);
	}

	public static String key(Artifact artifact) {
		return ArtifactUtils.key(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
	}

	public static String key(org.apache.maven.artifact.Artifact artifact) {
		return ArtifactUtils.key(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
	}
}
