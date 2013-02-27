package jini.intro;

import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;

public class LSP {
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

			System.out.println("Lookup Service: jini://" + host + ":" + port);

			// Invoke getRegistrar() on the LookupLocator to cause it to
			// perform unicast discovery on the lookup service indicated
			// by the host and port number passed to the constructor.
			// The result is a reference to the registar object sent from
			// the lookup service.
			ServiceRegistrar registrar = lookupLocator.getRegistrar();

			// A lookup service is, after all, a service -- a service
			// so proud of itself that it registers itself with itself. To
			// get the ID it assigns to itself, call getServicerID() on
			// the registrar object.
			ServiceID id = registrar.getServiceID();

			System.out.println("Lookup Service ID: " + id.toString());
		} catch (Exception e) {
			System.out.println("LSP Exception:" + e);
		}
	}
}
