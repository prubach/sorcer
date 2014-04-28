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

package sorcer.boot.destroy;

import com.sun.jini.admin.DestroyAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * @author Rafał Krupiński
 */
public class RiverServiceDestroyer implements ServiceDestroyer {
    private static final Logger log = LoggerFactory.getLogger(RiverServiceDestroyer.class);
    private DestroyAdmin backend;

    public RiverServiceDestroyer(DestroyAdmin backend) {
        this.backend = backend;
    }

    @Override
    public void destroy() {
        try {
            backend.destroy();
        } catch (NullPointerException e) {
            log.debug("Error while destroying local object {}", backend, e);
        } catch (RemoteException e) {
            throw new IllegalStateException("Error while destroying local object " + backend, e);
        }
    }
}
