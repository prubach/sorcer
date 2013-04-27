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

package sorcer.falcon.base;

import java.util.Map;

import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;

/**
 * An Conditional is a closure with its service dataContext for its free variables
 * in the form of path/value pairs with paths being guards's parameters.
 * 
 * @see Exertion
 * @see WhileExertion
 * @see IfExertion
 */
public interface Conditional {

	/**
	 * The isTrue method is responsible for evaluating the condition component of
	 * the Conditonal. Thus returning the boolean value true or false.
	 * 
	 * @return boolean true or false depending on the condition
	 * @throws ExertionException
	 *             if there is any problem within the isTrue method.
	 */
	public boolean isTrue() throws ExertionException;

	/**
	 * This sets the dataContext of the Condtion component with the current modified
	 * dataContext by the provider for evaluation.
	 * 
	 * @param context
	 *            the modified dataContext by the provider
	 * @throws ContextException
	 *             problem regarding setting or retrieving values in the
	 *             dataContext
	 */
	public void setConditionalContext(Context context) throws ContextException;

	/**
	 * Returns the a referencing environment for the non-local variables inside
	 * this Conditional. It is a mapping between a given variable name to the
	 * corresponding data path in the service dataContext related to this
	 * conditional. This mapping is used by the condition evaluator (isTrue)
	 * method.
	 * 
	 * @return Map for the reference
	 */
	public Map<String, Object> getReferencingMap();

	/**
	 * Returns the boolean expression for the condition. This expression
	 * dictates the evaluation of the isTrue method.
	 * 
	 * @return String this is the boolean expression
	 */
	public String getExpression();
}