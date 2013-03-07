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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Hashtable;

import javax.servlet.http.HttpSession;

import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.ServletProtocolStream;

public class ServletProtocolConnection extends ProtocolConnection implements
		Worker {
	public HttpSession session = null;

	public ServletProtocolConnection() {
		// default initialization
		// setConnectionProperties();
		super();
		stream = new ServletProtocolStream();
		// temporary solution
		// userRole = "root";
		// userLogin = "servlet";
	}

	public void run(Object data) {
		Hashtable args = (Hashtable) data;

		try {
			int cmd;

			if (args.containsKey("cmd")) {
				Object obj = args.get("cmd");
				if (obj instanceof Integer)
					cmd = ((Integer) obj).intValue();
				else
					cmd = Integer.parseInt((String) obj);
				// first condition for RMI calls, second for object commands
				if (cmd > SorcerConstants.OBJECT_CMD_START) {
					processObjectCmd(cmd, args);
					return;
				}
				StringReader sr = new StringReader((String) args.get("in"));
				((ServletProtocolStream) stream).in = new BufferedReader(sr);
			} else
				((ServletProtocolStream) stream).in = (BufferedReader) args
						.get("in");

			if (args.containsKey("session"))
				session = (HttpSession) args.get("session");
			System.out.println(getClass().getName() + "::run() session:"
					+ session);

			principal = (SorcerPrincipal) args.get("principal");
			if (principal == null) {
				principal = new SorcerPrincipal();
			}

			((ServletProtocolStream) stream).out = (PrintWriter) ((Hashtable) data)
					.get("pout");
			((ServletProtocolStream) stream).outStream = null;
			processCmd();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processObjectCmd(int cmd, Hashtable data) throws IOException {
		((ServletProtocolStream) stream).outStream = (ObjectOutputStream) ((Hashtable) data)
				.get("oout");
		((ServletProtocolStream) stream).out = null;
		Object[] objs = (Object[]) data.get("objects");

		if (objs[objs.length - 1] instanceof SorcerPrincipal)
			principal = (SorcerPrincipal) objs[objs.length - 1];

		processCmd(cmd, objs);
	}

	public HttpSession getSession() {
		return session;
	}

	/*
	 * protected void login(String arg) throws IOException, SQLException {
	 * Util.debug(this, "login:arg: " + arg); if (acceptUser(arg)) { if
	 * (connectionPool==null) { if (dbConnection!=null) { try {
	 * dbConnection.close(); } catch(SQLException e) { e.printStackTrace(); } }
	 * dbConnection = makeDBConnection(); } //debug(this, "login:userRole:" +
	 * userRole + ",userID=" + userID); System.out.println(getClass().getName()
	 * + "::login(): " + sep + userRole + sep + getPermissions(userRole) + sep +
	 * userID + sep + session.getId()); stream.writeLine("logged" + sep +
	 * userRole + sep + getPermissions(userRole) + sep + userID + sep +
	 * session.getId()); stream.done(); ApplicationDomain.logOperation("login",
	 * userRole, getPermissions(userRole), userLogin); } else {
	 * stream.writeLine("ERROR:" + Const.errors.get("passwordError"));
	 * stream.done();
	 * 
	 * if (userRole==null) ApplicationDomain.logOperation("login failed",
	 * userRole, getPermissions(userRole), userLogin); } }
	 */
	public boolean isServletProtocolConnection() {
		return true;
	}

}
