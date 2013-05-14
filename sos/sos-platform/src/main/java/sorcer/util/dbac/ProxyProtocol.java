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
package sorcer.util.dbac;

import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.Protocol;
import sorcer.util.ProtocolStream;
import sorcer.util.Result;
import sorcer.util.StringUtils;
import sorcer.util.ui.Launcher;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * The ProxyProtocol class implements generic Protocol interface to
 * ApplicationServer. GApp-based applications create a proxy being subclass of
 * ProxyProtocol and use it to pass on commands to an application server.
 * SocketProtocol, ReaderProtocol, HttpProtocol, and ServletProtocol provide the
 * basic database connectivity for GApp-based applications, including database
 * connection management, SQL queries management, authentication, authorization,
 * ACLs, and logging. Select the right one for you application, or extend
 * accordingly to your requirements, or implement own protocol subclass
 * <p>
 */
abstract public class ProxyProtocol implements Protocol, SorcerConstants {
	private static Logger logger = Logger.getLogger(ProxyProtocol.class
			.getName());
	public ProtocolStream stream;
	Vector result = new Vector(200);
	Object outcome;
	public String dsURL;
	protected boolean isController = true;
	protected static SorcerPrincipal principal;

	/**
	 * Make connection to an ApplicationServer, in the case of failure try to
	 * reconnect
	 */
	abstract public void connect();

