/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.util.dbas;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

/**
 * The GAppClassLoader implementation loads Java classes from a file system to
 * expend the GApp itself or to load application commands.
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * GAppClassLoader loader = new GAppClassLoader(myClasspath);
 * Class c = loader.load(classname);
 * Object obj = c.newInstance();
 * </pre>
 * 
 * @author Mike Sobolewski
 */
class GAppClassLoader extends ClassLoader {
	Hashtable cache = new Hashtable();
	String classpath;

	/**
	 * Construct a GAppClassLoader for the given path
	 * 
	 * @param path
	 *            absolute or relative path for the class file.
	 */
	public GAppClassLoader(String path) {
		classpath = path;
	}

	/**
	 * Loads content of a class
	 * 
	 * @param file
	 *            suffix
	 * @return media content as byte array
	 */
	private byte[] loadClassData(String name) {
		// load the class data from the connection
		int i = 0;
		File f = new File(classpath + File.separatorChar + name);

		byte[] bytes = new byte[(int) f.length()];

		try {
			FileInputStream in = new FileInputStream(f);
			i = in.read(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (i < 0)
			System.err.println("File read error: " + f.getName());
		return bytes;
	}

	/**
	 * Loads named class.
	 * 
	 * @param name
	 *            the name of the desired Class
	 * @param resolve
	 *            true if the Class needs to be resolved
	 * @return the resulting Class, or null if it was not found.
	 * @throws ClassNotFoundException
	 *             if the class definition could not be found
	 */
	public synchronized Class loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class c;

		if ((c = (Class) cache.get(name)) != null)
			return c;
		try {
			c = Class.forName(name);
		} catch (ClassNotFoundException e) {
			byte bytes[];
			if (name.endsWith(".class"))
				bytes = loadClassData(name);
			else
				bytes = loadClassData(name.replace('.', File.separatorChar)
						+ ".class");

			if (bytes == null)
				throw new ClassNotFoundException();
			// c = defineClass(bytes, 0, bytes.length);
			c = defineClass(name, bytes, 0, bytes.length);

			if (resolve)
				resolveClass(c);
		}
		cache.put(name, c);
		return c;
	}
}
