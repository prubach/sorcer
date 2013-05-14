/**
 *
 * Copyright 2013 the original author or authors.
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
