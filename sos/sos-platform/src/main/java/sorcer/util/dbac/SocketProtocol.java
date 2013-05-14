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
import sorcer.util.DataProtocolStream;
import sorcer.util.Protocol;
import sorcer.util.StringUtils;

import java.io.*;
import java.net.Socket;

/**
 * The SocketProtocol class implements interface Protocol to ApplicationServer.
 * GApp-based applications create one instance of the SocketProtocol class and
 * use it to pass on commands to an ApplicationServer. SocketProtocol provides
 * the basic database connectivity for GApp-based applications, including
 * connection management, SQL queries management, authentication, authorization,
 * ACLs, and logging.
 * <p>
 */
public class SocketProtocol extends ProxyProtocol implements Protocol,
		SorcerConstants {
	public Socket serverSocket;
	String serverName;
	int serverPort;
	RemakeConnection rc;
	private boolean noGUI = false;
	public static int readerTimeout = 16000;
	private int BUFSIZE = 1024;

	public SocketProtocol(String hostPort) {
		String items[] = StringUtils.tokenize(hostPort, ":");
		serverName = items[0];
		serverPort = Integer.parseInt(items[1]);
	}

	public SocketProtocol(String asHost, int asPort) {
		this(asHost, asPort, false);
	}

	public SocketProtocol(String asHost, int asPort, boolean noGUI) {
		this.noGUI = noGUI;
		serverName = asHost;
		serverPort = asPort;
	}

	/**
	 * Make connection to an ApplicationServer, in the case of failure try to
	 * reconnect
	 */
	public void connect() {
		stream = new DataProtocolStream();
		try {
			makeConnection();
		} catch (IOException e) {
			// Now create a thread to continuosly try to reconnect
			// with the errant ApplicationServer
			rc = new RemakeConnection(serverName, serverPort, true);
		}
	}

	/**
	 * Disconnects from an ApplicationServer server
	 */
	public void disconnect() {
		if (!connected()) {
			return;
		}
		// close socket
		try {
			if (((DataProtocolStream) stream).in != null)
				stream.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// allow gc
		stream = null;
		serverSocket = null;
	}

	/**
	 * @Return true, if the SocketProtocol is connected to an ApplicationServer
	 *         server
	 */
	public boolean connected() {
		return serverSocket != null;
	}

	// Create a connection with the ApplicationServer host
	public void makeConnection() throws IOException {
		// Attempt to connect to a port
		try {
			// Open the connection to the server
			serverSocket = new Socket(serverName, serverPort);
		} catch (IOException e) {
			System.err
					.println("SocketProtocol>>failed to create a server socket for host: "
							+ serverName + ", port: " + serverPort);
			stream = null;
			serverSocket = null;
			throw e;
		}
		// Attempt to open a client OutputStream
		((DataProtocolStream) stream).out = new DataOutputStream(
				new BufferedOutputStream(serverSocket.getOutputStream(),
						BUFSIZE));
		((DataProtocolStream) stream).in = new DataInputStream(
				new BufferedInputStream(serverSocket.getInputStream(), BUFSIZE));
	}

	public void checkConnection() {
		if (((DataProtocolStream) stream).in == null) {
			// If so, then get the new connection and keep going
			if (rc != null && rc.serverState) {
				serverSocket = rc.serverSocket;
				((DataProtocolStream) stream).in = rc.inStream;
				((DataProtocolStream) stream).out = rc.outStream;
				rc = null;
			}
		}
	}
}
