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

package sorcer.container.sdi;

import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;

import java.io.IOException;

/**
 * @author Rafał Krupiński
 */
public interface IDiscoveryManagerRegistry {
    ServiceDiscoveryManager getManager() throws IOException;

    ServiceDiscoveryManager getManager(String[] groups) throws IOException;

    LookupCache getLookupCache();

    LookupCache getLookupCache(String[] groups);
}
