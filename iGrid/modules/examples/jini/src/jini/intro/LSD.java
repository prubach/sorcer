package jini.intro.jini;

import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

public class LSD {
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
					.println("LSP requires a lookup service URL as first arg.");
			System.out.println("Example: \"LSP jini://localhost:2000\"");
			System.exit(0);
		}

		String urlString = args[0];

		try {
			System.setSecurityManager(new RMISecurityManager());

			// Instantiate a LookupLocator object set to perform
			// unicast discovery on the lookup service indicated
			// by the passed in host and port number. (If no port number
			// is specified, the default port of 4160 will be used.)
			LookupLocator lookupLocator = new LookupLocator(urlString);
			String host = lookupLocator.getHost();
			int port = lookupLocator.getPort();

			System.out.println();
			System.out.println("Lookup Service: jini://" + host + ":" + port);

			// Invoke getRegistrar() on the LookupLocator to cause it to
			// perform unicast discovery on the lookup service indicated
			// by the host and port number passed to the constructor.
			// The result is a reference to the registar object sent from
			// the lookup service.
			ServiceRegistrar registrar = lookupLocator.getRegistrar();

			// Create a ServiceTemplate object that will match all the
			// services in the lookup service.
			ServiceTemplate serviceTemplate = new ServiceTemplate(null, null,
					null);

			// Do the lookup. This is bad LSD in a way, because it will
			// only report at most 1000 services in the lookup service.
			ServiceMatches matches = registrar.lookup(serviceTemplate, 1000);

			System.out.println("Total Services: " + matches.totalMatches);
			System.out.println();
			for (int i = 0; i < matches.totalMatches; ++i) {

				ServiceItem item = matches.items[i];
				String typeName = item.service.getClass().getName();
				String idString = item.serviceID.toString();

				System.out.println(typeName + ": " + idString);
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println("LSD Exception: " + e);
		}
	}
}