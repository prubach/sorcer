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
package sorcer.ex1.bean;

import java.net.UnknownHostException;
import java.rmi.RemoteException;

import sorcer.core.provider.Provider;
import sorcer.core.SorcerEnv;
import sorcer.core.provider.ServiceProvider;
import sorcer.ex1.Message;
import sorcer.ex1.WhoIsIt;
import sorcer.ex1.provider.ProviderMessage;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.StringUtils;

public class WhoIsItBean1 implements WhoIsIt {

	private ServiceProvider provider;
	
	public void init(Provider provider) {
		this.provider = (ServiceProvider)provider;
	}
	
	public Context getHostName(Context context) throws RemoteException,
			ContextException {
		String hostname;
		try {
			hostname = SorcerEnv.getHostName();
			context.putValue("provider/hostname", hostname);
			context.putValue("provider/message", "Hello "
					+ context.getValue("requestor/address") + "!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return context;
	}

	public Context getHostAddress(Context context) throws RemoteException,
			ContextException {
		String ipAddress;
		try {
			ipAddress = SorcerEnv.getHostAddress();
			context.putValue("provider/address", ipAddress);
			context.putValue("provider/message", "Hello "
					+ context.getValue("requestor/address") + "!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getCanonicalHostName(sorcer.service.Context)
	 */
	public Context getCanonicalHostName(Context context)
			throws RemoteException, ContextException {
		String fqname;
		try {
			fqname = SorcerEnv.getLocalHost().getCanonicalHostName();
			context.putValue("provider/fqname", fqname);
			String rhn = (String) context.getValue("requestor/hostname");
			Message rmsg = (Message) context.getValue("requestor/message");
			context.putValue("provider/message", new ProviderMessage(rmsg
					.getMessage(), provider.getProviderName(), rhn));
		} catch (UnknownHostException e1) {
			context.reportException(e1);
			e1.printStackTrace();
		}
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getTimestamp(sorcer.service.Context)
	 */
	@Override
	public Context getTimestamp(Context context) throws RemoteException,
			ContextException {
        context.putValue("provider/timestamp", StringUtils.getDateTime());
		return context;
	}
}
