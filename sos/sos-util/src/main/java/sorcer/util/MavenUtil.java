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


import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static sorcer.util.Collections.i;

/**
 * @author Rafał Krupiński
 */
public class MavenUtil {
    public static String findVersion(Class<?> serviceType) {
        URL jar = serviceType.getProtectionDomain().getCodeSource().getLocation();
        JarURLConnection urlConnection = null;
        try {
            urlConnection = (JarURLConnection) jar.openConnection();
            JarFile jarFile = urlConnection.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            for (JarEntry entry : i(entries)) {
                String name = entry.getName();
                System.out.println(name);
                if (name.startsWith("META-INF/") && name.endsWith("/pom.properties")) {
                    Properties properties = new Properties();
                    properties.load(jarFile.getInputStream(entry));
                    return properties.getProperty("version");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
