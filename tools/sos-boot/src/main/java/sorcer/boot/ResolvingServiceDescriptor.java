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

package sorcer.boot;

import org.rioproject.resolver.Artifact;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import sorcer.provider.boot.AbstractServiceDescriptor;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class ResolvingServiceDescriptor extends AbstractServiceDescriptor {

    private Set<Artifact> codebase;

    private Set<Artifact> classpath;

    private String implClassName;

    private String configFile;

    @Inject
    protected Resolver resolver;

    public ResolvingServiceDescriptor(String codebase, String classpath, String implClassName, String configFile) {
        this.codebase = asArtifacts(codebase);
        this.classpath = asArtifacts(classpath);

        this.implClassName = implClassName;
        this.configFile = configFile;
    }

    protected static Set<Artifact> asArtifacts(String artifact) {
        Set<Artifact> result = new HashSet<Artifact>();
        if (artifact != null)
            result.add(new Artifact(artifact));
        return result;
    }

    @Override
    protected String getImplClassName() {
        return implClassName;
    }

    @Override
    protected String[] getServiceConfigArgs() {
        return new String[]{configFile};
    }

    @Override
    protected String getPolicy() {
        return null;
    }

    @Override
    protected Set<URL> getCodebase() {
        if (codebase == null)
            return null;
        Set<URL> result = new HashSet<URL>();
        try {
            for (Artifact artifact : codebase)
                result.add(toArtifactUri(artifact).toURL());
            return result;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Error while creating URL", e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Error while creating URL", e);
        }
    }

    @Override
    protected Set<URI> getClasspath() {
        if (classpath == null)
            return null;
        Set<URI> results = new HashSet<URI>();
        try {
            for (Artifact artifact : classpath) {
                String[] cpArray = resolver.getClassPathFor(artifact.getGAV());
                for (String entry : cpArray)
                    results.add(new File(entry).toURI());
            }
        } catch (ResolverException e) {
            throw new IllegalArgumentException("Could not resolve classpath", e);
        }
        return results;
    }

    public static URI toArtifactUri(Artifact a) throws URISyntaxException {
        return new URI("artifact", a.getGAV().replace(':', '/'), null);
    }
}
