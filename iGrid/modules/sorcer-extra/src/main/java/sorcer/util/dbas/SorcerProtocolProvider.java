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

import sorcer.core.SorcerConstants;
import sorcer.util.CmdFactory;
import sorcer.util.Command;
import sorcer.util.EmailCmd;
import sorcer.util.dbas.SessionStore;

/**
 * A factory for the SORCER commands. This factory identifies real commands
 * implementing jgapp.util.Command interface.
 */

public class SorcerProtocolProvider extends CmdFactory implements
		SorcerConstants {
	private static SorcerProtocolProvider factory = new SorcerProtocolProvider();

	public SorcerProtocolProvider() {
		// cmdContext = new Hashtable();
		factory = this;
	}

	public static CmdFactory getInstance() {
		return SorcerProtocolProvider.factory;
	}

	public Command getCmd(String cmdName, String context) {
		cmd = null;

		if (context.equals(CEMAIL))
			cmd = new EmailCmd(cmdName);

		/*----------------FROM DM Applicvation------------------------*/
		// if (context.equals(CFOLDER))
		// cmd = new FolderCmd(cmdName);
		// else if (context.equals(CDOCUMENT))
		// cmd = new DocumentCmd(cmdName);
		// else if (context.equals(CAPPROVAL))
		// cmd = new ApprovalCmd(cmdName);
		// else if (context.equals(CUSER))
		// cmd = new UserCmd(cmdName);
		// else if (context.equals(CROLE))
		// cmd = new RoleCmd(cmdName);
		// else if (context.equals(CGROUP))
		// cmd = new GroupCmd(cmdName);
		// else if (context.equals(CPERMISSION))
		// cmd = new PermissionCmd(cmdName);
		// else if (context.equals(CEMAIL))
		// cmd = new EmailCmd(cmdName);
		// else if (context.equals(FUPLOAD))
		// cmd = new UploadCmd(cmdName);
		// else if (context.equals(SUPDATE))
		// cmd = new UpdateCmd(cmdName);
		// else if (context.equals(DispatchCmd.context))
		// cmd = new DispatchCmd(cmdName);
		// else if (context.equals(CACL))
		// cmd = new ACLCmd(cmdName);
		/*-------------------------------DM Application---------------------*/

		/*-------------------------------SORCER Application------------------*/
		if (context.equals(InfoCmd.context))
			cmd = new InfoCmd(cmdName);
		else if (context.equals(CatalogFinderCmd.context))
			cmd = new CatalogFinderCmd(cmdName);
		else if (context.equals(NotifierCmd.context))
			cmd = new NotifierCmd(cmdName);
		else if (context.equals(SorcerTypeCmd.context))
			cmd = new SorcerTypeCmd(cmdName);
		else if (context.equals(SessionStore.context))
			cmd = new SessionStore(cmdName);
		else if (context.equals(RuntimeJobCmd.context))
			cmd = new RuntimeJobCmd(cmdName);

		/*-------------------------------SORCER Application------------------*/

		return cmd;
	}

	protected String getContext(String cmdName) {
		// context is based on gapp.util.Protocol commands numbers
		int cmdNo = Integer.parseInt(cmdName);
		String context = null;
		System.out.println(getClass().getName() + "getContext() " + cmdName);

		/*-------------------------------DM Application---------------------*/
		if (cmdNo == SERVLET_UPDATE)
			context = SUPDATE;
		// else if (cmdNo==100)
		// context = DispatchCmd.context;
		else if (cmdNo > 100 && cmdNo <= 110)
			context = CUSER;
		else if (cmdNo > 110 && cmdNo < 120)
			context = CGROUP;
		else if (cmdNo >= 120 && cmdNo < 135)
			context = CDOCUMENT;
		else if (cmdNo >= 135 && cmdNo < 140)
			context = CAPPROVAL;
		else if (cmdNo >= 140 && cmdNo < 150)
			context = CPERMISSION;
		else if (cmdNo > 150 && cmdNo <= 160)
			context = CROLE;
		else if (cmdNo >= 160 && cmdNo <= 180)
			context = CFOLDER;
		else if (cmdNo > 180 && cmdNo <= 190)
			context = CEMAIL;
		else if (cmdNo >= UPLOAD_DOC && cmdNo <= UPLOAD_END)
			context = FUPLOAD;
		else if (cmdNo == ACL_ISAUTHORIZED || cmdNo == GET_ACL)
			context = CACL;
		else if (cmdNo >= ADD_DOCUMENT && cmdNo <= UPDATE_DOCUMENT)
			context = CDOCUMENT;
		else if (cmdNo >= ADD_FOLDER && cmdNo <= UPDATE_FOLDER)
			context = CFOLDER;
		/*-------------------------------DM Application---------------------*/

		/*-------------------------------SORCER Application------------------*/
		else if (cmdNo > 500 && cmdNo <= 505)
			context = InfoCmd.context;
		else if (cmdNo > 505 && cmdNo <= 510)
			context = CatalogFinderCmd.context;
		else if (cmdNo >= 1520 && cmdNo <= 1530)
			context = NotifierCmd.context;
		else if (cmdNo >= 1501 && cmdNo <= 1510)
			context = SorcerTypeCmd.context;
		else if (cmdNo >= PERSIST_SORCER_NAME && cmdNo <= RENAME_SORCER_NAME)
			context = SorcerTypeCmd.context;
		else if (cmdNo == STORE_OBJECT || cmdNo == RESTORE_OBJECT)
			context = SessionStore.context;
		else if (cmdNo == GET_RUNTIME_JOBNAMES || cmdNo == GET_RUNTIME_JOB)
			context = RuntimeJobCmd.context;
		else
			return null;

		/*-------------------------------SORCER Application------------------*/
		return context;
	}
}
