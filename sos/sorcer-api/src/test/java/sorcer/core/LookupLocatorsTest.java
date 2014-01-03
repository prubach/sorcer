package sorcer.core;
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


import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Rafał Krupiński
 */
public class LookupLocatorsTest {
    @Test
    public void testInitialized() throws Exception {
        LookupLocators ll = new LookupLocators();
        assertFalse(ll.isInitialized());
    }

    @Test
    public void testSetStatic() throws Exception {
        LookupLocators ll = new LookupLocators();
        String[] staticUrls = {"one", "two"};
        ll.setStaticUrls(staticUrls);

        assertTrue(ll.isInitialized());
        assertArrayEquals(staticUrls, ll.getLookupLocators());
    }

    @Test
    public void testSetDynamic() throws Exception {
        LookupLocators ll = new LookupLocators();
        ll.setDynamicUrls(Arrays.asList("one", "two"));

        assertFalse(ll.isInitialized());
    }

    @Test
    public void testSetBoth() throws Exception {
        LookupLocators ll = new LookupLocators();
        String[] staticUrls = {"one", "two"};
        ll.setStaticUrls(staticUrls);
        ll.setDynamicUrls(Arrays.asList("three", "four"));

        assertTrue(ll.isInitialized());
        assertThat(Arrays.asList(ll.getLookupLocators()), CoreMatchers.hasItems("one", "two", "three", "four"));
    }

}
