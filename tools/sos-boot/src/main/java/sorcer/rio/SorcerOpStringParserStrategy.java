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

package sorcer.rio;

import org.rioproject.impl.opstring.OpStringParser;
import org.rioproject.impl.opstring.OpStringParserSelectionStrategy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Rafał Krupiński
 */
public class SorcerOpStringParserStrategy implements OpStringParserSelectionStrategy {
    @Override
    public OpStringParser findParser(Object source) {
        if (source instanceof File)
            return findParser(((File) source).toURI());
        else if (source instanceof URL)
            try {
                return findParser(((URL) source).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        else
            throw new UnsupportedOperationException("There is no support for " + source.getClass().getName() + " source");
    }

    public OpStringParser findParser(URI uri) {
        String path = uri.toString();
        if (path.endsWith(".groovy")) {
            return new SerialisedOpStringParser();
        } else
            throw new UnsupportedOperationException("There is no support for " + path + " format");

    }
}
