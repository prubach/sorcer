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
package sorcer.core.context;

import sorcer.service.Context;

import java.rmi.RemoteException;

/**
 * Context Management provides methods for managing contexts over the network.
 * The ContextManagement interface is implemented by providers to allow for
 * remote calls to obtain, update and delete contexts.
 */
public class ContextManagementAdapter implements ContextManagement {

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#currentContextList(java.lang.String)
	 */
	@Override
	public String[] currentContextList(String interfaceName)
			throws RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#deleteContext(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteContext(String interfaceName, String methodName)
			throws RemoteException {
		return false;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#getDataContext()
	 */
	@Override
	public Context getContext() throws RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see String#getContextScript()
	 */
	@Override
	public String getContextScript() throws RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#getMethodContext(java.lang.String, java.lang.String)
	 */
	@Override
	public Context getMethodContext(String interfaceName, String methodName)
			throws RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#saveMethodContext(java.lang.String, java.lang.String, sorcer.service.Context)
	 */
	@Override
	public boolean saveMethodContext(String interfaceName, String methodName,
			Context theContext) throws RemoteException {
		return false;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.ContextManagement#getMethodContextScript(java.lang.String, java.lang.String)
	 */
	@Override
	public String getMethodContextScript(String interfaceName, String methodName)
			throws RemoteException {
		return null;
	}
	

}
