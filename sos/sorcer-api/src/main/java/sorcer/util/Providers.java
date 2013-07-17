package sorcer.util;
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


import sorcer.core.Provider;

import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * @author Rafał Krupiński
 */
public class Providers {
    static Logger logger = Logger.getLogger(Providers.class.getName());
    /**
     * Test if provider is still replying.
     *
     * @param provider the provider to check
     * @return true if a provider is alive, otherwise false
     * @throws java.rmi.RemoteException
     */
    public static boolean isAlive(Provider provider)
            throws RemoteException {
        if (provider == null)
            return false;
        try {
            provider.getProviderName();
            return true;
        } catch (RemoteException e) {
            logger.throwing(Providers.class.getName(), "isAlive", e);
            throw e;
        }
	}
}
