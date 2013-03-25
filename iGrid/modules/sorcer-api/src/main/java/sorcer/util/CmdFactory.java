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

import java.util.logging.Logger;

/**
 * An abstract factory for all GApp commands provided by a factory
 * <appName>.<type>.<appName>CmdProvider. Real factories called command
 * providers are subclasses of this class.
 */

abstract public class CmdFactory {
	private static Logger logger = Logger.getLogger(CmdFactory.class.getName());
	public Command cmd = null;
	private static String provider, type;
	public Invoker invoker;

	public static void init(String providerName, String type) {
		CmdFactory.provider = providerName;
		CmdFactory.type = type;
	}

	public static CmdFactory getCmdProvider(String typeName,
			String providerName, String classPrefix) {
		String className = "", cfn;
		type = typeName;
		provider = providerName;

		if (type.startsWith("dbas"))
			cfn = classPrefix + "ProtocolProvider";
		else if (type.startsWith("lnch"))
			cfn = classPrefix + "CmdProvider";
		else {
			// not standard location of the command factory
			type = "lnch";
			cfn = null;
		}

		try {
			if (cfn == null)
				className = providerName + "." + typeName + "." + classPrefix
						+ "CmdProvider";
			else
				className = providerName + "." + typeName + "." + cfn;

			return (CmdFactory) Class.forName(className).newInstance();
		} catch (Exception e) {
			System.err.println("CmdFactory:getCmdProvider: Failed to create: "
					+ className);
			// e.printStackTrace();
			return null;
		}
	}

	public static CmdFactory getCmdProvider(String typeName, String providerName) {
		String className = null, cfn;
		type = typeName;
		provider = providerName;

		String name;
		int i = provider.indexOf('.');
		if (i > 0)
			name = provider.substring(i + 1);
		else
			name = provider;

		if (type.startsWith("dbas"))
			cfn = name + "ProtocolProvider";
		else if (type.startsWith("lnch"))
			cfn = name + "CmdProvider";
		else {
			// not standard location of the command factory
			type = "lnch";
			cfn = null;
		}

		try {
			if (cfn == null)
				className = typeName + "." + providerName + "CmdProvider";
			else
				className = provider.toLowerCase() + "." + type + "." + cfn;

			return (CmdFactory) Class.forName(className).newInstance();
		} catch (Exception e) {
			System.err.println("CmdFactory:getCmdProvider: Failed to create: "
					+ className);
			// e.printStackTrace();
			return null;
		}
	}

	public Command getCmd(String cmdName) {
		String context = getContext(cmdName);
		cmd = null;
		if (context == null) {
			logger.severe("getCmd:No context defined for command: " + cmdName);
		} else {
			cmd = getCmd(cmdName, context);
			if (invoker != null)
				cmd.setInvoker(invoker);
		}
		return cmd;
	}

	abstract public Command getCmd(String cmdName, String context);

	public Command getCmd(String cmdName, Object[] args) {
		Command cmd = getCmd(cmdName);
		cmd.setArgs(invoker, args);
		return cmd;
	}

	abstract protected String getContext(String cmdName);

	// implement in subclasses

	public static Command createCmd(String cmdName, Invoker invoker) {
		CmdFactory cmdProvider = getCmdProvider(type, provider);
		Command cmd = null;
		if (cmdProvider != null) {
			cmd = cmdProvider.getCmd(cmdName);
			cmd.setInvoker(invoker);
		}
		return cmd;
	}

	public static Command createCmd(String cmdName, String provider,
			Invoker invoker) {
		return createCmd(cmdName, "lnch", provider, invoker);
	}

	public static Command createCmd(String cmdName, String type,
			String provider, Invoker invoker) {
		CmdFactory cmdProvider = getCmdProvider(type, provider);
		Command cmd = null;
		if (cmdProvider != null) {
			cmd = cmdProvider.getCmd(cmdName);
			cmd.setInvoker(invoker);
		}
		return cmd;
	}

	public static Command createCmd(String cmdName, Object[] args) {
		CmdFactory cmdProvider = getCmdProvider(type, provider);
		if (cmdProvider != null)
			return cmdProvider.getCmd(cmdName, args);
		else
			return null;
	}

	public void setInvoker(Invoker invoker) {
		this.invoker = invoker;
	}

	public Invoker getInvoker() {
		return invoker;
	}
}
