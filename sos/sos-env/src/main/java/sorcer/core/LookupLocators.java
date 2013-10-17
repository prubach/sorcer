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


import java.util.List;

/**
 * Keep a list of lookup locator URLs. Two internal lists are kept - static locators that are read by SorcerEnv from
 * sorcer.env file, and dynamic (changing in time) locator list, which is maintained by other class, e.g. read from AWS API
 *
 * @author Rafał Krupiński
 */
public class LookupLocators {
    private String[] staticUrls = new String[0];
    private String[] dynamicUrls = new String[0];
    private String[] allUrls = staticUrls;

    private boolean initialized;

    /**
     * @return all lookup locator URLs
     */
    public String[] getLookupLocators() {
        return allUrls;
    }

    /**
     * Set list of dynamic locator list
     *
     * @param dynamicUrls list of lookup locator URLs read from external dynamic source
     */
    public void setDynamicUrls(List<String> dynamicUrls) {
        String[] newDynamic = dynamicUrls.toArray(new String[dynamicUrls.size()]);
        allUrls = join(staticUrls, newDynamic);
        this.dynamicUrls = newDynamic;
    }

    public void setStaticUrls(String[] urls) {
        allUrls = join(urls, dynamicUrls);
        this.dynamicUrls = urls;
        initialized = true;
    }

    /**
     * @return true if the static list was set, even to an empty list
     */
    public boolean isInitialized() {
        return initialized;
    }

    private String[] join(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
