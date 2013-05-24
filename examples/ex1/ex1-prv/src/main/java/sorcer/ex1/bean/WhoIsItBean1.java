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

import sorcer.core.Provider;
import sorcer.core.provider.ServiceProvider;
import sorcer.ex1.WhoIsIt;
import sorcer.org.rioproject.net.HostUtil;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.StringUtils;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

public class WhoIsItBean1 implements WhoIsIt {

	private ServiceProvider provider;
    private Logger logger = Logger.getLogger(WhoIsItBean1.class.getName());

    public void init(Provider provider) {
		this.provider = (ServiceProvider)provider;
        try {
            logger = provider.getLogger();
        } catch (RemoteException e) {
            // ignore it, local call
        }
	}
	
	public Context getHostName(Context context) throws RemoteException,
			ContextException {
		String hostname;
        logger.entering(WhoIsItBean2.class.getName(), "getHostName");
		try {
			hostname = HostUtil.getInetAddress().getHostName();
			context.putValue("provider/hostname", hostname);
			context.putValue("provider/message", "Hello "
					+ context.getValue("requestor/hostname") + "!");

            if (provider != null)
                context.appendTrace(getClass().getName() + ":" + provider.getProviderName());

            logger.info("executed getHostName: " + context);

        } catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return context;
	}

	public Context getHostAddress(Context context) throws RemoteException,
			ContextException {
		String ipAddress;
		try {
			ipAddress = HostUtil.getInetAddress().getHostAddress();
			context.putValue("provider/address", ipAddress);
			context.putValue("provider/message", "Hello "
					+ context.getValue("requestor/address") + "!");

            context.appendTrace(getClass().getName() + ":" + provider.getProviderName());

            logger.info("executed getHostName: " + context);
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
			fqname = HostUtil.getInetAddress().getCanonicalHostName();
			context.putValue("provider/fqname", fqname);
            context.putValue("provider/message", "Hello "
                    + context.getValue("requestor/address") + "!");
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
        context.putValue("provider/message", "Hello "
                + context.getValue("requestor/address") + "!");
		return context;
	}
}
