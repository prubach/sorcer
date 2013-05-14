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
package sorcer.tools.shell.cmds;

import java.io.File;
import java.io.PrintStream;
import java.util.StringTokenizer;

import sorcer.tools.shell.NetworkShell;
import sorcer.tools.shell.ShellCmd;

public class SorcerCmd extends ShellCmd {
	{
		COMMAND_NAME = "ig";

		NOT_LOADED_MSG = "***command not loaded due to conflict";

		COMMAND_USAGE = "ig -h | -n | -d";

		COMMAND_HELP = "Display SORCER_HOME \n  -h  cd to SORCER_HOME\n"
				+"-n  cd to the netlets directory\n  -d  cd to http data root directory.";
	}

	private PrintStream out;

	public SorcerCmd() {
	}

	public void execute() throws Throwable {
		out = NetworkShell.getShellOutputStream();
		StringTokenizer myTk = NetworkShell.getShellTokenizer();
		int numTokens = myTk.countTokens();
		if (numTokens == 0) {
			out.println("SORCER_HOME: " + System.getenv("SORCER_HOME"));
			return;
		}
		String option = myTk.nextToken();
		if (option.equals("ig")) {
			option = myTk.nextToken();
		}

		if (option.equals("-h")) {
			NetworkShell.setRequest("cd " + System.getenv("SORCER_HOME"));
			ShellCmd cmd = (ShellCmd) NetworkShell.getCommandTable().get("ls");
			cmd.execute();
		} else if (option.equals("-n")) {
			NetworkShell.setRequest("cd " + System.getenv("SORCER_HOME")
					+ File.separator + "netlets" + File.separator + "src");
			ShellCmd cmd = (ShellCmd) NetworkShell.getCommandTable().get("ls");
			cmd.execute();
		} else if (option.equals("-d")) {
			NetworkShell.setRequest("cd " + System.getenv("SORCER_HOME")
					+ File.separator + "data");
			ShellCmd cmd = (ShellCmd) NetworkShell.getCommandTable().get("ls");
			cmd.execute();
		} else if (option.equals("~")) {
			NetworkShell.setRequest("cd " + System.getProperty("user.home"));
			ShellCmd cmd = (ShellCmd) NetworkShell.getCommandTable().get("ls");
			cmd.execute();
		}
	}

}
