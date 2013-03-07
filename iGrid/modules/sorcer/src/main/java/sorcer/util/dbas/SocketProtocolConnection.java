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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import sorcer.util.DataProtocolStream;

/**
 * SocketProtocolConnection is the thread that handles all communication with a
 * client It also notifies the ConnectionWatcher when the connection is dropped.
 */
public class SocketProtocolConnection extends ProtocolConnection {
	// allow loginTrials wrong logins and suspend a user
	int loginFails = 0, loginTrials = 3;
	static int numberOfConnections = 0;
	protected Socket clientSocket;
	protected ConnectionWatcher watcher;
	private int BUFSIZE = 1024;

	// Initialize the streams and start the thread
	public SocketProtocolConnection(Socket clientSocket,
			ThreadGroup currentConnections, int priority,
			ConnectionWatcher watcher) {
		// Give the thread a group, a name, and a priority.
		super(currentConnections, "Connection number" + numberOfConnections++);
		setPriority(priority);
		// Save our other arguments away
		this.clientSocket = clientSocket;
		this.watcher = watcher;
		// setConnectionProperties();

		// Create the streams
		try {
			stream = new DataProtocolStream();
			((DataProtocolStream) stream).in = new DataInputStream(
					new BufferedInputStream(clientSocket.getInputStream(),
							BUFSIZE));
			((DataProtocolStream) stream).out = new DataOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream(),
							BUFSIZE));
		} catch (IOException e) {
			System.err
					.println("Exception while getting client socket streams: "
							+ e);
			stream = null;
			return;
		}
		// And start the thread up
		start();
	}

	/**
	 * Provide the service for all Protocol commands
	 */
	public void run() {
		// create the system jdbc connection to support system interactions
		// if (connectionPool==null) {
		// dbLogin = getDBLogin(Const.ROOT);
		// dbPasswd = getDBPassword(Const.ROOT);
		// systemDBConnection = makeDBConnection();
		// Util.debug(this, "systemDBConnection created");
		// }

		int cmd;
		try {
			// Loop forever, or until the connection is broken!
			while (true) {
				processCmd();
				stream.flush();
				// Yield to the other threads
				yield();
			} // end while
		} catch (IOException e) {
			System.err
					.println("ProtocolConnection: error stream communication");
			e.printStackTrace();
		}
		// When we're done, for whatever reason, be sure to close
		// the socket, and to notify the ConnectionWatcher object. Note that
		// we have to use synchronized first to lock the watcher
		// object before we can call notify() for it.
		finally {
			try {
				stream.close();
				clientSocket.close();

				// if (dbConnection!=null)
				// dbConnection.close();
			} catch (IOException e3) {
				synchronized (watcher) {
					watcher.notify();
				}
			}
		}
	}// end run

	// This method returns the string representation of the ProtocolConnection.
	// This is the string that will appear in the GUI List.
	public String getInfo() {
		return ("Connected from: " + clientSocket.getInetAddress()
				.getHostName());
	}
}
