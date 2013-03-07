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

package sorcer.util.ui;

import java.util.Observer;
import java.util.Vector;

import sorcer.util.Crypt;
import sorcer.util.Mandate;
import sorcer.util.dbac.SocketProtocol;

public class ProtocolLauncherModel extends LauncherModel {
	// used for file upload and other utils related to documents handling
	// set in application dependent models
	public String selectedOID, selectedUser, userDescriptor,
			selectedDescriptor,
			// handling DirBrowser
			selectedDirPath, selectedDir, selectedDocument, selectedStatus,
			// user access class (1-public) and export control (1-applies)flag
			userAC = "1", userEC = "1";
	private String[] accessClasses;
	// default forest path separator
	// applications define it as a property launcher.sepChar
	public static char pathSep = '.';

	public ProtocolLauncherModel(Observer parent) {
		super(parent);
	}

	public void initialize() {
		pathSep = getPropertyValue("launcher.sepChar", ".").charAt(0);
		// Util.debug(this, "initialize:pathSep=" + pathSep);
		super.initialize();
		initRoles();
		initAccessClasses();

		SocketProtocol.readerTimeout = Integer.parseInt(props.getProperty(
				"launcher.readerTimeout", "60000"));

		// Util.debug(this, "readerTimeout:" + SocketProtocol.readerTimeout);
	}

	protected void initRoles() {
		String[] items = new String[7];

		items[0] = LOOKER;
		items[1] = PUBLISHER;
		items[3] = ORIGINATOR;
		items[2] = REVIEWER;
		items[4] = APPROVER;
		items[5] = ADMIN;
		items[6] = ALL;

		roles = items;
	}

	// GE proprietary classes
	protected void initAccessClasses() {
		accessClasses = new String[4];
		accessClasses[0] = "1 - public"; // public
		accessClasses[1] = "2 - sensitive"; // sensitive
		accessClasses[2] = "3 - confidential"; // confidential
		accessClasses[3] = "4 - secret"; // secret
	}

	/**
	 * Checks is an application appName can share the resources of Launcher
	 * class: protocol, model, and controller.
	 */
	public boolean isFriendly(Object guest) {
		// reimplement in sublases
		return false;
	}

	/**
	 * Returns an SQL query used by database apps
	 */
	public String getQuery(String qName) {
		return "should be implemented by subclasses";
	}

	/**
	 * Returns true if an error is returned while receiving data from
	 * ApplicationServer
	 */
	public static boolean isError(Vector results) {
		return isError(results, null);
	}

	/**
	 * Returns true if an error is returned while receiving data from
	 * ApplicationServer and with a provided error message when results are
	 * empty
	 */
	public static boolean isError(Vector results, String message) {
		if (results.size() == 1) {
			String str = (String) results.elementAt(0);
			if (str.startsWith("ERROR")) {
				System.err.println(str.substring(6));
				return true;
			} else if (str.equals("null")) {
				System.err.println(message == null ? "No data available"
						: message);
				return true;
			}
		} else if (message != null && results.size() == 0) {
			System.err.println(message);
			return true;
		}
		return false;
	}

	public Mandate getMandate(int command) {
		return new Mandate(command, getPrincipal());
	}

	public String[] accessClasses() {
		return accessClasses;
	}

	public String getUserDescriptor() {
		// should be customized in subclasses
		return userDescriptor;
	}

	public String getProtocolType() {
		return props.getProperty("launcher.server.type", "socket");
	}

	public String getProtocolServletURL() {
		return props.getProperty("applicationServlet.url");
	}

	public String getHTTPProtocolURL() {
		return props.getProperty("launcher.http.server.url");
	}

	public static char getPathSep() {
		return pathSep;
	}

	// public void setPassword1(String password) {
	// byte[] salt = Auth.getSalt();
	// principal.setPassword(Auth.encriptPassword(password.toCharArray(),
	// salt));
	// }

	// public boolean isPasswordSame1(String password) {
	// return Auth.isPasswordSame(String.copyValueOf(principal.getPassword()),
	// password);
	// }

	public boolean isPasswordSame(String password) {
		return (new Crypt().crypt(password, SEED)).equals(String
				.copyValueOf(principal.getPassword()));
	}

	public void setPassword(String password) {
		principal
				.setPassword((new Crypt()).crypt(password, SEED).toCharArray());
	}
}
