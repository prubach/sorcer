package sorcer.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Get local address using from the default interface
 * 
 * @author Rafał Krupiński
 */
public class AddressProviderImpl implements AddressProvider {
	private static final Sigar sigar = new Sigar();
	private static final Logger log = Logger.getLogger(AddressProviderImpl.class.getName());
	protected InetAddress localAddress;

	protected InetAddress doGetLocalAddress() throws UnknownHostException {
		try {
			NetInterfaceConfig netInterfaceConfig = sigar.getNetInterfaceConfig();
			String[] addressString = netInterfaceConfig.getAddress().split("\\.");
			byte[] address = new byte[addressString.length];
			for (int i = 0; i < addressString.length; i++) {
				address[i] = Short.valueOf(addressString[i]).byteValue();
			}
			return InetAddress.getByAddress(address);
		} catch (SigarException e) {
			log.log(Level.WARNING, "Could not check local address with SIGAR", e);
			return InetAddress.getLocalHost();
		}
	}

	@Override
	public InetAddress getLocalAddress() throws UnknownHostException {
		if (localAddress == null) {
			localAddress = doGetLocalAddress();
		}
		return localAddress;
	}
}
