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

package sorcer.util;

import sorcer.core.SorcerConstants;

/**
 * CmdManager handles command execution, that might be delegated by default back
 * to its invoker, or executed by application specific commands. A command might
 * be executed a separate thread by runIt commands or within the same thread by
 * doIt commands.
 */

public class CmdManager implements Command, Runnable, SorcerConstants {
	private Command command = null;
	public boolean isSelectCmd = true, isDo = true;
	public String selectedCmdName = NONE, loginCmd = SYS_LOGIN;
	public CmdFactory provider = null;
	private Invoker invoker;

	public CmdManager(String type, String providerName, String classPrefix,
			Invoker invoker) {
		provider = CmdFactory.getCmdProvider(type, providerName, classPrefix);
		// Util.debug(this, ">>CmdManager:provider" + this.provider);
		provider.setInvoker(invoker);
		this.invoker = invoker;
	}

	public CmdManager(String type, String providerName, Invoker invoker) {
		provider = CmdFactory.getCmdProvider(type, providerName);
		// Util.debug(this, ">>CmdManager:provider" + this.provider);
		provider.setInvoker(invoker);
		this.invoker = invoker;
	}

	public void register(Command cmd) {
		command = cmd;
	}

	public void setArgs(Object target, Object[] args) {
		command.setArgs(target, args);
	}

	public void runCmd() {
		isDo = true;
		if (command == null) {
			backwardExec();
			return;
		}
		new Thread(this).start();
	}

	public void runIt() {
		isDo = true;
		command = provider.getCmd(selectedCmdName);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void runIt(String cmdName) {
		isDo = true;
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void runIt(String cmdName, Object[] args) {
		isDo = true;
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, args);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void runIt(Mandate mandate) {
		isDo = true;
		String cmdName = "" + mandate.getCommandID();
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, mandate.getArgs());
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void doCmd() {
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.doIt();
	}

	public void doIt() {
		command = provider.getCmd(selectedCmdName);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.doIt();
	}

	public void doIt(String cmdName) {
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.doIt();
	}

	public void doIt(int cmdName) {
		doIt(Integer.toString(cmdName));
	}

	public void doIt(String cmdName, Object[] args) {
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, args);
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setArgs(invoker, args);

		command.doIt();
	}

	public void doIt(Mandate mandate) {
		String cmdName = "" + mandate.getCommandID();
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, mandate.getArgs());
		if (command == null) {
			backwardExec();
			return;
		}
		if (command.getInvoker() == null)
			command.setArgs(invoker, mandate.getArgs());

		command.doIt();
	}

	public void runUndoCmd() {
		isDo = false;
		new Thread(this).start();
	}

	public void doIt(int cmdName, Object[] args) {
		doIt(Integer.toString(cmdName), args);
	}

	public void runUndo() {
		isDo = false;
		command = provider.getCmd(selectedCmdName);
		new Thread(this).start();
	}

	public void runUndo(String cmdName) {
		isDo = false;
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName);
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void runUndo(String cmdName, Object[] args) {
		isDo = false;
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, args);
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		new Thread(this).start();
	}

	public void undoCmd() {
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.undoIt();
	}

	public void undoIt() {
		command = provider.getCmd(selectedCmdName);
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.undoIt();
	}

	public void undoIt(String cmdName) {
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName);
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.undoIt();
	}

	public void undoIt(String cmdName, Object[] args) {
		selectedCmdName = cmdName;
		command = provider.getCmd(cmdName, args);
		if (command.getInvoker() == null)
			command.setInvoker(invoker);
		command.undoIt();
	}

	public Command command() {
		return command;
	}

	public void run() {
		if (command == null)
			backwardExec();
		else {
			// Util.debug(this, "run: " + selectedCmdName);
			if (isDo)
				command.doIt();
			else
				command.undoIt();
		}
	}

	// delegate to invoker, useful is small apps
	// or reimplement in subclasses
	private void backwardExec() {
		// Util.debug(this, "run:no command for: " + selectedCmdName);
		if (isSelectCmd)
			invoker.executeSelect(selectedCmdName);
		else
			invoker.executeAction(selectedCmdName);
	}

	public Invoker getInvoker() {
		return invoker;
	}

	public void setInvoker(Invoker invoker) {
		this.invoker = invoker;
		provider.setInvoker(invoker);
	}
}