	/**
	 * Disconnects from an application server
	 */
	public void disconnect() {
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Return true, if the ProxyProtocol is connected to an application server
	 */
	abstract public boolean connected();

	public boolean isAlive() {
		result.removeAllElements();
		sendCmd(IS_ALIVE, OK);
		String result = getResultString();
		if (result == null)
			return false;
		else
			return result.equals(OK);
	}

	public String processCmd(int cmd, String inline) {
		sendCmd(cmd, inline);
		return getResultString();
	}

	public String processCmd(int cmd, int subCmd, String[] data)
			throws IOException {
		// Util.debug(this, "processCmd:cmd=" + cmd + " subCmd: " + subCmd );
		sendCmd(cmd, subCmd, data);
		return getResultString();
	}

	public void sendCmd(int cmd, String inline) {
		checkConnection();
		try {
			stream.writeInt(cmd);
			if (inline != null)
				stream.writeLine(inline);
			if (principal != null)
				stream.writeLine(principal.asString());
			stream.flush();
		} catch (java.io.IOException e) {
			result.removeAllElements();
			e.printStackTrace();
			return;
		}
		readData();
	}

	public void sendCmd(int cmd, int subCmd, String[] data) throws IOException {
		checkConnection();
		try {
			stream.writeInt(cmd);
			stream.writeInt(subCmd);
			if (data != null) {
				stream.writeInt((principal != null) ? data.length + 1
						: data.length);
				for (int i = 0; i < data.length; i++) {
					stream.writeLine(data[i]);
				}
			} else
				stream.writeInt(1);
			if (principal != null)
				stream.writeLine(principal.asString());
			stream.flush();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			throw e;
		}
		readData();
	}

	// command for a given page of SQL results
	public void processPageQueryCmd(int cmd, String query, int page,
			int pageSize) {
		checkConnection();
		try {
			stream.writeInt(cmd);
			stream.writeInt(page);
			stream.writeInt(pageSize);
			stream.writeLine(query);
			if (principal != null)
				stream.writeLine(principal.asString());
			stream.flush();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return;
		}
		readData();
	}

	public String execute(String sql) {
		String q = sql.toLowerCase().trim();
		if ((q.indexOf("update") == 0) || (q.indexOf("delete") == 0)
				|| (q.indexOf("insert") == 0))
			return (String) executeUpdate(sql, true);
		else if (q.indexOf("select") == 0)
			return (String) executeQuery(sql, true);
		// assuming that a query name has first letter
		// stating a type: select or update
		else if (q.charAt(0) == 'u')
			return (String) executeUpdateFor(sql, true);
		else if (q.charAt(0) == 's')
			return (String) executeQueryFor(sql, true);

		// if failed
		return "";
	}

	public Object login(String userName, String userPassword) {
		String out = processCmd(LOGIN, userName + SEP + userPassword);
		if (out != null && out.startsWith("logged")) {
			principal = SorcerPrincipal.fromString(out
					.substring(out.indexOf(SEP) + 1));
			return principal;
		}
		// Util.debug(this, "login:out=" + out);
		// access = new String[4];
		// String[] tokens = Util.tokenize(out, sep);
		// for (int i=0; i<tokens.length; i++)
		// access[i] = tokens[i]
		// access[0] = tokens[1]; //user role
		// access[1] = tokens[2]; //user permissions
		// access[2] = tokens[3]; //user id
		// access[3] = tokens[4]; //session id
		/**
		 *ERROR(4)+sep(1) = 5. So from 6th starts the error message!
		 **/
		else if (out.startsWith("ERROR"))
			return out.substring(6);

		// access = new String[2];
		// int i = out.indexOf(delim);
		// String info = out.substring(i+1);
		// info = info.trim();
		// access[0] = "ERROR";
		// access[1] = info;
		// }
		else
			return null;
	}

	public String[] changePassword(String userPassword) {
		String[] access = new String[2];
		String out = processCmd(CHANGEPASSWD, userPassword);

		if (out.startsWith("passwordChanged")) {
			String[] tokens = StringUtils.tokenize(out, DELIM);
			// Util.debug(this, "changedPassword: " + tokens[1] + DELIM +
			// tokens[2]);
			if (!userPassword.equals(tokens[2])) {
				access[0] = "ERROR";
				access[1] = "not able to change user password";
				return access;
			}
			access[0] = tokens[1]; // login name
			access[1] = tokens[2]; // new password
		} else if (out.startsWith("ERROR")) {
			int i = out.indexOf(DELIM);
			String info = out.substring(i + 1);
			info = info.trim();
			access[0] = "ERROR";
			access[1] = info;
		}
		return access;
	}

	public Object executeQuery(String sql, boolean isString) {
		sendCmd(EXECQUERY, sql);

		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public Vector executeQuery(String sql) {
		return (Vector) executeQuery(sql, false);
	}

	public Object executePageQueryCmd(String query, int page, int pageSize,
			boolean isString) {
		processPageQueryCmd(EXECPQUERY, query, page, pageSize);

		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public Vector executePageQueryCmd(String sql, int page, int pageSize) {
		return (Vector) executePageQueryCmd(sql, page, pageSize, false);
	}

	public Object executeQueryFor(String parameters, boolean isString) {
		sendCmd(EXECPREPQUERY, parameters);
		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public Vector executeQueryFor(String parameters) {
		return executeQueryFor(parameters);
	}

	public Object executeUpdate(String sql, boolean isString) {
		sendCmd(EXECUPDATE, sql);
		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public int executeUpdate(String sql) {
		return (new Integer((String) executeUpdate(sql, true))).intValue();
	}

	public Object executeUpdateFor(String parameters, boolean isString) {
		sendCmd(EXECPREPUPDATE, parameters);
		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public int executeUpdateFor(String parameters) {
		return (new Integer((String) executeUpdateFor(parameters, true)))
				.intValue();
	}

	public String update(String aspect, String[] args) {
		String argline = aspect;
		int size = args.length;
		if (size > 0) {
			argline = argline + SEP;

			for (int i = 0; i < size - 1; i++)
				argline = argline + args[i] + SEP;
			argline = argline + args[size - 1];
		}
		String r = processCmd(UPDATE, argline);
		// Util.debug(this, "update:argline=" + argline);
		return processCmd(UPDATE, argline);
	}

	/**
	 * Determines if the principal is allowed access the resource.
	 * 
	 * @param providerName
	 *            The name of the resource
	 * @param serviceType
	 *            The name to the resource access operation (e.g., read, write)
	 * @param principal
	 *            The name of the principal
	 * @return boolean Returns true if access is allowed. Otherwise returns
	 *         false.
	 */
	/*
	 * public boolean isAuthorizedToAccessResource(String resourceName, String
	 * operation, String principalName) { String r = processCmd(AUTHORIZE,
	 * resourceName + sep + operation + sep + principalName); return
	 * r.equals("true"); }
	 */

	public boolean isAuthorized(SorcerPrincipal principal, Class serviceType,
			String providerName) {
		return true;
	}

	public boolean isAuthorized(String principal, Class objectType,
			String objectId, String operation) {
		logger.info("in protocol isAuthorized principal =" + principal
				+ " objectType =" + objectType + "\n" + "objectId=" + objectId
				+ " operation=" + operation);

		if (ADMIN.equals(Launcher.model.getUserRole())
				|| ROOT.equals(Launcher.model.getUserRole()))
			return true;

		String r = processCmd(ACL_ISAUTHORIZED, principal + SEP + objectType
				+ SEP + objectId + SEP + operation);

		logger.info("result r=" + r);
		return r.equals("true");
	}

	public SorcerPrincipal getGAppPrincipal(String id) {
		Vector res;
		try {
			res = executeCmd(GET_GAPP_PRINCIPAL, new String[] { id });
		} catch (IOException ie) {
			return null;
		}
		if (res == null || result.size() < 1)
			return null;
		return SorcerPrincipal.fromString((String) result.elementAt(0));
	}

	public SorcerPrincipal getSSOPrincipal(String id) {
		Vector res;
		try {
			res = executeCmd(GET_SSO_PRINCIPAL, new String[] { id });
		} catch (IOException ie) {
			return null;
		}
		if (res == null || result.size() < 1)
			return null;
		return SorcerPrincipal.fromString((String) result.elementAt(0));
	}

	public void log(String message) {
		processCmd(LOG, message);
	}

	public void logOperation(String operation, String objType, String objName,
			String userName, String contextName, String contextType) {
		StringBuffer description = new StringBuffer();

		description.append('[');
		description.append(new java.util.Date());
		description.append("] ");
		description.append(operation);
		description.append(": ");
		description.append(objType);
		description.append(" (");
		description.append(objName);

		if (contextName != null) {
			description.append(") from: ");
			description.append(contextType);
			description.append(" (");
			description.append(contextName);
		}

		description.append(") by ");
		description.append(userName);

		log(description.toString());
	}

	public void logOperation(String operation, String objType, String objName,
			String userName) {
		logOperation(operation, objType, objName, userName, null, null);
	}

	public Object executeDefault(int command, String[] data, boolean isString)
			throws IOException {
		// Util.debug(this, "executeDefault:" + command + " data:" + data);

		if (data == null)
			sendCmd(EXECCMD, command, null);
		else
			sendCmd(EXECDEFAULT, command, data);
		if (isString)
			return getResultString();
		else
			return getResult();
	}

	public Vector executeDefault(int command, String[] data) throws IOException {
		// Util.debug(this, "executeDefault:" + command);
		return (Vector) executeDefault(command, data, false);
	}

	public Vector executeCmd(int command, String[] data) throws IOException {
		// Util.debug(this, "executeDefault:" + command + " args:" +
		// Util.arrayToString(data));
		return (Vector) executeDefault(command, data, false);
	}

	public Vector executeCmd(int command) throws IOException {
		// Util.debug(this, "executeCmd:" + command);
		return (Vector) executeDefault(command, null, false);
	}

	protected void readData() {
		// for serialized objects do nothing
		if (!isController)
			return;

		String line;
		result.removeAllElements();
		try {
			while (true) {
				line = stream.readLine();
				if (line == null) {
					System.err
							.println("ProxyProtocol>>Application Server closed connection");
					result.removeAllElements();
					return;
				} // if NOT null
				else if (line.equalsIgnoreCase("_DONE_")) {
					// Util.debug(this, "readData:done:" + line);
					break;
				} else {
					result.addElement(new String(line));
					// Util.debug(this, "readData:line:" + line);
				}
			}
		} catch (IOException e) {
			remakeConnection();
		}
	}

	// create a datasource connection if it is a new dsURL
	// No longer pertinent
	/*
	 * public String[] connect(String dsDriver, String dsURL) { String[] access
	 * = new String[2]; if (this.dsURL.equals(dsURL)) { access[0] =
	 * "Current dsURL"; access[1] = dsURL; return access; } this.dsURL = dsURL;
	 * String out = processCmd(DSCONNECT, dsDriver + delim + dsURL);
	 * 
	 * //Util.debug(this, "connect:dsDriver: " + dsDriver + " dsURL: " + dsURL);
	 * 
	 * if (out.startsWith("ERROR")) { int i = out.indexOf(delim); String info =
	 * out.substring(i+1); info = info.trim(); access[0] = "ERROR"; access[1] =
	 * info; } return access; }
	 */

	protected void remakeConnection() {
		// do nothing, implement in subclasses
	}

	abstract public void checkConnection();

	public String getResultString() {
		// System.out.println("ProxyProtocol.getResultString:result=" + result);
		if (!isController) {
			if (outcome instanceof Result) {
				Result out = (Result) outcome;
				if (out.size() == 1)
					return out.firstElement().toString();
			}
			return outcome.toString();
		}

		int s = result.size();
		if (s == 0)
			return null;

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s - 1; i++) {
			sb.append((String) result.elementAt(i));
			sb.append("\n");
		}
		sb.append((String) result.elementAt(s - 1));
		return sb.toString();
	}

	public Object getResult() {
		if (!isController) {
			if (outcome instanceof Result) {
				Result out = (Result) outcome;
				return ((Vector) out).clone();
			}
			return outcome;
		} else
			return result;
	}

	public void isController(boolean state) {
		isController = state;
	}
}
