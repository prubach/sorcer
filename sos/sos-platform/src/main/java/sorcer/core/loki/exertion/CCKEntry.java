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
package sorcer.core.loki.exertion;

import java.security.Key;
import java.util.Map;

import net.jini.core.entry.Entry;
import net.jini.id.Uuid;

public class CCKEntry implements Entry {

    private static final long serialVersionUID = 323371062406569479L;

    public Map<Uuid, Key> ccKeys;

    static public CCKEntry get(Map<Uuid, Key> keys) {
        CCKEntry CCK = new CCKEntry();
        CCK.ccKeys = keys;
        return CCK;
    }

    public String getName() {
        return "Complimentary Compound Key Entry";
    }

}
