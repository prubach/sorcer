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
import sorcer.core.SorcerEnv;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.util.GenericUtil;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ResolvingServiceDescriptor extends AbstractServiceDescriptor {
    @Inject
    protected Resolver resolver;

    private Set<Artifact> classpath;

    protected ResolvingServiceDescriptor() {
    }

    public ResolvingServiceDescriptor(String codebase, String policyFile, String classpath, String implClassName, String configFile) {
        setCodebase(codebase(asArtifacts(codebase)));
        this.classpath = asArtifacts(classpath);
        setImplClassName(implClassName);
        setServiceConfigArgs(Arrays.asList(configFile));
        setPolicyFile(policyFile);
    }

    protected static Set<Artifact> asArtifacts(String artifact) {
        Set<Artifact> result = new HashSet<Artifact>();
        if (artifact != null)
            result.add(new Artifact(artifact));
        return result;
    }

    public static Set<URL> codebase(Set<Artifact> codebase) {
        Set<URL> result = new HashSet<URL>();
        for (Artifact artifact : codebase)
            result.add(GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), artifact.getGAV()));
        return result;
    }

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
}
