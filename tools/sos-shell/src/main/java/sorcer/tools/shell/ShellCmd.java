/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.tools.shell;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;
import sorcer.tools.shell.cmds.DiscoCmd;
import sorcer.tools.webster.Webster;
import sorcer.util.ServiceAccessor;

import java.rmi.RemoteException;
import java.util.ArrayList;

@SuppressWarnings("rawtypes")
abstract public class ShellCmd {

	protected String COMMAND_NAME;

	protected String NOT_LOADED_MSG;

	protected String COMMAND_USAGE;

	protected String COMMAND_HELP;

	protected static final int MAX_MATCHES = 64;

	abstract public void execute() throws Throwable;

	public String getCommandWord() {
		return COMMAND_NAME;
	}

	public String getUsage(String subCmd) {
		return COMMAND_USAGE;
	}

	public String getShortHelp() {
		return COMMAND_HELP;
	}

	public String getLongDescription(String subCmd) {
		return COMMAND_HELP;
	}

	public String nameConflictDetected(Class<?> conflictClass) {
		return NOT_LOADED_MSG;
	}

	public void initializeSubsystem() {
	}

	public void endSubsystem() {
	}

	public String toString() {
		return getClass().getName() + ": " + COMMAND_NAME;
	}

	public static ServiceItem[] lookup(
			Class[] serviceTypes) throws RemoteException {
		return lookup(serviceTypes, (String)null);
	}
	
	public static ServiceItem[] lookup(
			Class[] serviceTypes, String serviceName) throws RemoteException {
		return lookup(null, serviceTypes, serviceName);
	}

	public static ServiceItem[] lookup(ServiceRegistrar registrar,
			Class[] serviceTypes, String serviceName) throws RemoteException {
		ServiceRegistrar regie = null;
		if (registrar == null) {
			regie = DiscoCmd.getSelectedRegistrar();
			if (regie == null)
				return null;
		} else {
			regie = registrar;
		}

		ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		ServiceMatches matches = null;
		Entry myAttrib[] = null;
		if (serviceName != null) {
			myAttrib = new Entry[1];
			myAttrib[0] = new Name(serviceName);
		}
		ServiceTemplate myTmpl = new ServiceTemplate(null, serviceTypes,
				myAttrib);

		matches = regie.lookup(myTmpl, MAX_MATCHES);
		for (int j = 0; j < Math.min(MAX_MATCHES, matches.totalMatches); j++) {
			serviceItems.add(matches.items[j]);
		}
		ServiceItem[] sItems = new ServiceItem[serviceItems.size()];
		return serviceItems.toArray(sItems);
	}

	public static ServiceItem[] serviceLookup(
			Class[] serviceTypes) throws RemoteException {
		ServiceTemplate st = new ServiceTemplate(null, serviceTypes, null);
		ServiceItem[] serviceItems = ServiceAccessor.getServiceItems(st, null,
				NetworkShell.getGroups());
		return serviceItems;
	}
	
	static ServiceItem[] serviceLookup(Class[] serviceTypes, String[] groups) {
		ServiceTemplate st = new ServiceTemplate(null, serviceTypes, null);
		ServiceItem[] serviceItems = ServiceAccessor.getServiceItems(st, null,
				groups);
		return serviceItems;
	}

}
