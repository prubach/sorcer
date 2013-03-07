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

package sorcer.util.dbac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import sorcer.util.Mandate;
import sorcer.util.ServletProtocolStream;
import sorcer.util.Sorcer;
import sorcer.util.ui.Launcher;

public class ServletProtocol extends ProxyProtocol {
	protected String controllerURL, previousUrlStr, dispatcherURL;
	// controllerURL =
	// launcher.model.props.getProperty("launcher.applicationServlet.url");
	protected URLConnection urlConnection;

	public ServletProtocol() {
		stream = new ServletProtocolStream();
		controllerURL = null;
	}

	public ServletProtocol(String url, boolean controllerRequired) {
		stream = new ServletProtocolStream();

		if (controllerRequired)
			controllerURL = url;
		else
			dispatcherURL = url;

		isController = controllerRequired;
	}

	public ServletProtocol(String url) {
		this(url, true);
	}

	public void connect() {
		// do nothing
	}

	public void makeConnection() {
		// create the connection to the servlet
		try {

			if (principal == null) {
				System.out
						.println(getClass().getName()
								+ "::makeConnection() NULL PRINCIPAL getting from Launcher");

				if (Launcher.model != null)
					if (Launcher.model.getPrincipal() == null)
						System.out
								.println(getClass().getName()
										+ "::makeConnection() Launcher.model.principal == null");
					else
						principal = Launcher.model.getPrincipal();
				if (principal != null)
					System.out.println(getClass().getName()
							+ "::makeConnection() principal:"
							+ principal.asString());
			} else if (principal.isAnonymous()) {

				System.out
						.println(getClass().getName()
								+ "::makeConnection() NULLANONYMOUS PRINCIPAL getting from Launcher");
				if (Launcher.model != null)
					if (Launcher.model.getPrincipal() == null)
						System.out
								.println(getClass().getName()
										+ "::makeConnection() Launcher.model.principal == null");
					else
						principal = Launcher.model.getPrincipal();
				if (principal != null)
					System.out.println(getClass().getName()
							+ "::makeConnection() principal:"
							+ principal.asString());
			}

			if (isController)
				urlConnection = new URL(controllerURL + "?cmd=gapp")
						.openConnection();
			else
				urlConnection = new URL(dispatcherURL + "?cmd=gapp")
						.openConnection();

			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			((ServletProtocolStream) stream).out = new PrintWriter(
					urlConnection.getOutputStream());
		} catch (Exception e) {
			System.err.println("Servlet output stream error");
			e.printStackTrace();
			((ServletProtocolStream) stream).out = null;
		}

		if (previousUrlStr != null) {
			// Util.debug(this, "makeConnection:previousUrlStr=" +
			// previousUrlStr);
			controllerURL = previousUrlStr;
			previousUrlStr = null;
		}
	}

	public void sendCmd(int cmd, String inline) {
		checkConnection();
		// handling serialized objects
		if (!isController) {
			executeCmd(cmd, inline);
			return;
		}

		// handling char streams
		try {
			stream.writeInt(cmd);
			if (inline != null)
				stream.writeEscapedLine(inline);
			if (principal != null)
				stream.writeEscapedLine(principal.asString());
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
		// handling serialized objects
		if (!isController) {
			Object[] args = new Object[data.length + 1];
			args[0] = new Integer(subCmd);
			for (int i = 0; i < data.length; i++)
				args[i + 1] = data[i];
			executeCmd(cmd, args);
			return;
		}
		try {
			stream.writeInt(cmd);
			stream.writeInt(subCmd);
			if (data != null) {
				stream.writeInt((principal != null) ? data.length + 1
						: data.length);

				for (int i = 0; i < data.length; i++) {
					stream.writeEscapedLine(data[i]);
				}
				if (principal != null)
					stream.writeLine(principal.asString());
			} else {
				if (principal != null) {
					stream.writeInt(1);
					stream.writeLine(Launcher.model.getPrincipal().asString());
				} else
					stream.writeInt(0);
			}
			stream.flush();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			throw e;
		}
		readData();
	}

	protected void readData() {
		try {
			((ServletProtocolStream) stream).in = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()));
			super.readData();
			disconnect();
		} catch (Exception e) {
			System.err.println("Servlet input stream error");
			e.printStackTrace();
			((ServletProtocolStream) stream).in = null;
		}
	}

	public void checkConnection() {
		makeConnection();
	}

	public boolean connected() {
		return true;
	}

	public Vector executeQuery(String sql) {
		return super.executeQuery(sql);
	}

	public Vector executeQuery(String sql, String qyeryString) {
		if (qyeryString == null)
			return super.executeQuery(sql);
		else {
			previousUrlStr = controllerURL;
			controllerURL = controllerURL + "&" + qyeryString;
			sendCmd(EXECQUERY, sql);
			return (Vector) getResult();
		}
	}

	public Vector executeCmd(int command, String[] data) throws IOException {
		return (Vector) super.executeDefault(command, data, false);
	}

	public Vector executeCmd(int command, String[] data, String qyeryString)
			throws IOException {

		if (qyeryString == null)
			return super.executeCmd(command, data);
		else {
			previousUrlStr = controllerURL;
			controllerURL = controllerURL + "&" + qyeryString;

			return (Vector) executeDefault(command, data, false);
		}
	}

	public Object doTask(Serializable object) {
		return executeCmd(DO_TASK, object);
	}

	public Object doJob(Serializable object) {
		return executeCmd(DO_JOB, object);
	}

	public Object executeMandate(Mandate mandate) {
		if (Launcher.model != null && Launcher.model.getPrincipal() != null)
			mandate.setPrincipal(Launcher.model.getPrincipal());
		return executeCmd(EXEC_MANDATE, mandate);
	}

	public Object executeCmd(int command, Serializable object) {
		Serializable data[] = { object };
		return executeCmd(command, data);
	}

	// Server side storing of Objects.
	public Object store(Serializable object) {
		Serializable data[] = { object };
		return executeCmd(STORE_OBJECT, data);
	}

	public Object restore(String id) {
		Serializable[] data = { id };
		return executeCmd(RESTORE_OBJECT, data);
	}

	public Object executeCmd(int command, Serializable[] data) {
		try {
			URLConnection con;
			Serializable[] newData = new Serializable[(principal != null) ? data.length + 1
					: data.length];

			for (int i = 0; i < data.length; i++)
				newData[i] = data[i];
			if (principal != null)
				newData[data.length] = principal;

			if (command == DO_JOB || command == DO_TASK
					|| command == EXEC_MANDATE || !isController) {
				// Util.debug(this, "dispatcherURL=" + dispatcherURL);
				con = new URL(dispatcherURL + "?cmd=" + command)
						.openConnection();
			} else {
				// Util.debug(this, "controllerURL=" + controllerURL);
				con = new URL(controllerURL + "?cmd=" + command)
						.openConnection();
			}
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			// Write the arguments as post data
			ObjectOutputStream out = new ObjectOutputStream(con
					.getOutputStream());
			out.writeObject(newData);
			out.flush();
			out.close();

			ObjectInputStream ois = new ObjectInputStream(con.getInputStream());
			outcome = ois.readObject();
			return outcome;
		} catch (Exception e) {
			System.err.println("Servlet stream communication error");
			e.printStackTrace();
			return e;
		}
	}

	public void setDispatcherURL(String url) {
		dispatcherURL = url;
	}
}
