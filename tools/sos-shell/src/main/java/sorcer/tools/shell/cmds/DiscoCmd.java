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
package sorcer.tools.shell.cmds;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.*;

import net.jini.admin.Administrable;
import net.jini.admin.JoinAdmin;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;
import sorcer.tools.shell.NetworkShell;
import sorcer.tools.shell.ShellCmd;
import sorcer.util.WhitespaceTokenizer;

public class DiscoCmd extends ShellCmd {

    protected static final int MAX_MATCHES = 64;

	{
		COMMAND_NAME = "disco";

		NOT_LOADED_MSG = "***command not loaded due to conflict";

		COMMAND_USAGE = "disco [<registrar index> | -v | -x]";

		COMMAND_HELP = "List all lookup services discovered for provided groups;"
			+ "\n\twith <registrar index> select the specified registrar as a default one"
			+ "\n\t-v   print the default registrar info"
			+ "\n\t-x   clear the selected registrar and start discovery";
	}

    static private PrintStream out;

	public DiscoCmd() {
	}

	public void execute() throws IOException, ClassNotFoundException {
		// out.println("registrars: " + registrars);
		out = NetworkShell.getShellOutputStream();
		WhitespaceTokenizer myTk = NetworkShell.getShellTokenizer();
		int numTokens = myTk.countTokens();
		int index = 0;
		String next = null;
        List<ServiceRegistrar> registrars = NetworkShell.getRegistrars();
		if (numTokens == 1) {
			next = myTk.nextToken();
			if (next.equals("-x")) {
				NetworkShell.getRegistrars().clear();
				// default index
				NetworkShell.selectedRegistrar = 0;
				NetworkShell.getDisco().terminate();
				// start new lookup discovery
				NetworkShell.setLookupDiscovery(NetworkShell.getGroups());
			}
			else if (next.equals("-v")) {
				if (NetworkShell.selectedRegistrar >= 0
						&& NetworkShell.selectedRegistrar < registrars.size()) {
					describeServiceRegistrar(registrars.get(NetworkShell.selectedRegistrar),
							true);
				}
				else {
					out.println("No selected registrar!");
				}
			} else if (next != null) {
				index = Integer.parseInt(next);
				if (index >= 0 && index < registrars.size())
					NetworkShell.selectedRegistrar = index;
				describeServiceRegistrar(registrars.get(NetworkShell.selectedRegistrar),
						true);
			} else {
				out.println("Wrong argument for selected registrar!");
			}
			return;
		}
		if ((registrars != null) && (registrars.size() > 0)) {
			Iterator it = registrars.iterator();
			while (it.hasNext()) {
				ServiceRegistrar myReg = (ServiceRegistrar) it.next();
				describeServiceRegistrar(myReg, false);
			}
		} else
			System.out.println("Sorry, no lookup services located");
	}

	static public void describeServiceRegistrar(ServiceRegistrar myReg,
			boolean withDetails) throws IOException, ClassNotFoundException {
		String[] groups;
		String msg = "";
		if (out == null) {
			out = NetworkShell.getShellOutputStream();
		}
        List<ServiceRegistrar> registrars = NetworkShell.getRegistrars();
		out.println("--------- LOOKUP SERVICE # " + registrars.indexOf(myReg)
				+ " ---------");
		out.println("ID: " + myReg.getServiceID());
		groups = myReg.getGroups();
		if (groups.length > 0)
			for (int o = 0; o < groups.length; o++) {
				msg += "\'" + groups[o] + "\' ";
			}
		out.println("Groups supported: " + msg);
		out.println("Lookup locator: " + myReg.getLocator().getHost() + ":"
				+ myReg.getLocator().getPort());
		if (withDetails)
			printDetails(myReg);
	}

	static public void printCurrentLus() throws IOException,
			ClassNotFoundException {
        List<ServiceRegistrar> registrars = NetworkShell.getRegistrars();
		if (NetworkShell.selectedRegistrar >= 0) {
			NetworkShell.shellOutput.println("Current lookup service: ");
			if (registrars.size() > 0)
				describeServiceRegistrar(registrars.get(NetworkShell.selectedRegistrar), false);
		} else {
			NetworkShell.shellOutput
					.println("No selected LUS; use 'disco' cmd");
		}
	}
	
	static private void printDetails(ServiceRegistrar myReg) throws IOException,
			ClassNotFoundException {
		Class myCls = myReg.getClass();
		out.println("Proxy class: " + myCls);
		Class[] allIntf = myCls.getInterfaces();
		out.println("Interfaces: ");
		for (int k = 0; k < allIntf.length; k++)
			out.println("  " + allIntf[k].getName());
		
		Administrable admin = null;
		JoinAdmin jAdmin = null;
		if (myReg instanceof Administrable) {
			admin = (Administrable) myReg;
			jAdmin = (JoinAdmin) admin.getAdmin();
			out.println("Lookup locators: "
					+ Arrays.toString(jAdmin.getLookupLocators()));
			ClassLoader cl = myReg.getClass().getClassLoader();
			if (cl instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader) cl).getURLs();
				if (urls.length > 0) {
					out.println("Codbase URLs:");
					for (int l = 1; l < urls.length; l++) {
						out.println("  " + urls[l]);
					}
				}
			}
			NetworkShell.printLookupAttributes(jAdmin.getLookupAttributes());
		}
	}


    public static ServiceItem[] lookup(ServiceRegistrar registrar,
                                       Class[] serviceTypes, String serviceName) throws RemoteException {
        ServiceRegistrar regie = null;
        if (registrar == null) {
            regie = getSelectedRegistrar();
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

    public static ServiceItem[] lookup(
            Class[] serviceTypes) throws RemoteException {
        return lookup(serviceTypes, null);
    }

    public static ServiceItem[] lookup(
            Class[] serviceTypes, String serviceName) throws RemoteException {
        return lookup(null, serviceTypes, serviceName);
    }



    public static ServiceRegistrar getSelectedRegistrar() {
        if (NetworkShell.getRegistrars() != null && NetworkShell.getRegistrars().size() > 0
                && NetworkShell.selectedRegistrar >= 0)
            return NetworkShell.getRegistrars().get(NetworkShell.selectedRegistrar);
        else if (NetworkShell.selectedRegistrar < 0 && NetworkShell.getRegistrars().size() > 0) {
            return NetworkShell.getRegistrars().get(0);
        } else
            return null;
    }

}
