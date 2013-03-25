/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
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

package sorcer.core.invoker;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.service.Context;
import sorcer.service.EvaluationException;

/**
 * @author Mike Sobolewski
 */

/**
 * Invoker interface is context driven invocation via instances of Context type.
 */
public interface ServiceInvoker {
	
	/** Logger for logging information about instances of this type */
	static final Logger logger = Logger.getLogger(ServiceInvoker.class.getName());
	
	public Context invoke(Context... contexts) throws RemoteException, EvaluationException;
	
}
