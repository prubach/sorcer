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

import java.rmi.RemoteException;
import java.security.Principal;

import sorcer.core.SorcerConstants;
import sorcer.core.UEID;
import sorcer.core.monitor.MonitorUIManagement;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.util.Command;
import sorcer.util.Invoker;
import sorcer.util.Mandate;
import sorcer.util.ProviderAccessor;

public class RuntimeJobCmd implements Command, SorcerConstants {

	private String cmdName;
	private SorcerProtocolStatement fps;
	private Object[] args;

	private Mandate resultMandate;

	private static MonitorUIManagement uimanager;
	private Object result;

	public static final String context = "RuntimeJobCmd";

	public RuntimeJobCmd(String cmdName) {

		this.cmdName = cmdName;
		try {
			if (uimanager == null)
				uimanager = (MonitorUIManagement) ProviderAccessor
						.getProvider(MonitorUIManagement.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RuntimeJobCmd(String cmdName, Object[] args) {
		this(cmdName);
		this.args = args;
	}

	public void setArgs(Object target, Object[] args) {
		fps = (SorcerProtocolStatement) target;
		this.args = args;
	}

	public void doIt() {
		resultMandate = new Mandate(Integer.parseInt(cmdName));
		try {
			switch (Integer.parseInt(cmdName)) {
			case GET_RUNTIME_JOBNAMES:
				getRuntimeJobNames();
				break;
			case GET_RUNTIME_JOB:
				getRuntimeJob();
				break;
			default:
				fps.answer = "ERROR:Invalid cmd: " + cmdName;
			}
		} catch (ExertionException e) {
			// fps.answer = "ERROR:" + e.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:ExertionException: " + e.getMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			// fps.answer = "ERROR:" + e.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:RemoteException: " + e.getMessage());
		} catch (SignatureException tme) {
			// fps.answer = "ERROR:" + tme.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:ExertionMethodException: " + tme.getMessage());
		} catch (Exception ex) {
			// fps.answer = "ERROR:" + tme.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:ExertionMethodException: " + ex.getMessage());
		}

		if (result != null)
			resultMandate.getResult().addElement(result);
		else
			resultMandate.getResult().addElement("ERROR: !");
	}

	public void getRuntimeJobNames() throws Exception {
		result = uimanager.getMonitorableExertionInfo(null, null);
	}

	public void getRuntimeJob() throws Exception {
		result = uimanager.getMonitorableExertion((UEID)null, (Principal)null);
	}

	public Mandate getResult() {
		return resultMandate;
	}

	public void setInvoker(Invoker invoker) {
		fps = (SorcerProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return fps;
	}

	public void undoIt() {
		// do nothing
	}
}
