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

import java.util.Date;

import javax.servlet.http.HttpSession;

import sorcer.core.SorcerConstants;
import sorcer.util.Command;
import sorcer.util.Invoker;

public class SessionStore implements SorcerConstants, Command {
	String cmdName;
	Object[] args;
	public ApplicationProtocolStatement aps;
	public static String context = "SESSION_STORE";

	public SessionStore(String cmdName) {
		this.cmdName = cmdName;
	}

	public void setArgs(Object target, Object[] args) {
		aps = (ApplicationProtocolStatement) target;
		this.args = args;
	}

	public void doIt() {
		switch (Integer.parseInt(cmdName)) {
		case STORE_OBJECT:
			storeObject();
		case RESTORE_OBJECT:
			restoreObject();
		}
	}

	private void storeObject() {
		if (aps.pConnection.isServletProtocolConnection()) {
			HttpSession session = ((ServletProtocolConnection) aps.pConnection).session;
			String timeStamp;

			String id = session.getId() + SEP
					+ Long.toHexString(new Date().getTime());
			session.setAttribute(id, args[0]);
			aps.objAnswer = id;
		}
	}

	private void restoreObject() {
		if (aps.pConnection.isServletProtocolConnection()) {
			HttpSession session = ((ServletProtocolConnection) aps.pConnection)
					.getSession();
			aps.objAnswer = session.getAttribute((String) args[0]);
		}
	}

	public void setInvoker(Invoker invoker) {
		aps = (ApplicationProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return aps;
	}

	public void undoIt() {
		// do nothing
	}
}
