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

import edu.emory.mathcs.util.classloader.URIClassLoader;
import org.rioproject.opstring.ClassBundle;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import sorcer.util.SorcerResolverHelper;

import java.net.*;
import java.util.Arrays;

/**
 * @author Rafał Krupiński
 */
public class OpStringUtil {
    public static Class loadServiceClass(ServiceElement serviceElement, Resolver resolver) throws ResolverException {
        return loadClass(serviceElement.getComponentBundle(), serviceElement, resolver);
    }

    public static Class loadClass(ClassBundle bundle, ServiceElement serviceElement, Resolver resolver) throws ResolverException {
        return loadClass(bundle, serviceElement, Thread.currentThread().getContextClassLoader(), resolver);
    }

    /**
     * Load a class from Rio ClassBundle
     *
     * @param bundle   The part of OpString describing a class and required jars
     * @param resolver
     * @return class referred by the class bundle
     * @throws ResolverException
     */
    public static Class loadClass(ClassBundle bundle, ServiceElement serviceElement, ClassLoader classLoader, Resolver resolver) throws ResolverException {
        String className = bundle.getClassName();
        try {
            return Class.forName(className, false, getClassLoader(serviceElement, bundle, classLoader, resolver));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(className, e);
        }
    }

    public static ClassLoader getClassLoader(ServiceElement serviceElement, ClassBundle classBundle, ClassLoader parentCL, Resolver resolver) throws ResolverException {
        String[] classPath = resolver.getClassPathFor(classBundle.getArtifact(), serviceElement.getRemoteRepositories());
        try {
            return new URIClassLoader(SorcerResolverHelper.toURIs(classPath), parentCL);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(Arrays.toString(classPath), e);
        }
    }
}
