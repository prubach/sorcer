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

import java.io.Serializable;
import java.rmi.RemoteException;

import sorcer.co.tuple.Parameter;
import sorcer.service.Context;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Identity;

/**
 * @author Mike Sobolewski
 */

/**
 * The invoker interface defines dataContext driven invocations on a service dataContext
 * containing its parameters (paths) and arguments (values). The requested
 * invocation is specified by the array of contexts.
 * 
 * The semantics for how parameters can be declared and how the arguments get
 * passed to the parameters of callable unit are defined by the language, but
 * the details of how this is represented in any particular computing system
 * depend on the calling conventions of that system. A dataContext-driven computing
 * system defines callable unit called invokers used within a scope of service
 * contexts, data structures defined in SORCER.
 * 
 * A service dataContext is dictionary (associative array) composed of a collection
 * of (key, value) pairs, such that each possible key appears at most once in
 * the collection. Keys are considered as parameters and values as arguments of
 * the service invokers accepting service contexts as their input data. A key is
 * expressed by a path of attributes like directories in paths of a file system.
 * Paths define a namespace of the dataContext parameters. A dataContext argument is any
 * object referenced by its path or returned by a dataContext invoker referenced by
 * its path inside the dataContext. An ordered list of parameters is usually
 * included in the definition of an invoker, so that, each time the invoker is
 * called, the dataContext arguments for that call can be assigned to the
 * corresponding parameters of the invoker. The dataContext values for all paths
 * inside the dataContext are defined explicitly by corresponding objects or
 * calculated by corresponding invokers. Thus, requesting a value for a path in
 * a dataContext is a computation defined by a invoker composition within the scope
 * of the dataContext.
 */
@SuppressWarnings("rawtypes")
public interface Invoking<T> extends Evaluation<T>, Serializable {

	public T invoke(Parameter... entries) throws RemoteException,
		EvaluationException;
	
	public T invoke(Context context, Parameter... entries) throws RemoteException,
			EvaluationException;
	
}
