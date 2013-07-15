package sorcer.protocol;
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
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class ProtocolHandlerRegistry implements URLStreamHandlerFactory {
    private static final Logger log = LoggerFactory.getLogger(ProtocolHandlerRegistry.class);

    private static final ProtocolHandlerRegistry instance = new ProtocolHandlerRegistry();

    private Map<String, Class> /*protocol : handler class */ handlers = new HashMap<String, Class>();

    public void install() {
        URL.setURLStreamHandlerFactory(this);
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        Class handlerClass = handlers.get(protocol);
        if (handlerClass == null) return null;
        try {
            return (URLStreamHandler) handlerClass.newInstance();
        } catch (Exception e) {
            log.error("Error while instantiating protocol handler for " + protocol, e);
            return null;
        }
    }

    public void register(String protocol, Class t) {
        handlers.put(protocol, t);
    }

    public static ProtocolHandlerRegistry get(){
        return instance;
    }

    static {
        instance.install();
    }
}
