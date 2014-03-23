package sorcer.tools.shell;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Lookup methods working on a selected ServiceRegistrar.
 *
 * Extracted from ShellCmd
 *
 * @author Mike Sobolewski
 */
public class ReggieHelper {
    public static final int MAX_MATCHES = 64;

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
            regie = NetworkShellAccessor.getNetworkShell(Thread.currentThread().getContextClassLoader()).getSelectedRegistrar();
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
        serviceItems.addAll(Arrays.asList(matches.items).subList(0, Math.min(MAX_MATCHES, matches.totalMatches)));
        ServiceItem[] sItems = new ServiceItem[serviceItems.size()];
        return serviceItems.toArray(sItems);
    }

/*
    public static ServiceItem[] serviceLookup(
            Class[] serviceTypes) throws RemoteException {
        ServiceTemplate st = new ServiceTemplate(null, serviceTypes, null);
        return Accessor.getServiceItems(st, null,
                NetworkShell.getGroups());
    }

    static ServiceItem[] serviceLookup(Class[] serviceTypes, String[] groups) {
        ServiceTemplate st = new ServiceTemplate(null, serviceTypes, null);
        return Accessor.getServiceItems(st, null,
                groups);
    }
*/
}
