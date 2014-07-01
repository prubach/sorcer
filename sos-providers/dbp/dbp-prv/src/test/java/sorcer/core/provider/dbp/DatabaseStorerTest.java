package sorcer.core.provider.dbp;
/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.client.ProxyHelper;
import sorcer.service.Accessor;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.*;

/**
 * @author Rafał Krupiński
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api", "org.sorcersoft.sorcer:dbp-api"})
public class DatabaseStorerTest {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStorerTest.class);

    public static final String TXT = "This is me";
    private static final String TXT2 = "This is me again";

    @Test(/*timeout = 5000*/)
    public void testDbp() throws Exception {
        IDatabaseProvider databaseProvider = Accessor.getService(IDatabaseProvider.class);
        assertTrue(ProxyHelper.isReady(databaseProvider));
        assertNotNull("No IDatabaseProvider found", databaseProvider);

        URL url = null;
        try {
            url = databaseProvider.storeObject(TXT);
            Thread.sleep(2000);
            log.info("url = {}", url);
            verify(url, TXT);

            databaseProvider.updateObject(url, TXT2);
            Thread.sleep(2000);
            verify(url, TXT2);
        } finally {
            if (url != null)
                databaseProvider.deleteObject(url);
        }
    }

    private void verify(URL url, String expected) throws IOException {
        URLConnection urlConnection = url.openConnection();
        assertNotNull(urlConnection);
        Object content = urlConnection.getContent();
        assertTrue(content instanceof String);
        String result = (String) content;
        assertEquals(expected, result);
    }
}
