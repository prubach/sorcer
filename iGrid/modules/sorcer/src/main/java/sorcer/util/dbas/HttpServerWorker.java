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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Implements the Worker interface for the HttpServer
 */
public class HttpServerWorker implements Worker {
	// Type of message: also used in HttpServerWorker
	final static public int PENDING = 1;
	final static public int DATA = 2;

	/**
	 * Invoked by the Pool when a job comes in for the Worker
	 * 
	 * @param data
	 *            Worker data
	 * @return void
	 */
	public void run(Object data) {
		Socket socket = (Socket) ((Hashtable) data).get("Socket");
		HttpApplicationServer server = (HttpApplicationServer) ((Hashtable) data)
				.get("HttpServer");
		try {
			DataInputStream input = new DataInputStream(
					new BufferedInputStream(socket.getInputStream()));
			String line = input.readLine();
			if (line.toUpperCase().startsWith("POST")) {
				for (; (line = input.readLine()).length() > 0;)
					;
				int type = input.readInt();
				switch (type) {
				case DATA: {
					int length = input.readInt();
					byte buffer[] = new byte[length];
					input.readFully(buffer);
					ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
					server.notifyListener(new ByteArrayInputStream(buffer),
							dataOut);
					DataOutputStream output = new DataOutputStream(
							new BufferedOutputStream(socket.getOutputStream()));
					server.writeResponse(output);
					output.writeInt(DATA);
					output.writeInt(dataOut.toByteArray().length);
					output.write(dataOut.toByteArray());
					output.flush();

					input.close();
					output.close();
					socket.close();
					break;
				}
				case PENDING: {
					// DON'T CLOSE THE SOCKET!
					server.addClient(socket);
					break;
				}
				default: {
					System.err.println("Invalid type: " + type);
				}
				}
			} else {
				System.err.println("Invalid HTTP request: " + line);
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}
