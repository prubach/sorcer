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

import java.io.IOException;

import sorcer.util.DataProtocolStream;
import sorcer.util.Sorcer;
import sorcer.util.Stopwatch;

/**
 * The ReaderProtocol class implements interface Protocol end expends
 * GAppProtocol having own reader to ApplicationServer.
 * <p>
 * 
 * @see gapp.dbac.GAppProtocol
 */
public class ReaderProtocol extends SocketProtocol {
	public static int conTimeout = 9000;
	// used to keep track of the current datasource connectio
	public String dsURL;
	RemakeConnection rc;
	protected boolean noGUI = false, isReaderRunning = false;
	protected ProtocolReader reader;

	public ReaderProtocol(String asHost, int asPort) {
		super(asHost, asPort, false);
	}

	public ReaderProtocol(String asHost, int asPort, boolean noGUI) {
		super(asHost, asPort, noGUI);
	}

	/**
	 * Make connection to an ApplicationServer, in the case of failure try to
	 * reconnect
	 */
	public void connect() {
		super.connect();
		startReader();
	}

	/**
	 * Create a ProtocolReader, wait for reconnection if needed then allow the
	 * reader to start up
	 */
	public ProtocolReader startReader() {
		// now start event reader thread
		Stopwatch stopwatch = new Stopwatch();
		// if so, then wait to make reconnection or time out
		int attempts = 0;
		while (((DataProtocolStream) stream).out == null) {
			try {
				Thread.currentThread().sleep(3000);
				attempts = attempts + 1;
				if (stopwatch.get() > conTimeout) {
					// allow to exit RemakeConnection thread gracefully
					rc.serverState = true;
					String msg = "Made "
							+ attempts
							+ " attempts,\nnot able to connect to ApplcationServer";

					System.err.println(msg);

					return null;
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				rc = null;
			}
			// if so, then get the new connection and keep going
			if (rc != null && rc.serverState) {
				serverSocket = rc.serverSocket;
				((DataProtocolStream) stream).out = rc.outStream;
				rc = null;
			}
		}

		reader = new ProtocolReader(this);

		// wait for reader thread to get started
		while (!isReaderRunning) {
			// Util.debug(this, "Waiting for reader thread to get started");
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException ie) {
				disconnect();
				return null;
			}
		}
		return reader;
	}

	/**
	 * Disconnects from an ApplicationServer server
	 */
	public void disconnect() {
		super.disconnect();
		while (isReaderRunning) {
			// Util.debug(this, "Waiting for reader thread to die");
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException ie) {
			}
		}
		isReaderRunning = false;
	}

	/**
	 * @Return true, if the ReaderProtocol is connected to an ApplicationServer
	 *         server.
	 */
	public boolean connected() {
		return ((serverSocket != null) && (isReaderRunning));
	}
}

class ProtocolReader extends Thread {
	protected ReaderProtocol protocol;
	public boolean notifyOn = true;

	public ProtocolReader(ReaderProtocol p) {
		super("Protocol Reader");
		this.protocol = p;
		this.start();
	}

	public synchronized void run() {
		protocol.isReaderRunning = true;
		String line = "";

		try {
			while (protocol.isReaderRunning) {
				try {
					if (notifyOn) {
						// Util.debug(this, "ProtocolReader:wait");
						this.wait();
						notifyOn = false;
						protocol.result.removeAllElements();
					}
				} catch (InterruptedException e) {
					System.out
							.println("Exception while reading from application server "
									+ e.getMessage());
				}
				// prevent simultaneous access
				if (((DataProtocolStream) protocol.stream).in == null) {
					System.err
							.println("ERROR:Receiving stream closed connection");
					protocol.result.removeAllElements();
					// Go make the connection
					// protocol.connect();
					break;
				}
				line = protocol.stream.readLine();
				if (line == null) {
					System.err.println("Application Server closed connection");
					protocol.result.removeAllElements();
					break;
				} // if NOT null
				else if (line.equalsIgnoreCase("_DONE_")) {
					// Util.debug(this, "run:" + line);
					notifyOn = true;
				} else {
					protocol.result.addElement(new String(line));
					// Util.debug(this, "run:line:" + line);
				}
			} // while loop
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (((DataProtocolStream) protocol.stream).in != null)
					protocol.stream.close();
				protocol.isReaderRunning = false;
				System.err.println("ProtocolReader:run:finally");
			} catch (IOException e) {
				System.out
						.println("ProtocolReader failed to close client connection "
								+ e);
			}
		}
	}
}
