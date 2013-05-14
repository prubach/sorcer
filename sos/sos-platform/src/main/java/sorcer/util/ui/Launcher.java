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

import sorcer.util.*;

import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public abstract class Launcher extends Applet implements Observer, Invoker,
		Runnable {
	private static Logger logger = Logger.getLogger(Launcher.class.getName());
	public static boolean isDebugged = false;
	public static Launcher launcher;
	public static LauncherModel model;
	public static CmdManager controller;
	public static Protocol protocol = null;
	// private final TipPresenter tip = TipPresenter.getTipPresenter();

	// A reference of view on right side of launcher
	// View.cleanup() called when needed.
	protected Component currentView = null;

	// use for data exchange, allow gc
	public static Object buffer = null;

	public static Properties loadConfiguration(Properties props, String filename)
			throws IOException {
		System.out.println("load:filename=" + filename);
		InputStream is = Launcher.class.getResourceAsStream(filename);
		if (is == null)
			is = new FileInputStream(new File(filename));
		if (is != null) {
			props = (props == null) ? new Properties() : props;
			props.load(is);
		} else {
			logger.info("Not able to open stream on properties " + filename);
		}
		return props;
	}

	// public void addTip(Component c, String msg) {
	// if (msg!=null)
	// tip.put(c,msg);
	// }

	// public void removeTip(Component c) {
	// tip.remove(c);
	// }

	protected boolean isAuthorized() {
		return false;
	}

	public void update(Observable o, Object arg) {
		if (arg instanceof Hashtable) {
			Hashtable args = (Hashtable) arg;
			Object aspect = args.get("aspect");

			if (aspect instanceof Button) {
				if (args.size() == 2)
					acceptButton(((Button) aspect).getLabel(), args.get("arg1"));
				else if (args.size() == 3)
					acceptButton(((Button) aspect).getLabel(),
							args.get("arg1"), args.get("arg2"));
				else if (args.size() == 4)
					acceptButton(((Button) aspect).getLabel(),
							args.get("arg1"), args.get("arg2"), args
									.get("arg3"));
			} else if (aspect instanceof Choice) {
				if (args.size() == 2)
					acceptChoice(((Choice) aspect).getSelectedItem(), args
							.get("arg1"));
				else if (args.size() == 3)
					acceptChoice(((Choice) aspect).getSelectedItem(), args
							.get("arg1"), args.get("arg2"));
				else if (args.size() == 4)
					acceptChoice(((Choice) aspect).getSelectedItem(), args
							.get("arg1"), args.get("arg2"), args.get("arg3"));
			} else if (args.size() == 2)
				update(o, aspect, args.get("arg1"));
			else if (args.size() == 3)
				update(o, aspect, args.get("arg1"), args.get("arg2"));
			else if (args.size() == 4)
				update(o, aspect, args.get("arg1"), args.get("arg2"), args
						.get("arg3"));
		}
	}

	public void update(String aspect, String[] args) {
		// implemented by subclasses
		logger.info("Launcher>>update:aspect: " + aspect + " args: "
				+ StringUtils.arrayToString(args));
	}

	public void update(Observable o, Object aspect, Object arg1) {
		// implemented by subclasses
		logger.info("Launcher>>update:aspect: " + aspect + " arg1: " + arg1);
	}

	public void update(Observable o, Object aspect, Object arg1, Object arg2) {
		// implemented by subclasses
		logger.info("Launcher>>update:aspect: " + aspect + " arg1: " + arg1
				+ " arg2: " + arg2);
	}

	public void update(Observable o, Object aspect, Object arg1, Object arg2,
			Object arg3) {
		// implemented by subclasses
		logger.info("Launcher>>update:aspect: " + aspect + " arg1: " + arg1
				+ " arg2: " + arg2 + " arg3: " + arg3);
	}

	public String getUserName() {
		return model().getUserName();
	}

	public void acceptButton(String buttonLabel, Object arg1) {
		// implemented in sublcalsses
		logger.info("acceptButton:buttonLabel: " + buttonLabel + " arg1: "
				+ arg1);
	}

	public void acceptButton(String buttonLabel, Object arg1, Object arg2) {
		// implemented in sublcalsses
		logger.info("acceptButton:buttonLabel: " + buttonLabel + " arg1: "
				+ arg1 + " arg2: " + arg2);
	}

	public void acceptButton(String buttonLabel, Object arg1, Object arg2,
			Object arg3) {
		// implemented in sublcalsses
		logger.info("acceptButton:buttonLabel: " + buttonLabel + " arg1: "
				+ arg1 + " arg2: " + arg2 + " arg3: " + arg3);
	}

	public void acceptChoice(String choice, Object arg1) {
		// implemented in sublcalsses
		logger.info("acceptChoice:choice: " + choice + " arg1: " + arg1);
	}

	public void acceptChoice(String choice, Object arg1, Object arg2) {
		// implemented in sublcalsses
		logger.info("acceptChoice:choice: " + choice + " arg1: " + arg1
				+ " arg2: " + arg2);
	}

	public void acceptChoice(String choice, Object arg1, Object arg2,
			Object arg3) {
		// implemented in sublcalsses
		logger.info("acceptChoice:choice: " + choice + " arg1: " + arg1
				+ " arg2: " + arg2 + " arg3: " + arg3);
	}

	/**
	 * Allows the use of path info and query string in getting a document via an
	 * URL and displayed in a target frame.
	 */
	public void getDoc(String docPropName, String pathInfo, String queryString,
			String target, boolean isAppended) {
		try {
			String urlStr;
			if (isAppended)
				urlStr = model().props.getProperty(docPropName) + pathInfo
						+ "&" + queryString;
			else
				urlStr = model().props.getProperty(docPropName) + pathInfo
						+ "?" + queryString;

			// debug(this, "getDoc:docURL: " + urlStr);
			getAppletContext().showDocument(new URL(getDocumentBase(), urlStr),
					model().isInBrowser ? target : "_blank");
			showMessage("Document requested at " + urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void getDoc(String docPropName, String pathInfo, String queryString,
			String target) {
		getDoc(docPropName, pathInfo, queryString, target, false);
	}

	public void getDoc(String docPropName, String queryString, String target) {
		getDoc(docPropName, "", queryString, target, false);
	}

	public void getDoc(String docPropName, String target) {
		String urlStr = model().props.getProperty(docPropName);
		// debug(this, "getDoc:urlStr: " + urlStr + " for: " + docPropName);
		getDocBaseDoc(urlStr, model().isInBrowser ? target : "_blank");
	}

	public void getCodeBaseDoc(String urlStr, String target) {
		// debug(this, "code base: " + getCodeBase() + " urlStr: " + urlStr );
		try {
			getAppletContext().showDocument(new URL(getCodeBase(), urlStr),
					model().isInBrowser ? target : "_blank");
			showMessage("Document requested at " + urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void getDocBaseDoc(String urlStr, String target) {
		// debug(this, "document base: " + getDocumentBase() + " urlStr: " +
		// urlStr );
		try {
			getAppletContext().showDocument(new URL(getDocumentBase(), urlStr),
					model().isInBrowser ? target : "_blank");
			showMessage("Document requested at " + urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void getDocBaseDoc(URL urlStr, String target) {
		// debug(this, "document base: " + getDocumentBase() + " urlStr: " +
		// urlStr );
		getAppletContext().showDocument(urlStr,
				model().isInBrowser ? target : "_blank");
		showMessage("Document requested at " + urlStr.getFile());
	}

	public void getCBDocFile(String filename, String target) {
		String fn = StringUtils.urlEncode(filename);
		try {
			getAppletContext().showDocument(new URL(getCodeBase(), fn),
					model().isInBrowser ? target : "_blank");
			showMessage("Document requested: " + filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void getDBDocFile(String filename, String target) {
		String fn = StringUtils.urlEncode(filename);
		try {
			getAppletContext().showDocument(new URL(getDocumentBase(), fn),
					model().isInBrowser ? target : "_blank");
			showMessage("Document requested: " + filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void showMessage(String str) {
		// debug(this, "showMessage: " + str);
		if (str != null)
			showStatus(str);
	}

	public void getAppDefaults() {
		model().props = new Properties();
		try {
			String defFileName = model().getAppDefaultsFile();
			loadConfiguration(model().props, defFileName);
			// model().props.load(new URL(getCodeBase(),
			// defFileName).openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (isDebugged) {
			Enumeration enu = model().props.propertyNames();
			while (enu.hasMoreElements()) {
				String propKey = (String) enu.nextElement();
				System.out.println(propKey + ": "
						+ model().props.getProperty(propKey));
			}
		}
	}

	// used with CmdManager
	public boolean executeSelect(String action) {
		// should be implemnted is subclasses when CmdFactory is not used
		// do nothing here
		return true;
	}

	// used with CmdManager
	public boolean executeAction(String cmd) {
		// should be implemnted is subclasses when CmdFactory is not used
		// do nothing here
		return true;
	}

	// used with CmdManager
	public boolean executeMandate(Mandate mandate) {
		// should be implemnted is subclasses when CmdFactory is not used
		// do nothing here
		return true;
	}

	public void getUserParameters() {
		String in;
		in = getParameter("appName");
		model().appName = (in == null) ? "Launcher" : new String(in);
		// debug(this, "get.UserParameters.appName: " + model.appName);
	}

	public boolean prepare() {
		// implemented by subclasses to provide class data for applets
		// tagged in HTML document and called in applet's init method
		// use model.prepareContext for relevant preparation, see
		// gapp/docs/DWorksheet
		return false;
	}

	public Launcher launcher() {
		return Launcher.launcher;
	}

	public static Launcher any() {
		if (launcher != null)
			return launcher;
		else
			return (Launcher) buffer;
	}

	public static boolean useSSO() {
		return model.useSSO;
	}

	public LauncherModel model() {
		return Launcher.model;
	}

	public CmdManager controller() {
		return Launcher.controller;
	}

	public Protocol protocol() {
		return protocol;
	}

	public String createID(Object obj) {
		String identifier = Long.toHexString(new Date().getTime());
		return new ID(
				Launcher.model.getPropertyValue("applicationServer.host"),
				Integer.parseInt(Launcher.model
						.getPropertyValue("applicationServer.port")), obj
						.hashCode()
						+ "/" + identifier).toString();
	}

	public void setCurrentComponent(Component component) {
		currentView = component;
	}
}
