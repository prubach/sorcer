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
import java.io.FileFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	/**
	 * path is created as: ${groupId=~s|\.|/|}/${artifactId}/${version}/${artifact}-${version}[-${classifier}].${packaging}
	 */
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

	/**
	 * Find a file with its path matching (the space is used, so it's not an end of comment) ${root}/** /${simpleName}/{1}/${simpleName}-{1}.jar
	 * <p/>
	 * This method doesn't support (return) artifacts with classifiers
	 *
	 * @param simpleName artifactId for which to guess groupId and version
	 * @param packaging
	 * @return path to a jar
	 */
	@Override
	public String resolveSimpleName(String simpleName, String packaging) {
		List<ArtifactCoordinates> results = new LinkedList<ArtifactCoordinates>();
		resolveSimpleName(new File(root), null, simpleName, packaging, results);
		if (results.isEmpty()) {
			return null;
		}
		ArtifactCoordinates result;
		if (results.size() > 1) {
			result = refineVersions(results);
		} else if (results.isEmpty()) {
			return null;
		} else {
			result = results.get(0);
		}
		return resolveRelative(result);
	}

	private ArtifactCoordinates refineVersions(List<ArtifactCoordinates> artifacts) {
		//first check if any of files is in classpath
		List<ArtifactCoordinates> cp = new LinkedList<ArtifactCoordinates>();
		for (ArtifactCoordinates coords : artifacts) {
			try {
				if (resolveVersion(coords.getGroupId(), coords.getArtifactId()).equals(coords.getVersion())) {
					cp.add(coords);
				}
			} catch (IllegalArgumentException x) {
				//ignore
			}
		}
		if (cp.size() == 1) {
			return cp.get(0);
		}
		Collections.sort(artifacts, Collections.reverseOrder());
		//get last
		ArtifactCoordinates result = artifacts.get(0);
		log.warn("Found {} artifacts with artifactId = {}; returning {}", artifacts.size(), result.getArtifactId(), result);
		return result;
	}

	protected void resolveSimpleName(File root, String groupId, String artifactId, String packaging, List<ArtifactCoordinates> results) {
		root.listFiles(new ArtifactDirFilter(groupId, artifactId, packaging, results));
	}

	protected class ArtifactDirFilter implements FileFilter {
		private String groupId;
		private String artifactId;
		private String packaging;
		private List<ArtifactCoordinates> results;


		public ArtifactDirFilter(String groupId, String artifactId, String packaging, List<ArtifactCoordinates> results) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.packaging = packaging;
			this.results = results;
		}

		/*
		 * always return false
		 */
		@Override
		public boolean accept(File file) {
			if (!file.isDirectory()) {
				return false;
			}
			if (file.getName().equals(artifactId)) {
				if (findVersion(file, groupId, packaging, results)) {
					return false;
				}
			}
			String childGroupId = childGroupId(file.getName());
			resolveSimpleName(file, childGroupId, artifactId, packaging, results);

			return false;
		}

		String childGroupId(String added) {
			if (groupId == null) {
				return added;
			} else {
				return groupId + '.' + added;
			}
		}
	}

	protected boolean findVersion(File artifactDir, String groupId, String packaging, List<ArtifactCoordinates> results) {
		File[] files = artifactDir.listFiles(new ArtifactFilter(groupId, packaging, results));
		//returned array contains directories, actual files are in the results list
		return files.length != 0;
	}

	protected class ArtifactFilter implements FileFilter {
		private String groupId;
		private String packaging;
		private List<ArtifactCoordinates> results;

		public ArtifactFilter(String groupId, String packaging, List<ArtifactCoordinates> results) {
			this.groupId = groupId;
			this.packaging = packaging;
			this.results = results;
		}

		@Override
		public boolean accept(File versionDir) {
			String version = versionDir.getName();
			String artifactId = versionDir.getParentFile().getName();
			File artifact = new File(versionDir, artifactId + "-" + version + "." + packaging);
			if (artifact.exists()) {
				results.add(new ArtifactCoordinates(groupId, artifactId, packaging, version, null));
				return true;
			}
			return false;
		}
	}
}
