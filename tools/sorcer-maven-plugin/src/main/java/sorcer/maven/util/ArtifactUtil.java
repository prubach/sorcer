/*
 * Copyright 2013 Rafał Krupiński.
 * Copyright 2013 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
