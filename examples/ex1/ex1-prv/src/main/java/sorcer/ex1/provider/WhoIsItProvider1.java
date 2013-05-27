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
package sorcer.ex1.provider;

import com.sun.jini.start.LifeCycle;
import sorcer.core.provider.ServiceTasker;
import sorcer.ex1.WhoIsIt;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ServiceExertion;
import sorcer.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

public class WhoIsItProvider1 extends ServiceTasker implements WhoIsIt {

    public WhoIsItProvider1() throws RemoteException {
        super();
    }

	public WhoIsItProvider1(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
        ServiceExertion.debug = true;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getHostName(sorcer.service.Context)
	 */
	@Override
	public Context getHostName(Context context) throws RemoteException,
			ContextException {
		try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String hostname = inetAddress.getHostName();
			context.putValue("provider/hostname", hostname);
            context.putValue("provider/message", "Hello "
                    + context.getValue("requestor/address") + "!");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getHostAddress(sorcer.service.Context)
	 */
	@Override
	public Context getHostAddress(Context context) throws RemoteException,
			ContextException {
		try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
			context.putValue("provider/address", ipAddress);
            context.putValue("provider/message", "Hello "
                    + context.getValue("requestor/address") + "!");
        } catch (UnknownHostException e1) {
			context.reportException(e1);
			e1.printStackTrace();
		}
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getCanonicalHostName(sorcer.service.Context)
	 */
	public Context getCanonicalHostName(Context context)
			throws RemoteException, ContextException {
		try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String fqname = inetAddress.getCanonicalHostName();
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
