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

import sorcer.service.EvaluationException;

public class ParException extends EvaluationException {

	private String varName = null;
	private Exception exception = null;
	
	public ParException() {
	}
	/**
	 * Constructs a new VariableException with an embedded exception.
	 * 
	 * @param exception
	 *            embedded exception
	 */
	public ParException(Exception exception) {
		super(exception);
	}
	
	public ParException(String msg, Exception e) {
		super(msg);
		e.printStackTrace();
	}

	public ParException(String msg) {
		super(msg);
	}
	
	public ParException(String msg, String varName, Exception exception) {
		super(msg);
		this.varName = varName;
		this.exception = exception;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public Exception getException() {
		return exception;
	}
}
