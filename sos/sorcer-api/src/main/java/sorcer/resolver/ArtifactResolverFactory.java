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

import sorcer.core.SorcerEnv;
import sorcer.util.Artifact;
import sorcer.util.ArtifactCoordinates;

/**
 * Creates an artifact resolver instance. If there is a maven repository (its
 * path may be configured in sorcer.env; ~/.m2/repository is the default) it is
 * checked against org.sorcersoft.sorcer:sorcer-api artifact. If such an artifact
 * exists this repository is used. Otherwise we use ${sorcer.home}/lib as a root
 * of flattened jar repository.
 * 
 * @author Rafał Krupiński
 */
public class ArtifactResolverFactory {

	public ArtifactResolver createResolver() {
        //repoRoot = new File();
        //File repoRoot = null;
        String rootDir = SorcerEnv.getHomeDir().toString() + (File.separator + "lib");// SorcerEnv.getRepoDir();
        if (rootDir !=null) {
            File rootFile = new File(rootDir);
            ArtifactResolver artifactResolver = new MappedFlattenedArtifactResolver(rootFile);
            //ArtifactResolver artifactResolver = new RepositoryArtifactResolver(repoRoot.getPath());
            if (tryResolve(Artifact.getSorcerApi(), artifactResolver)) {
                return new HybridArtifactResolver(rootFile, SorcerEnv.getRepoDir());
            }
        }
        rootDir = SorcerEnv.getRepoDir();
        if (rootDir==null) throw new RuntimeException("Problem determining resolving mechanism!");
        return new RepositoryArtifactResolver(rootDir);
	}

	private boolean tryResolve(ArtifactCoordinates coords, ArtifactResolver resolver) {
		String artifactPath = resolver.resolveAbsolute(coords);
		return artifactPath!=null;
	}
}
