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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sorcer.util.ArtifactCoordinates;
import sorcer.util.PropertiesLoader;

/**
 * Artifact resolver compatible with layout used in the Sorcer distribution
 *
 * @author Rafał Krupiński
 */
public class MappedFlattenedArtifactResolver extends AbstractArtifactResolver {

	public static final String GROUPDIR_DEFAULT = "commons";
	protected File rootDir;

	protected Map<String, String> groupDirMap = new HashMap<String, String>();

	protected List<String> roots;

	public MappedFlattenedArtifactResolver(File rootDir) {
		this.rootDir = rootDir;
	}

	{
		String resourceName = "META-INF/maven/repolayout.properties";
		Map<String, String> propertyMap = new PropertiesLoader().loadAsMap(resourceName, Thread.currentThread().getContextClassLoader());
		groupDirMap.putAll(propertyMap);

		Collection<String> _roots = new HashSet<String>(groupDirMap.values());
		_roots.add(GROUPDIR_DEFAULT);
		roots = Collections.unmodifiableList(new ArrayList<String>(_roots));

	}

	@Override
	public String resolveAbsolute(ArtifactCoordinates artifactCoordinates) {
		String relPath = resolveRelative(artifactCoordinates);
		if (relPath == null) return null;
		else return new File(rootDir, relPath).getPath();
	}

	@Override
	public String resolveRelative(ArtifactCoordinates coords) {
		String groupId = coords.getGroupId();
		String groupDir;
		if (groupDirMap.containsKey(groupId)) {
			groupDir = groupDirMap.get(groupId);
		} else {
			groupDir = GROUPDIR_DEFAULT;
		}
		File relFile = new File(groupDir, coords.getArtifactId() + (coords.getClassifier() != null ? '-' + coords.getClassifier() : "") + '.' + coords.getPackaging());
		File jar = new File(rootDir, relFile.getPath());
		if (jar.exists())
			return relFile.getPath();
		else
			return null;
	}

	@Override
	public String getRootDir() {
		return rootDir.toString();
	}

	@Override
	public String getRepoDir() {
		return rootDir.toString();
	}

	/**
	 * Search a file $simpleName.$packaging in all 1st level subdirectories of tooDir
	 *
	 * @see RepositoryArtifactResolver#resolveSimpleName(String, String)
	 *
	 * @return the first file matching name $simpleName.$packaging or null if none found
	 */
	@Override
	public String resolveSimpleName(String simpleName, String packaging) {
		String fileName = simpleName + "." + packaging;

		List<File> files = new LinkedList<File>();
		for (String subDir : roots) {
			File file = new File(subDir, fileName);
			if (new File(rootDir, file.getPath()).exists()) {
				files.add(file);
			}
		}

		if (files.isEmpty()) {
			return null;
		} else {
			File result = files.get(0);
			if (files.size() > 1) {
				log.warn("Found {} files matching name {}; returning {}", files.size(), fileName, result);
			}
			return result.getPath();
		}
	}
}
