package org.sorcersoft.rio;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import org.rioproject.opstring.ClassBundle;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Rafał Krupiński
 */
public class OpStringUtil {
    /**
     * Load a class from Rio ClassBundle
     *
     * @param bundle The part of OpString describing a class and required jars
     * @return class referred by the class bundle
     * @throws MalformedURLException
     * @throws ResolverException
     * @throws ClassNotFoundException
     */
    public static Class loadClass(ClassBundle bundle) throws MalformedURLException, ResolverException, ClassNotFoundException {
        String[] urlStrings = ResolverHelper.resolve(bundle.getArtifact(), ResolverHelper.getResolver(), null);
        URL[] urls = new URL[urlStrings.length];

        for (int i = 0; i < urlStrings.length; i++) {
            String urlString = urlStrings[i];
            urls[i] = new URL(urlString);
        }
        URLClassLoader cl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        return Class.forName(bundle.getClassName(), false, cl);
    }

}
