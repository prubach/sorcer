/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 * Copyright 2013 the original author or authors.
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
package sorcer.ui.serviceui;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ui.factory.JFrameFactory;

import javax.swing.*;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * The UIFrameFactory class is a helper for use with the ServiceUI
 */
public class UIFrameFactory implements JFrameFactory, Serializable {

	static final long serialVersionUID = 5806535989492809459L;

	private final static Logger logger = Logger.getLogger(UIFrameFactory.class.getName());

	private String className;
	private URL[] exportURL;
	//private String name;
	private URL helpURL;
	private String accessibleName = "Main Window";

	public String getAccessibleName() {
		return accessibleName;
	}

	public UIFrameFactory(URL exportUrl, String className, String name, URL helpUrl) {
		this.className = className;
		this.exportURL = new URL[] { exportUrl };
		this.accessibleName = name;
		this.helpURL = helpUrl;
	}

	public UIFrameFactory(URL exportUrl, String className) {
		this(exportUrl, className, null, null);
	}

	public UIFrameFactory(URL[] exportURL, String className, String name, URL helpUrl) {
		this.className = className;
		this.exportURL = exportURL;
		this.accessibleName = name;
		this.helpURL = helpUrl;
	}

	public UIFrameFactory(URL[] exportURL, String className) {
		this(exportURL, className, null, null);
	}

	public JFrame getJFrame(Object roleObject) {
		if (!(roleObject instanceof ServiceItem)) {
			throw new IllegalArgumentException("ServiceItem required");
		}
		ClassLoader cl = ((ServiceItem) roleObject).service.getClass()
				.getClassLoader();
		JFrame component = null;
		final URLClassLoader uiLoader = URLClassLoader.newInstance(exportURL,
				cl);
		final Thread currentThread = Thread.currentThread();

		final ClassLoader parentLoader = (ClassLoader) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return (currentThread.getContextClassLoader());
					}
				});

		try {
			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					currentThread.setContextClassLoader(uiLoader);
					return (null);
				}
			});

			try {
				Class clazz = uiLoader.loadClass(className);
				Constructor constructor = clazz
						.getConstructor(Object.class);
				Object instanceObj = constructor
						.newInstance(roleObject);
				component = (JFrame) instanceObj;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Unable to instantiate ServiceUI " + className + ": "
								+ e.getClass().getName(), e);
			}
		} finally {
			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					currentThread.setContextClassLoader(parentLoader);
					return (null);
				}
			});
		}
		return (component);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.jini.lookup.ui.factory.JComponentFactory#getJComponent(java.lang.
	 * Object)
	 */
	public JComponent getJComponent(Object arg) {
		if (helpURL == null)
			return new JLabel("No help page available");

		try {
			logger.info("help url: " + helpURL);
			JEditorPane htmlView = new JEditorPane(helpURL);
			htmlView.setEditable(false);
			// set the AccessibleContext Name for this view
			// so the SORCER browser will display it
			JScrollPane sp = new JScrollPane(htmlView);
			sp.getAccessibleContext().setAccessibleName(accessibleName);
			return sp;
		} catch (Exception ex) {
			//return new JLabel(ex.toString() + " = " + helpFilename);
			JLabel lb = new JLabel("");
			lb.getAccessibleContext().setAccessibleName(accessibleName);
			return lb;
		}
	}

	public String toString() {
		return "UIFrameFactory for className: " + className + ", exportURL: "
				+ Arrays.toString(exportURL);
	}
}
