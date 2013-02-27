package jini.intro;

import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.StringTokenizer;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.Name;

public class SummerService2 extends RemoteObject implements Summer,
		ServiceIDListener, Serializable {
	public SummerService2() throws RemoteException {
		super();
	}

	public long sumString(String s) throws InvalidLongException,
			RemoteException {

		System.out.println("sumString(\"" + s + "\")");
		long sum = 0;
		StringTokenizer st = new StringTokenizer(s);
		String token;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			try {
				sum += Long.parseLong(token);
			} catch (NumberFormatException e) {
				throw new InvalidLongException("Invalid number: " + token);
			}
		}

		return sum;
	}

	public void serviceIDNotify(ServiceID idIn) {
		System.out.println("got service id: " + idIn);
	}

	public static void main(String[] args) {

		try {
			System.setSecurityManager(new RMISecurityManager());

			Entry[] attributes = new Entry[1];
			attributes[0] = new Name("SummerService");

			SummerService provider = new SummerService();
			Summer proxy = null;
			try {
				// get the configuration (by default a FileConfiguration)
				Configuration config = ConfigurationProvider.getInstance(args);

				// and use this to construct an exporter
				Exporter exporter = (Exporter) config.getEntry(
						"jini.intro.SummerService", "exporter", Exporter.class);
				// export an object of this class
				proxy = (Summer) exporter.export(provider);
			} catch (Exception e) {
				System.err.println(e.toString());
				e.printStackTrace();
				System.exit(1);
			}

			JoinManager joinMgr = null;
			try {
				LookupDiscoveryManager mgr = new LookupDiscoveryManager(
						LookupDiscovery.ALL_GROUPS, null, // unicast locators
						null); // DiscoveryListener
				joinMgr = new JoinManager(proxy, // service proxy
						attributes, // attr sets
						provider, // ServiceIDListener
						mgr, // DiscoveryManager
						new LeaseRenewalManager());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		} catch (Exception e) {
			System.out.println("SummerService Exception:" + e);
		}
	}
}
