package jini.intro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.rmi.RMISecurityManager;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

class SummerClient {
	public static void main(String[] args) {

		try {
			System.setSecurityManager(new RMISecurityManager());

			// Perform unicast lookup on localhost
			LookupLocator lookup = new LookupLocator("jini://localhost");

			// Get the service registrar object for the lookup service
			ServiceRegistrar registrar = lookup.getRegistrar();

			// Search the lookup server to find the service that has the
			// name attribute of "SummerService".
			Entry[] attributes = new Entry[1];
			attributes[0] = new Name("SummerService");
			ServiceTemplate template = new ServiceTemplate(null, null,
					attributes);

			// lookup() returns the service object for a service that matches
			// the search criteria passed in as template
			// Here, although I searched by Name attribute, I'm assuming that
			// the object that comes back implements the Summer interface
			Summer summer = (Summer) registrar.lookup(template);

			LineNumberReader stdinReader = new LineNumberReader(
					new BufferedReader(new InputStreamReader(System.in)));

			while (true) {

				String userLine = stdinReader.readLine();

				if (userLine == null || userLine.length() == 0) {
					break;
				}

				String outString;
				try {
					long sum = summer.sumString(userLine);
					outString = Long.toString(sum);
				} catch (InvalidLongException e) {
					outString = e.getMessage();
				}
				System.out.println(outString);
			}
		} catch (Exception e) {
			System.out.println("client: MyClient.main() exception: " + e);
			e.printStackTrace();
		}
	}
}
