package sorcer.util;
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * @author Rafał Krupiński
 */
public class ClassPath {
    final private static Logger log = LoggerFactory.getLogger(ClassPath.class);

    public static boolean contains(ClassLoader cl, URL entry) {
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;
            if (Arrays.asList(ucl.getURLs()).contains(entry)) return true;
        }
        ClassLoader parent = cl.getParent();
        if (parent != null)
            return contains(parent, entry);
        return false;
    }
}
