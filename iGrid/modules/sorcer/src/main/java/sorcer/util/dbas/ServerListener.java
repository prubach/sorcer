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

package sorcer.util.dbas;

/**
 * Used by the server to listen for messages from a client
 */
public interface ServerListener extends java.util.EventListener {
	/**
	 * Method invoked when a message is received by the a server
	 * 
	 * @param data
	 *            Message data
	 * @param results
	 *            Stream to write response too
	 * @return void
	 */
	public void service(java.io.InputStream data, java.io.OutputStream results);
}
