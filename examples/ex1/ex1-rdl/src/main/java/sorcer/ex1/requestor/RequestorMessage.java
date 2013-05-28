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
package sorcer.ex1.requestor;

import sorcer.ex1.Message;

import java.io.Serializable;

public class RequestorMessage implements Message, Serializable {

	private static final long serialVersionUID = 2010624006315717678L;

	private String name;

	public RequestorMessage(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.Message#getMessgae()
	 */
	@Override
	public String getMessage() {
		return "Hi " + name + "!";
	}
	
	@Override
	public String toString() {
		return getMessage();
	}
}