package sorcer.boot;
/**
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import org.rioproject.impl.opstring.OpStringUtil;
import org.rioproject.opstring.ClassBundle;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.ResolverException;
import sorcer.core.SorcerEnv;
import sorcer.util.GenericUtil;
import sorcer.util.SorcerResolverHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class OpstringServiceDescriptor extends ResolvingServiceDescriptor {
    private String name;

    private ServiceElement serviceElement;

    public OpstringServiceDescriptor(ServiceElement serviceElement, URL policyFile) {
        this.serviceElement = serviceElement;
        try {
            setCodebase(getCodebase(serviceElement));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error while processing codebase of " + serviceElement.getName(), e);
        }
        setPolicyFile(policyFile.toExternalForm());
        String[] configArgs = serviceElement.getServiceBeanConfig().getConfigArgs();
        setServiceConfigArgs(Arrays.asList(configArgs));
        setImplClassName(serviceElement.getComponentBundle().getClassName());
        name = serviceElement.getOperationalStringName() + "/" + serviceElement.getName();
    }

    protected Set<URI> getClasspath() {
        String artifact = serviceElement.getComponentBundle().getArtifact();
        try {
            Set<URI> result = new HashSet<URI>();
            Collections.addAll(result, SorcerResolverHelper.toURIs(resolver.getClassPathFor(artifact, serviceElement.getRemoteRepositories())));
            return result;
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Malformed URI", e);
        } catch (ResolverException e) {
            throw new IllegalStateException("Could not resolve artifact " + artifact);
        }
    }

    private static Set<URL> getCodebase(ServiceElement serviceElement) throws MalformedURLException {
        try {
            URL codebaseRoot = SorcerEnv.getCodebaseRoot();
            OpStringUtil.checkCodebase(serviceElement, codebaseRoot.toExternalForm());
        } catch (IOException e) {
            throw new IllegalStateException("Malformed URL", e);
        }

        Set<URL> result = new HashSet<URL>();
        URL[] exportURLs = serviceElement.getExportURLs();
        if (exportURLs.length != 0) {
            Collections.addAll(result, exportURLs);
        } else {
            ClassBundle[] exportBundles = serviceElement.getExportBundles();

            for (ClassBundle exportBundle : exportBundles) {
                URL codebaseRoot = new URL(exportBundle.getCodebase());
                String artifact = exportBundle.getArtifact();
                if (artifact != null)
                    result.add(GenericUtil.toArtifactUrl(codebaseRoot, artifact));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "OpstringServiceDescriptor " + name;
    }
}
