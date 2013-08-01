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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static sorcer.util.JavaSystemProperties.LIBRARY_PATH;

/**
 * @author Rafał Krupiński
 */
public class LibraryPathHelper {
	private static final Logger log = LoggerFactory.getLogger(LibraryPathHelper.class);

	static Field fieldSysPath;
	static {
        fieldSysPath = prepareSysPathField();
    }

    private static Set<String> initElements() {
        Set<String> result = new HashSet<String>();
        Collections.addAll(result, System.getProperty(LIBRARY_PATH,"").split(File.pathSeparator));
        return result;
    }

    private static Field prepareSysPathField() {
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
            return fieldSysPath;
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}

	public static synchronized void updateLibraryPath(String libraryPath) {
		File libraryDir = new File(libraryPath);
		if (!libraryDir.exists() || !libraryDir.isDirectory() || !libraryDir.canRead()) {
			throw new IllegalArgumentException("Could not access directory " + libraryPath);
		}

        Set<String> elements = initElements();
        if(elements.contains(libraryPath)){
            return;
        }

        String systemLibPath = StringUtils.join(elements, File.pathSeparator);
		log.info("New {} = {}", LIBRARY_PATH, systemLibPath);
        doUpdateLibraryPath(systemLibPath);
	}

    private static void doUpdateLibraryPath(String libraryPath) {
        System.setProperty(LIBRARY_PATH, libraryPath);

		try {
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}
}
