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
import org.rioproject.resolver.RemoteRepository;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import org.rioproject.url.artifact.ArtifactURLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Rafał Krupiński
 */
public class ArtifactClassLoader extends URIClassLoader {
    private Resolver resolver;
    private static final Logger logger = LoggerFactory.getLogger(ArtifactRmiLoader.class);

    public ArtifactClassLoader(ClassLoader parent, Resolver resolver) {
        super(new URI[0], parent);
        this.resolver = resolver;
    }

    public void addURI(URI artifact) {
        if (!"artifact".equals(artifact.getScheme())) {
            super.addURI(artifact);
            return;
        }
        try {
            URI[] resolved = resolve(artifact, resolver);
            for (URI uri : resolved)
                super.addURI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        } catch (ResolverException e) {
            try {
                logger.warn("Trying again to resolve: " + artifact + " in repos: " + new ArtifactURLConfiguration(artifact.toString()).getRepositories().toString());
                URI[] resolved = resolve(artifact, resolver);
                for (URI uri : resolved)
                    super.addURI(uri);
            } catch (Exception ee) {
                throw new IllegalArgumentException(ee);
            }
        }
    }

    private static URI[] resolve(URI artifactUri, Resolver resolver) throws URISyntaxException, ResolverException {
        String input = artifactUri.toString();
        /*String path = artifactUri.getPath();
        if (path == null) {
            throw new URISyntaxException(input, "URI has null path");
        }*/

        ArtifactURLConfiguration configuration = new ArtifactURLConfiguration(input);
        String artifact = configuration.getArtifact();
        String A = "artifact:";
        if (artifact.startsWith(A))
            artifact = artifact.substring(A.length());

        String[] cp = resolver.getClassPathFor(artifact, configuration.getRepositories());
        URI[] result = new URI[cp.length];
        for (int i = 0; i < cp.length; i++)
            result[i] = new File(cp[i]).toURI();
        return result;
    }
}
