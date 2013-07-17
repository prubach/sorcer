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
package sorcer.util.bdb.sdb;

import sorcer.protocol.ProtocolHandlerRegistry;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Note that it's impossible to load this handler with the default package-based mechanism, since it only allows to load
 * classes that are on the system classpath.
 *
 * @author Mike Sobolewski
 */
public class Handler extends URLStreamHandler {
    
	/* (non-Javadoc)
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
	        return new SdbConnection(url);
	}

    public static void register() {
        ProtocolHandlerRegistry.get().register("sos", Handler.class);
    }
}
