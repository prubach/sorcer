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

package sorcer.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Convert classpath returned by Rio Resolver to URLs or URIs
 *
 * @author Rafał Krupiński
 */
public class SorcerResolverHelper {
    public static URI[] toURIs(String[] filePaths) throws URISyntaxException {
        URI[] result = new URI[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            result[i] = toURI(filePaths[i]);
        }
        return result;
    }

    public static URL[] toURLs(String[] filePaths) throws MalformedURLException {
        URL[] result = new URL[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            result[i] = toURI(filePaths[i]).toURL();
        }
        return result;
    }

    public static URI toURI(String filePath) {
        return new File(filePath).toURI();
    }
}
