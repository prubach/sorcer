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
package sorcer.util.ui;

import static sorcer.core.SorcerConstants.*;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.Crypt;
import sorcer.util.DataReader;

import java.awt.*;
import java.util.Observer;
import java.util.Properties;

public class LauncherModel extends Model {
	public static boolean isDebugged = false;
	private String appDefaultsFile;
	protected SorcerPrincipal principal = new SorcerPrincipal();
	// roles used by MembershipModel if set by application
	public String[] roles = { ADMIN, LOOKER, PUBLISHER, ORIGINATOR, APPROVER,
			ALL };
	// "all,admin,updater,logger,looker";
	public boolean noPassword = false, noDBC = true;
	public boolean useSSO;
	public int httpPort, asPort, coPort, launcherWidth, launcherHeight;
	public String httpHost, asHost, coHost;
	public String appName, appProperties, prepareContext = "",
			bgColor = "C0C0C0";
	public Properties props = null; // defined in getProperties() in subclasses
	public boolean isInBrowser = true, isMediated = false;
	protected String target;
	// view is name of a view imbeded by applications into an applet by
	// AppletView
	// when the applet should be displayed in a frame
	static public String asSeparator;

	public LauncherModel(Observer parent) {
		addObserver(parent);
	}

	public void initialize() {
		String str = props.getProperty("launcher.noPassword", "false");
		noPassword = str.equals("true");
		str = props.getProperty("launcher.useSSO", "false");
		useSSO = "true".equals(str);
		str = props.getProperty("launcher.isDebugged", "false");
		Launcher.isDebugged = str.equals("true");
		str = props.getProperty("launcher.noDBC", "true");
		noDBC = str.equals("true");

		appName = props.getProperty("launcher.appName", "unknown");
		asSeparator = props.getProperty("launcher.asSeparator", "/");
		target = props.getProperty("launcher.target", "Docs");
		launcherWidth = Integer.parseInt(props.getProperty("launcher.width",
				"150"));
		launcherHeight = Integer.parseInt(props.getProperty("launcher.height",
				"455"));

		httpPort = Integer.parseInt(props.getProperty("http.port", "80")); // TCS
		httpHost = props.getProperty("http.host");

		DataReader.host = httpHost;
		DataReader.port = httpPort;

		asPort = Integer.parseInt(props.getProperty("applicationServer.port",
				"6002"));
		asHost = props.getProperty("applicationServer.host");

		coPort = Integer.parseInt(props.getProperty("co.port", "6003"));
		coHost = props.getProperty("co.host");

	}

	public boolean isAuthorized(String operation) {
		return true;
	}

	public String getAppDefaultsFile() {
		if (appDefaultsFile == null)
			return appName.toLowerCase() + "/lnch/" + appName.toLowerCase()
					+ ".def";
		else
			return appDefaultsFile;
	}

	public void setAppDefaultsFile(String path) {
		appDefaultsFile = path;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget() {
		return target;
	}

	public boolean isUserVerified() {
		return principal.isAuth();
	}

	public SorcerPrincipal getPrincipal() {
		return principal;
	}

	public void setPrincipal(SorcerPrincipal principal) {
		this.principal = principal;
	}

	public void isUserVerified(boolean val, Object requestor) {
		// customize in subclases to controll access to isUserVerified
		// only by authorized requestors
		principal.isAuth(val);
		if (!val) {
			principal.setName(null);
			principal.clearPassword();
		}
	}

	public Dimension getDefaultSize(String propName) {
		String sizeStr = props.getProperty(propName);
		int i = sizeStr.indexOf(',');
		int w = Integer.parseInt(sizeStr.substring(0, i));
		int h = Integer.parseInt(sizeStr.substring(i + 1));
		return new Dimension(w, h);
	}

	public String getPropertyValue(String propName, String defaulValue) {
		return props.getProperty(propName, defaulValue);
	}

	public String getPropertyValue(String propName) {
		return props.getProperty(propName);
	}

	public boolean isOn(String propName) {
		String str = props.getProperty(propName);
		if (str == null)
			return false;
		else
			return str.equals("true");
	}

	// This method contains special method which returns authentication string
	// needed by Application servlet to
	// authenticate.
	public String getSecureFileAccessRequestURL(String secureFile) {
		String userCredentials = principal.getId()
				+ new String(principal.getPassword())
				+ principal.getSessionID();

		return new StringBuffer(props.get("launcher.applicationServlet.url")
				.toString()).append("?file=").append(secureFile).append(
				"&userID=").append(principal.getId()).append("&nounce=")
				.append(principal.getSessionID()).append("&hash=").append(
						new Crypt().crypt(userCredentials, SEED)).toString();
		// java.net.
		// URLEncoder.
		// encode(Auth.getEncriptedPassword1(userCredentials.toCharArray())))
		// .toString();
	}

	public String getUserName() {
		return principal.getName();
	}

	public char[] getUserPassword() {
		return principal.getPassword();
	}

	public String getUserID() {
		return principal.getId();
	}

	public String getUserRole() {
		return principal.getRole();
	}

	public int getUserRoles() {
		return principal.getRoles();
	}

	public String getUserPermissions() {
		return principal.getPermissions();
	}

	public void setUserPermissions(String permissions) {
		principal.setPermissions(permissions);
	}

	public String getAsocValue(String association) {
		int i = association.indexOf('=');
		if (i > 0)
			return association.substring(i + 1);
		else
			return "";
	}
}
