package jini.intro;

import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

// Lookup Service Find - Does discovery to find the nearby lookup
// services and prints out a list
public class LSF implements DiscoveryListener {
	public static void main(String[] args) {

		try {
			System.setSecurityManager(new RMISecurityManager());

			LookupDiscovery ld = new LookupDiscovery(LookupDiscovery.NO_GROUPS);
			ld.addDiscoveryListener(new LSF());
			ld.setGroups(LookupDiscovery.ALL_GROUPS);

			Thread.currentThread().sleep(1000000000L);
		} catch (Exception e) {
			System.out.println("LSF Exception:" + e);
		}
	}

	public void discovered(DiscoveryEvent de) {

		try {
			// Invoke getRegistrar() on the LookupLocator to cause it to
			// perform unicast discovery on the lookup service indicated
			// by the host and port number passed to the constructor.
			// The result is a reference to the registar object sent from
			// the lookup service.
			ServiceRegistrar[] registrars = de.getRegistrars();

			for (int i = 0; i < registrars.length; ++i) {
				// A lookup service is, after all, a service -- a service
				// so proud of itself that it registers itself with itself. To
				// get the ID it assigns to itself, call getServicerID() on
				// the registrar object.
				ServiceID id = registrars[i].getServiceID();

				LookupLocator lookupLocator = registrars[i].getLocator();
				String host = lookupLocator.getHost();
				int port = lookupLocator.getPort();

				System.out.println("Lookup Service: jini://" + host + ":"
						+ port + ", " + id.toString());
			}
		} catch (Exception e) {
			System.out.println("LSF Exception:" + e);
		}
	}

	public void discarded(DiscoveryEvent de) {
	}
}
