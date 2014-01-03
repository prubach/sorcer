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
package sorcer.core;

import java.rmi.RemoteException;

/**
 *  Interface used to identify a Sorcer Provider
 * @author Pawel Rubach
 */
public interface DestroyAdmin {
	/**
	 * Destroy the service, if possible, including its persistent storage.
	 * 
	 * @see sorcer.core.DestroyAdmin#destroy()
	 */
	void destroy() throws RemoteException;

	/**
	 * Destroy all services in this node (virtual machine) by calling each
	 * destroy().
	 * 
	 * @see sorcer.core.DestroyAdmin#destroy()
	 */
	void destroyNode() throws RemoteException;

}
