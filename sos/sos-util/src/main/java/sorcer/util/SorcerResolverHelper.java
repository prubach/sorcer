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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Rafał Krupiński
 */
public class SorcerResolverHelper {
    public static String[] fixUriStrings(String[] uris) {
        if (!System.getProperty("os.name").startsWith("Windows"))
            return uris;

        String[] result = new String[uris.length];
        for (int i = 0; i < uris.length; i++) {
            result[i] = fixUri(uris[i]);
        }
        return result;
    }

    public static URI[] fixUris(String[] uris) throws URISyntaxException {
        URI[] result = new URI[uris.length];
        for (int i = 0; i < uris.length; i++) {
            result[i] = new URI(fixUri(uris[i]));
        }
        return result;
    }

    public static URL[] fixUrls(String[] uris) throws URISyntaxException, MalformedURLException {
        URL[] result = new URL[uris.length];
        for (int i = 0; i < uris.length; i++) {
            result[i] = new URI(fixUri(uris[i])).toURL();
        }
        return result;
    }

    public static String fixUri(String uriString) {
        if (uriString.startsWith("file:"))
            return uriString.replace('\\', '/');
        else
            return uriString;

    }
}
