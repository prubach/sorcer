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

import com.google.common.collect.MapMaker;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class ArtifactRmiLoader extends RMIClassLoaderSpi {
    /*
     * the default RMIClassLoader uses our ClassLoader with resolved artifact to load the classes. codebase passed
     */

    private static final Logger logger = LoggerFactory.getLogger(ArtifactRmiLoader.class);
    private static final RMIClassLoaderSpi loader = RMIClassLoader.getDefaultProviderInstance();

    private static Resolver resolver;

    private Map<ClassLoaderKey, ClassLoader> loaders = new MapMaker().weakValues().makeMap();

    private static class ClassLoaderKey{
        String codebase;
        ClassLoader parent;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassLoaderKey that = (ClassLoaderKey) o;

            return codebase.equals(that.codebase) && parent.equals(that.parent);
        }

        @Override
        public int hashCode() {
            int result = codebase.hashCode();
            result = 31 * result + parent.hashCode();
            return result;
        }

        private ClassLoaderKey(String codebase, ClassLoader parent) {
            this.codebase = codebase;
            this.parent = parent;
        }
    }

    static {
        try {
            resolver = ResolverHelper.getResolver();
        } catch (ResolverException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
        logger.debug("loadClass name: {}, codebase: {}, defaultLoader: {}", name, codebase, defaultLoader);
        return loader.loadClass(codebase, name, getClassLoader(codebase, defaultLoader));
    }

    @Override
    public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
        logger.debug("loadProxyClass ifaces {}, codebase: {}, defaultLoader: {}", interfaces, codebase, defaultLoader);
        return loader.loadProxyClass(codebase, interfaces, getClassLoader(codebase, defaultLoader));
    }

    private ClassLoader getClassLoader(String codebase, ClassLoader classLoader) throws ClassNotFoundException, MalformedURLException {
        if (codebase == null)
            return classLoader;

        if (classLoader == null)
            classLoader = Thread.currentThread().getContextClassLoader();

        synchronized (codebase.intern()){
            ClassLoaderKey key = new ClassLoaderKey(codebase, classLoader);
            if(loaders.containsKey(key))
                return loaders.get(key);

            ClassLoader result = _getClassLoader(codebase, classLoader);
            loaders.put(key, result);
            return result;
        }
    }

    private ClassLoader _getClassLoader(String codebase, ClassLoader classLoader) throws ClassNotFoundException, MalformedURLException {
        String[] cba = StringUtils.tokenizerSplit(codebase, " ");
        URI uris[] = new URI[cba.length];
        boolean resolve = false;
        try {
            for (int i = 0; i < cba.length; i++) {
                uris[i] = new URI(cba[i]);
                resolve |= cba[i].startsWith("artifact");
            }
            ClassLoader cl = classLoader;
            if (resolve)
                try {
                    cl = new ArtifactClassLoader(uris, cl, resolver);
                } catch (ResolverException e) {
                    logger.warn("Could not resolve {}", codebase, e);
                    throw new ClassNotFoundException("Could not resolve codebase " + codebase, e);
                }
            return cl;
        } catch (URISyntaxException x) {
            throw new MalformedURLException(x.getMessage());
        }
    }

    @Override
    public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
        ClassLoader result = loader.getClassLoader(codebase);
        logger.debug("getClassLoader {} -> {}", codebase, result);
        return result;
    }

    @Override
    public String getClassAnnotation(final Class<?> aClass) {
        String result = loader.getClassAnnotation(aClass);
        logger.debug("getClassAnnotation {} -> {}", aClass, result);
        return result;
    }
}
