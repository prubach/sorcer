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
