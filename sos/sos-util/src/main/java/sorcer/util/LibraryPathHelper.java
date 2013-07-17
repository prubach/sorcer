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
package sorcer.util;

import java.io.File;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class LibraryPathHelper {
	private static final String KEY_LIB_PATH = "java.library.path";
	private static final Logger log = LoggerFactory.getLogger(LibraryPathHelper.class);

	static Field fieldSysPath;
	static {
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}

	public static synchronized void updateLibraryPath(String libraryPath) {
		File libraryDir = new File(libraryPath);
		if (!libraryDir.exists() || !libraryDir.isDirectory() || !libraryDir.canRead()) {
			throw new IllegalArgumentException("Could not access directory " + libraryPath);
		}

		String systemLibPath = System.getProperty(KEY_LIB_PATH);
        if (systemLibPath == null || systemLibPath.isEmpty()) {
			systemLibPath = libraryPath;
		} else {
			systemLibPath += File.pathSeparator + libraryPath;
		}
		log.info("New {} = {}", KEY_LIB_PATH, systemLibPath);
		System.setProperty(KEY_LIB_PATH, systemLibPath);

		try {
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}
}
