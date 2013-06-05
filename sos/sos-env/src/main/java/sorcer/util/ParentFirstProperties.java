package sorcer.util;/**
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

import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
public class ParentFirstProperties extends Properties {

    public ParentFirstProperties(Properties defaults) {
        super(defaults);
    }

    /**
     * Searches for the property with the specified key in the default property list.
     * If the key is not found in the default property list, this property list is then checked. The method returns
     * <code>null</code> if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     * @see #defaults
     */
    @Override
    public String getProperty(String key) {
        Object value = null;
        if (defaults != null) {
            value = defaults.getProperty(key);
        }
        if (value == null) {
            value = get(key);
        }
        return value instanceof String ? (String) value : null;
    }

}
