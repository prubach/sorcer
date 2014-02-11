package sorcer.util.rio;
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

import org.rioproject.opstring.ClassBundle;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.SorcerResolverHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Rafał Krupiński
 */
public class OpStringUtil {
    private static Logger log = LoggerFactory.getLogger(OpStringUtil.class);

    public static Class loadClass(ClassBundle bundle, ServiceElement serviceElement) throws MalformedURLException, ResolverException, ClassNotFoundException {
        return loadClass(bundle, serviceElement, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Load a class from Rio ClassBundle
     *
     * @param bundle The part of OpString describing a class and required jars
     * @return class referred by the class bundle
     * @throws MalformedURLException
     * @throws ResolverException
     * @throws ClassNotFoundException
     */
    public static Class loadClass(ClassBundle bundle, ServiceElement serviceElement, ClassLoader parentCL) throws MalformedURLException, ResolverException, ClassNotFoundException {
        try {
            URL[] urls = SorcerResolverHelper.fixUrls(ResolverHelper.resolve(bundle.getArtifact(), ResolverHelper.getResolver(), serviceElement.getRemoteRepositories()));
            URLClassLoader cl = new URLClassLoader(urls, parentCL);
            return Class.forName(bundle.getClassName(), false, cl);
        } catch (URISyntaxException e) {
            throw new ResolverException("Error in resulting URLs", e);
        }
    }

    /**
     * Ensure that list of bundles is exactly one element and return that element.
     */
    public static ClassBundle getClassBundle(ClassBundle... bundles) {
        if (bundles.length != 1)
            throw new IllegalArgumentException("OpString service with no codebase is not supported");
        return bundles[0];
    }

    /**
     * Parse operational string
     *
     * @throws java.io.IOException
     */
    public static OperationalString getOpString(String opString) throws IOException {
        OperationalString[] operationalStrings;
        try {
            OpStringStrLoader opStringLoader = new OpStringStrLoader();
            operationalStrings = opStringLoader.parseOperationalString(opString);
        } catch (Exception e) {
            log.error("Error while parsing operational string", e);
            throw new IllegalStateException("Error while parsing operational string: " + e.getMessage());
        }
        if (operationalStrings.length != 1)
            throw new IllegalArgumentException("OpString file must contain exactly one OpString");
        return operationalStrings[0];
    }

    /**
     * Resolve exported class from a bundle, handle Rio exceptions
     *
     * @throws IOException
     */
    public static Class loadClass2(ClassBundle bundle, ServiceElement serviceElement) throws IOException {
        String type = bundle.getClassName();
        try {
            return loadClass(bundle, serviceElement);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + type + " was not found in " + bundle, e);
        } catch (ResolverException e) {
            throw new IllegalArgumentException("Class " + type + " was not found in " + bundle, e);
        }
    }

    /**
     * Return single service from operational string
     * @throws IllegalArgumentException when there is zero or more than 1 services in the opstring
     */
    public static ServiceElement getServiceElement(OperationalString operationalString) {
        ServiceElement[] services = operationalString.getServices();
        if (services.length != 1)
            throw new IllegalArgumentException("OpString with more than 1 services are not supported");
        return services[0];
    }

}
