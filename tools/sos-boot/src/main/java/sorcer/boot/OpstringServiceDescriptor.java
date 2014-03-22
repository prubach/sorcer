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
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import sorcer.core.SorcerEnv;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.util.SorcerResolverHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Rafał Krupiński
 */
public class OpstringServiceDescriptor extends AbstractServiceDescriptor {
    @Inject
    protected Resolver resolver;

    private ServiceElement serviceElement;
    private URL policyFile;

    public OpstringServiceDescriptor(ServiceElement serviceElement, URL policyFile) {
        this.serviceElement = serviceElement;
        this.policyFile = policyFile;
    }

    protected String[] getServiceConfigArgs() {
        return serviceElement.getServiceBeanConfig().getConfigArgs();
    }

    protected String getImplClassName() {
        return serviceElement.getComponentBundle().getClassName();
    }

    @Override
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

    @Override
    protected Set<URL> getCodebase() {
        URL codebaseRoot = SorcerEnv.getCodebaseRoot();
        try {
            OpStringUtil.checkCodebase(serviceElement, codebaseRoot.toExternalForm());
            return getCodebase(serviceElement);
        } catch (IOException e) {
            throw new IllegalStateException("Malformed URL", e);
        }
    }


    private static Set<URL> getCodebase(ServiceElement serviceElement) throws MalformedURLException {
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
                    result.addAll(artifactToUrl(codebaseRoot, artifact));
            }
        }
        return result;
    }

    private static List<URL> artifactToUrl(URL codebase, String artifact) throws MalformedURLException {
        List<URL> result = new ArrayList<URL>();
/*
        String urlBase = codebase.toExternalForm();
        String mvnRootFileUrl = new File(SorcerEnv.getRepoDir()).toURI().toString();

        for (String file : resolver.getClassPathFor(artifact)) {
            String replace = new File(file).toURI().toString().replace(mvnRootFileUrl, urlBase);
            logger.debug("{} -> {}", file, replace);
            result.add(new URL(replace));
        }
*/
        artifact = artifact.replace(':', '/');
        result.add(new URL("artifact:" + artifact + ";" + codebase.toExternalForm() + "@" + codebase.getHost()));
        return result;
    }

    @Override
    public String toString() {
        return "OpstringServiceDescriptor " + serviceElement.getOperationalStringName() + "/" + serviceElement.getName();
    }

    @Override
    protected String getPolicy() {
        return policyFile.toExternalForm();
    }
}
