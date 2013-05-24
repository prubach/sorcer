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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import sorcer.core.provider.ServiceTasker;
import sorcer.ex1.Message;
import sorcer.ex1.WhoIsIt;
import sorcer.org.rioproject.net.HostUtil;
import sorcer.service.Context;
import sorcer.service.ContextException;

import com.sun.jini.start.LifeCycle;
import sorcer.util.StringUtils;

public class WhoIsItProvider2 extends ServiceTasker implements WhoIsIt {

    public WhoIsItProvider2() throws RemoteException {
        super();
    }

	public WhoIsItProvider2(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}

	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getHostName(sorcer.service.Context)
	 */
	@Override
	public Context getHostName(Context context) throws RemoteException,
			ContextException {
		String hostname;
		logger.info("Got dataContext to process: " + context);
		try {
			hostname = HostUtil.getInetAddress().getHostName();
			context.putValue("provider/hostname", hostname);
			String rhn = (String) context.getValue("requestor/hostname");
			Message msg = (Message) context.getValue("requestor/message");
			context.putValue("provider/message", new ProviderMessage(msg
					.getMessage(), getProviderName(), rhn));
			
			Thread.sleep(1000);
			context.reportException(new RuntimeException("getHostName: slept for 1 sec"));
			context.appendTrace(getClass().getName() + ":" + getProviderName());
			
			Logger cl = getContextLogger();
			if (cl != null)
				cl.info("executed getHostName: " + context);
			
			Logger pl = getProviderLogger();
			if (pl != null)
				pl.info("executed getHostName: " + context);
			
			Logger rl = getRemoteLogger();
			if (rl != null)
				rl.info(">>>>>>>>>>>> out dataContext: " + context);
		} catch (UnknownHostException e1) {
			context.reportException(e1);
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			context.reportException(e2);
			e2.printStackTrace();
		}
		return context;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.ex1.provider.WhoIsIt#getHostAddress(sorcer.service.Context)
	 */
	@Override
	public Context getHostAddress(Context context) throws RemoteException,
			ContextException {
		String ipAddress;
		try {
			ipAddress = HostUtil.getInetAddress().getHostAddress();
			context.putValue("provider/address", ipAddress);
			String rhn = (String) context.getValue("requestor/hostname");
			Message rmsg = (Message) context.getValue("requestor/message");
			context.putValue("provider/message", new ProviderMessage(rmsg
					.getMessage(), getProviderName(), rhn));
			
			Thread.sleep(1000);
			context.reportException(new RuntimeException("getHostAddress: slept for 1 sec"));
			context.appendTrace(getClass().getName() + ":" + getProviderName());
			
			Logger cl = getContextLogger();
			if (cl != null)
				cl.info("executed getHostAddress: " + context);
			
			Logger pl = getProviderLogger();
			if (pl != null)
				pl.info("executed getHostName: " + context);
			
			Logger rl = getRemoteLogger();
			if (rl != null)
				rl.info(">>>>>>>>>>>> out dataContext: " + context);
		} catch (UnknownHostException e1) {
			context.reportException(e1);
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			context.reportException(e2);
			e2.printStackTrace();
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
			String rhn = (String) context.getValue("requestor/hostname");
			Message rmsg = (Message) context.getValue("requestor/message");
			context.putValue("provider/message",
					new ProviderMessage(rmsg.getMessage(), getProviderName(),
							rhn));
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
        String rhn = (String) context.getValue("requestor/hostname");
        Message rmsg = (Message) context.getValue("requestor/message");
        context.putValue("provider/message",
                new ProviderMessage(rmsg.getMessage(), getProviderName(),
                        rhn));
		return context;
	}
}
