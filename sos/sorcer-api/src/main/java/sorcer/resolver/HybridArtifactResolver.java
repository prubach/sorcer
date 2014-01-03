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

import sorcer.util.ArtifactCoordinates;

import java.io.File;

/**
 * @author Paweł Rubach
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

    public String resolveFlatRelative(ArtifactCoordinates coords) {
        String jar = flatResolver.resolveRelative(coords);
        if (jar!=null) return jar;
        return null;
    }

    public String resolveRepoRelative(ArtifactCoordinates coords) {
        String jar = repoResolver.resolveRelative(coords);
        if (jar!=null) return jar;
        return null;
    }


    @Override
    public String getRootDir() {
        return flatRootDir.toString();
    }

    @Override
    public String getRepoDir() {
        return repoDir;
    }

	/**
	 * First try to resolve with simple resolver
	 */
	@Override
	public String resolveSimpleName(String simpleName, String packaging) {
		String result = flatResolver.resolveSimpleName(simpleName, packaging);
		return result != null ? result : repoResolver.resolveSimpleName(simpleName, packaging);
	}
}
