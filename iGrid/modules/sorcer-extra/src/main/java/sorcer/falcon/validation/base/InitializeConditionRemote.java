/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.falcon.validation.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;

/**
 * Remote Interface for SimpleProvider
 * @author Michael Alger
 */
public interface InitializeConditionRemote extends Remote {
	
    /**
     * Service method which initialize the sentinel for the if condition 
     * @param context ProviderContext 
     * @throws RemoteException
     * @return ServiceContext
     */
    public Context initializeCondition(Context context) throws RemoteException;
}
