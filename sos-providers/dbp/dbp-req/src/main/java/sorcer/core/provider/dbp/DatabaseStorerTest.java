package sorcer.core.provider.dbp;
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
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.core.SorcerEnv;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.resolver.Resolver;
import sorcer.service.Accessor;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Rafał Krupiński
 */
public class DatabaseStorerTest {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStorerTest.class);

    public static final String TXT = "This is me";
    private static final String TXT2 = "This is me again";

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        if (System.getProperty("org.rioproject.resolver.jar") == null)
            System.setProperty("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));
        System.setSecurityManager(new SecurityManager());

        try {
			SorcerEnv.debug = true;
			ServiceRequestor.prepareCodebase();
            new DatabaseStorerTest().run();
        } catch (Exception x) {
            log.error("Error in test", x);
        }
    }

    private void run() throws Exception {
        IDatabaseProvider databaseProvider = Accessor.getService(IDatabaseProvider.class);
        assertNotNull(databaseProvider);

        URL url = databaseProvider.storeObject(TXT);
        log.info("url = {}", url);
        verify(url, TXT);

        databaseProvider.updateObject(url, TXT2);
        verify(url, TXT2);

        databaseProvider.deleteObject(url);
    }

    private void verify(URL url, String expected) throws IOException {
        Object content = url.openConnection().getContent();
        assertTrue(content instanceof String);
        String result = (String) content;
        assertEquals(expected, result);
    }
}
