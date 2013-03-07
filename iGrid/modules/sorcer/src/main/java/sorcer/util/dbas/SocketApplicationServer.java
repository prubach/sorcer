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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;

import sorcer.core.SorcerConstants;

//import javax.net.*;
//import javax.net.ssl.*;

public class SocketApplicationServer extends ApplicationDomain implements
		MonitoredProcess, Runnable {
	// the server thread waiting for incoming requests
	protected Thread serverThread = null;
	protected ServerSocket serverSocket;
	protected ThreadGroup currentConnections;
	protected Vector connections;
	protected ConnectionWatcher watcher;
	public ProcessMonitor monitor;
	public static final int DEFAULT_PORT = 6001;

	// password for administration interface
	protected String passwd;

	// Exit with an error message, when an exception occurs.
	public static void fail(Exception e, String msg) {
		System.err.println(msg + ": " + e);
		System.exit(1);
	}

	// initialze a ServerSocket to listen for connections on; startthe thread.
	public void initialize(int port, String password) {
		// Create our server thread with a name.
		passwd = password;
		createSocket(port);

		// Create a threadgroup for our connections
		currentConnections = new ThreadGroup(appName + " Connections");

		// Initialize a vector to store our connections in
		connections = new Vector();
		// Create a ConnectionWatcher thread to wait for other threads to die.
		// It starts itself automatically.
		watcher = new ConnectionWatcher(this);

		// Create access control manager
		createAclManager();

		// Start the server listening for connections
		serverThread = new Thread(this);
		serverThread.start();

		Properties sysProps = System.getProperties();
		logOperation("started", getClass().getName(), appName, sysProps
				.getProperty("user.name")
				+ ", java version: " + sysProps.getProperty("java.version"));

		ProtocolConnection.connectionPool = JdbcConnectionPool.getInstance();
	}

	protected void createSocket(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			fail(e, "Exception creating server socket");
		}
	}

	public void stopProcess() {
		Properties sysProps = System.getProperties();
		logOperation("killed", getClass().getName(), appName, sysProps
				.getProperty("user.name")
				+ ", java version: " + sysProps.getProperty("java.version"));

		try {
			serverThread.stop();
			serverSocket.close();
		} catch (IOException ex) {
			System.err
					.println("SocketApplicationServer: appPrefix filed to stop");
			ex.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * The body of the server thread. Loop forever, listening for and accepting
	 * connections from clients. For each connection, create a Connection object
	 * to handle communication through the new Socket. When we create a new
	 * connection, add it to the Vector of connections, and display it in the
	 * List. Note that we use synchronized to lock the Vector of connections.
	 * The ConnectionWatcher class does the same, so the watcher won't be
	 * removing dead connections while we're adding fresh ones.
	 */
	public void run() {
		try {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				SocketProtocolConnection c = new SocketProtocolConnection(
						clientSocket, currentConnections,
						Thread.NORM_PRIORITY - 1, watcher);
				// prevent simultaneous access.
				synchronized (connections) {
					connections.addElement(c);
				}
			}
		} catch (IOException e) {
			fail(e, "Exception while listening for connections");
		}
		System.exit(0);
	}

	public static void usage() {
		System.err
				.println("usage: java jgapp.dbas.SocketApplicationServer [-port <num>] "
						+ "[-debug] [-dir <server properties dir>] [-admin <passwd>]");
		System.err.println("defaults: \n\t port = " + DEFAULT_PORT);
		System.err.println("\t debug          = OFF");
		System.err.println("\t properties dir = NONE");
		System.err.println("\t admin          = NONE");
		System.exit(-1);
	}

	// Start the server up, listening on an optionally specified port
	public static void main(String[] argv) {
		String password = SorcerConstants.ADMIN;
		int port = DEFAULT_PORT;

		// process command line
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].equals("help")) {
				usage();
			}
			if (argv[i].equals("-port")) {
				try {
					port = Integer.parseInt(argv[i + 1], 10);
					i++;
				} catch (Exception e) {
					usage();
				}
			}
			if (argv[i].equals("-dir")) {
				try {
					asDir = argv[i + 1];
					i++;
				} catch (Exception e) {
					usage();
				}
			}
			if (argv[i].equals("-admin")) {
				try {
					password = argv[i + 1];
					i++;
				} catch (Exception e) {
					usage();
				}
			}
		}

		// loadProperties();

		try {
			String str = props.getProperty("applicationServer.port", Integer
					.toString(DEFAULT_PORT));
			if (str != null)
				port = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.out.println("Exception parsing server port " + e);
		}
		;

		SocketApplicationServer.initQueryTable();
		SocketApplicationServer.createServer(port, password);
	}

	public static void createServer(int port, String password) {
		// Determine whether to use extended SocketApplicationServer - a
		// subclass
		String str = props.getProperty("applicationServer.isExtended", "false");
		String cn = appPrefix + "SocketApplicationServer";
		SocketApplicationServer as;
		if (str.equalsIgnoreCase("true")) {
			try {
				as = (SocketApplicationServer) Class.forName(
						appPrefix + ".dbas." + cn).newInstance();
			} catch (Exception e) {
				System.err.println("Failed to create : "
						+ SocketApplicationServer.appPrefix
						+ "SocketApplicationServer");
				e.printStackTrace();
				return;
			}
		} else
			as = new SocketApplicationServer();
		as.initialize(port, password);

		if (as.isNotifierEnabled())
			as.createNotifier();
		;
	}
}
