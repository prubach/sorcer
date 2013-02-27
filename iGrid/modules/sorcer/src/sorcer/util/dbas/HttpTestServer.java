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

import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpTestServer implements ServerListener, Runnable {
	private HttpApplicationServer server;
	private String data;
	private TextArea area;
	private long timeout;
	private int count;

	public HttpTestServer(int port, long timeout) throws Exception {
		this.timeout = timeout;
		Frame f = new Frame("TestServer");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		area = new TextArea(10, 20);
		area.setEditable(false);
		f.add("Center", area);
		f.pack();
		f.show();

		HttpApplicationServer.init(null);
		server = HttpApplicationServer.createServer(port, "admin");
		server.addHttpServerListener(this);

		data = new String("What's Up\n");
		new Thread(this).start();
	}

	public void service(InputStream data, OutputStream out) {
		try {
			DataInputStream input = new DataInputStream(data);
			String s = input.readLine();
			area.append((count++) + "TestClient sent: " + s + "\n");
			input.close();

			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeBytes("Hi");
			dataOut.flush();
			dataOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(timeout);
				server.send(data.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static public void main(String argv[]) throws Exception {
		if (argv.length == 2) {
			new HttpTestServer(Integer.parseInt(argv[0]), Long
					.parseLong(argv[1]));
		} else {
			System.out.println("Usage: java TestServer <port> <send timeout>");
			System.exit(1);
		}
	}
}
