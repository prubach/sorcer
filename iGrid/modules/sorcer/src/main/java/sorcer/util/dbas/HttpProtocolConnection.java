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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.DataProtocolStream;
import sorcer.util.SorcerUtil;

// This class is the thread that handles all communication with a client
// It also notifies the ConnectionWatcher when the connection is dropped.
public class HttpProtocolConnection extends ProtocolConnection implements
		ServerListener, SorcerConstants {

	public void service(InputStream data, OutputStream out) {
		try {
			((DataProtocolStream) stream).in = new DataInputStream(data);

			// set user access data
			String s = stream.readLine();
			String[] items = SorcerUtil.tokenize(s, SEP);
			// initialize user data
			SorcerPrincipal principal = new SorcerPrincipal();
			principal.setName(items[ULOGIN]);
			principal.setPassword(items[UPASS]);
			principal.setRole(items[UROLE]);
			principal.setId(items[UOID]);

			((DataProtocolStream) stream).out = new DataOutputStream(out);
			processCmd();

			stream.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}
