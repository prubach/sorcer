/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.boot.util;

import static java.lang.System.out;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sorcer.core.SorcerEnv;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Rafał Krupiński
 */
public class ClassPathVerifier {
	final private static Logger log = LoggerFactory.getLogger(ClassPathVerifier.class);

	public void verifyClassPaths(ClassLoader cl) {

		Multimap<ClassLoader, String> classPaths = HashMultimap.create();
		for (ClassLoader classLoader : getClassLoaderTree(cl)) {
			classPaths.get(classLoader).addAll(getClassPath(classLoader));
		}
		HashMultimap<String, ClassLoader> dest = HashMultimap.create();
		Multimap<String, ClassLoader> classLoaders = Multimaps.invertFrom(classPaths, dest);
		for (String key : classLoaders.keySet()) {
			// don't check bootstrap classpath
			if (SorcerEnv.getRepoDir()==null || !key.contains(SorcerEnv.getRepoDir()))
				continue;
			if (classLoaders.get(key).size() > 1) {
				out.println(key + " is loaded by multiple class loaders:");
				for (ClassLoader kcl : classLoaders.get(key)) {
					out.println("\t" + kcl);
				}
				out.println();
			}
		}
	}

	private Collection<String> getClassPath(ClassLoader curClassLoader) {
		if (curClassLoader instanceof URLClassLoader) {
			URL[] urls = ((URLClassLoader) curClassLoader).getURLs();
			if (urls != null) {
				Collection<String> result = new ArrayList<String>(urls.length);
				for (URL url : urls) {
					result.add(url.toString());
				}
				return result;
			}
		} else {
			log.debug("{} is an unknown ClassLoader", curClassLoader);
		}
		return Collections.emptyList();
	}

	public List<ClassLoader> getClassLoaderTree(ClassLoader classloader) {
		List<ClassLoader> loaderList = new ArrayList<ClassLoader>();
		while (classloader != null) {
			loaderList.add(classloader);
			classloader = classloader.getParent();
		}
		return loaderList;
	}

}
