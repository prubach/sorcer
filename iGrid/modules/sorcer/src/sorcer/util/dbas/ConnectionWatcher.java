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

// This class waits to be notified that a thread is dying (exiting)
// and then cleans up the list of threads and the graphical list.
class ConnectionWatcher extends Thread {
	protected SocketApplicationServer server;

	protected ConnectionWatcher(SocketApplicationServer s) {
		super(s.currentConnections, "ConnectionWatcher");
		server = s;
		start();
	}

	// This is the method that waits for notification of exiting threads
	// and cleans up the lists. It is a synchronized method, so it
	// acquires a lock on the `this' object before running. This is
	// necessary so that it can call wait() on this. Even if the
	// the Connection objects never call notify(), this method wakes up
	// every five seconds and checks all the connections, just in case.
	// Note also that all access to the Vector of connections and to
	// the GUI List component are within a synchronized block as well.
	// This prevents the Application Server class from adding a new connection
	// while we're removing an old one.
	public synchronized void run() {
		while (true) {
			try {
				wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Caught an Interrupted Exception");
			}
			// prevent simultaneous access
			synchronized (server.connections) {
				// loop through the connections
				for (int i = 0; i < server.connections.size(); i++) {
					ProtocolConnection c;
					c = (ProtocolConnection) server.connections.elementAt(i);
					// if the connection thread isn't alive anymore,
					// remove it from the Vector and List.
					if (!c.isAlive()) {
						server.connections.removeElementAt(i);
						server.monitor.messageList.delItem(i);
						i--;
					}
				}
			}
		}
	}
}
