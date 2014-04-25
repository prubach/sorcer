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
package sorcer.tools.shell;

import net.jini.config.Configuration;

@SuppressWarnings("rawtypes")
abstract public class ShellCmd {

	protected String COMMAND_NAME;

	protected String NOT_LOADED_MSG;

	protected String COMMAND_USAGE;

	protected String COMMAND_HELP;

    public void execute() throws Throwable {
    }

    public void execute(INetworkShell shell) throws Throwable {
        execute();
    }

    protected Configuration config;

	public String getCommandWord() {
		return COMMAND_NAME;
	}

	public String getUsage(String subCmd) {
		return COMMAND_USAGE;
	}

	public String getShortHelp() {
		return COMMAND_HELP;
	}

	public String getLongDescription(String subCmd) {
		return COMMAND_HELP;
	}

	public String nameConflictDetected(Class<?> conflictClass) {
		return NOT_LOADED_MSG;
	}

	public void initializeSubsystem() {
	}

	public void endSubsystem() {
	}

	public String toString() {
		return getClass().getName() + ": " + COMMAND_NAME;
	}

    public void setConfig(Configuration config) {
        this.config = config;
    }
}
