package sorcer.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Rafał Krupiński
 */
public interface AddressProvider {
	InetAddress getLocalAddress() throws UnknownHostException;
}
