/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.rmi;

import edu.emory.mathcs.util.classloader.URIClassLoader;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import org.rioproject.url.artifact.ArtifactURLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ArtifactClassLoader extends URIClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(ArtifactClassLoader.class);
    private static final String ARTIFACT_PREFIX = "artifact:";
//    private static ConcurrentMap<URI, URI[]> resolved = new ConcurrentHashMap<URI, URI[]>();

    public ArtifactClassLoader(URI[] uris, ClassLoader parent, Resolver resolver) throws URISyntaxException, ResolverException {
        super(resolve(uris, resolver), parent);
    }

    private static URI[] resolve(URI[] artifact, Resolver resolver) throws URISyntaxException, ResolverException {
        Set<URI> result = new HashSet<URI>();
        for (URI uri : artifact) {
            Collections.addAll(result, resolve(uri, resolver));
        }
        return result.toArray(new URI[result.size()]);
    }

    private static URI[] resolve(URI artifactUri, Resolver resolver) throws URISyntaxException, ResolverException {
        String input = artifactUri.toString();
        /*String path = artifactUri.getPath();
        if (path == null) {
            throw new URISyntaxException(input, "URI has null path");
        }*/

//        synchronized (input.intern()) {
//            if (resolved.containsKey(artifactUri))
//                return resolved.get(artifactUri);

        ArtifactURLConfiguration configuration = new ArtifactURLConfiguration(input);
        String artifact = configuration.getArtifact();
        if (artifact.startsWith(ARTIFACT_PREFIX))
            artifact = artifact.substring(ARTIFACT_PREFIX.length());

        String[] cp = resolver.getClassPathFor(artifact, configuration.getRepositories());
        URI[] result = new URI[cp.length];
        for (int i = 0; i < cp.length; i++)
            result[i] = new File(cp[i]).toURI();
//            resolved.put(artifactUri, result);
        return result;
//        }
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException x) {
            com.sun.jini.start.ClassLoaderUtil.displayClassLoaderTree(this);
            logger.warn("Class {} not found", name);
            throw x;
        }
    }
}
