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

		StringBuilder result = new StringBuilder(groupId.replace('.', SEP));
		result.append(SEP).append(artifactId).append(SEP).append(version).append(SEP)
				.append(artifactId).append('-').append(version);
		if (classifier != null) {
			result.append('-').append(classifier);
		}
		result.append('.').append(artifactCoordinates.getPackaging());
		return result.toString();

	}

    @Override
    public String getRootDir() {
        return root;
    }

    @Override
    public String getRepoDir() {
        return root;
    }
}
