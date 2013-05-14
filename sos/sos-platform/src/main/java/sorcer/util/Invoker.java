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
package sorcer.util;

/**
 * Invoker interface is used by CmdManager in the Command pattern. Each GApp
 * application should implement a factory <appName>.<type>.<appName>CmdProvider
 * class providing its commands that implement gapp.lnch.Command interface.
 * Invokers can dispatch commands using a CmdManager or execute default commands
 * themselves via this interface.
 */

public interface Invoker {

	// should be implemented is classes when CmdFactory does not have
	// a selected command to be executed
	public boolean executeSelect(String action);

	// should be implemented is classes when CmdFactory does not have
	// an action command to be executed
	public boolean executeAction(String cmd);

	// should be implemented is classes when CmdFactory does not have
	// a command of mandate to be executed
	public boolean executeMandate(Mandate mandate);
}
