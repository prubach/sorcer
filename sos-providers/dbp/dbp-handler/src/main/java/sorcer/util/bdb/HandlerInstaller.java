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

package sorcer.util.bdb;

import com.sun.jini.start.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.protocol.ProtocolHandlerRegistry;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * @author Rafał Krupiński
 */
public class HandlerInstaller {
    private static final Logger log = LoggerFactory.getLogger(HandlerInstaller.class);

    public HandlerInstaller(String[] config, LifeCycle lifeCycle) {
        ProtocolHandlerRegistry registry;
        try {
            registry = new ProtocolHandlerRegistry();
            URL.setURLStreamHandlerFactory(registry);
        } catch (Error ignored) {
            log.warn("Could not install URLStreamHandlerFactory, current: {}", getCurrentInstance());
            return;
        }
        registry.register("sos", new sorcer.util.bdb.sdb.Handler());
    }

    private static URLStreamHandlerFactory getCurrentInstance() {
        try {
            Field factoryField = URL.class.getField("factory");
            if (!factoryField.isAccessible())
                factoryField.setAccessible(true);
            return (URLStreamHandlerFactory) factoryField.get(null);
        } catch (IllegalAccessException ignored) {
            return null;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }
}
