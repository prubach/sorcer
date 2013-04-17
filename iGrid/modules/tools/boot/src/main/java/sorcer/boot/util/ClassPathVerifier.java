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
			if (!key.contains(".m2/repository"))
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
		loaderList.add(null);
		Collections.reverse(loaderList);
		return loaderList;
	}

}
