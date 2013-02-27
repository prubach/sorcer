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

package sorcer.falcon.validation.conditionalJob.requestor;

import sorcer.falcon.validation.base.Function;

/**
 * Implementation of the Function interface. This class uses the command pattern
 * for evaluating the function. In this case the function is a method.
 * 
 * @author Michael Alger
 */
public class FunctionImpl implements Function {

	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor
	 */
	public FunctionImpl() {
		//do nothing
	}

	/**
	 * Returns the scalar value of the function, which uses the command pattern
	 * @param x double
	 * @return double
	 */
	public double evaluate(double x) {
		return x*x*x;
	}
}
