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

public interface Process {
	/**
	 * Method invoked to set the server monitoring flag
	 * 
	 * @param value
	 *            true if a server is monitored, otherwise false
	 * @return void
	 */
	public void isMonitored(boolean value);

	/**
	 * Method invoked to kill the server
	 * 
	 * @return void
	 */
	public void stopProcess();

	/**
	 * Returns the application name of the process
	 * 
	 * @return String
	 */
	public String appName();

	/**
	 * Returns the application admin password
	 * 
	 * @return String
	 */
	public String passwd();
}
