/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.core.provider.proxy;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Permission;
import java.security.Principal;
import java.util.Date;
import java.util.Properties;

import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.entry.Name;
import sorcer.core.Provider;
import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.signature.NetSignature;
import sorcer.core.util.ServiceTypes;
import sorcer.jini.lookup.entry.SorcerServiceInfo;
import sorcer.service.Context;
import sorcer.service.ExecState;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.util.SorcerUtil;

/**
 * A smart proxy for the SORCER service providers. The inner proxy "provider"
 * can point to any other provider(s), functionality of any type that implement
 * own interface(s).
 */
public abstract class SorcerProxy implements Serializable, Provider,
		RemoteMethodControl, SorcerConstants {
	/** The provider's stub */
	protected Provider provider;

	protected NetTask task;

	protected Job job;

	protected String providerName;

	protected String dataDir = "./";

	protected String[] serviceTypes;

	// creates a worker thread when active
	protected boolean isActive;

	protected boolean noSpace = false;

	protected boolean registerInterfaceOnly = false;

	protected static ServiceTypes types;

	protected Properties props;

	public SorcerProxy() throws RemoteException {
		// do nothing
	}

	public Permission[] getGrants(Class cl, Principal[] principals) {
		return null;
	}

	public void grant(Class cl, Principal[] principals, Permission[] permissions) {
	}

	public boolean grantSupported() {
		return false;
	}

	public SorcerProxy(Provider stub) throws RemoteException {
		provider = stub;
	}

	public void init() {
		// do nothing, implement by subclasses if needed
	}

	public void init(Provider stub) throws RemoteException {
		provider = stub;
		// props = ((SorcerServer) stub).getDelegate().getProviderProperties();
	}

	public boolean equals(Object o) {
		return o instanceof SorcerProxy
				&& provider.equals(((SorcerProxy) o).provider);
	}

	public int hashCode() {
		return provider.hashCode();
	}

	public NetTask getTask() throws RemoteException {
		return task;
	}

	public Job getJob() throws RemoteException {
		return job;
	}

	public Context invokeMethod(String method, Context contexts)
			throws RemoteException, SignatureException {

		// Class[] argTypes = new Class[] { contexts.getClass() };
		Class[] argTypes = new Class[] { Context.class };

		try {
			Method m = getClass().getMethod(method, argTypes);
			/*
			 * Util.debug(this, "SorcerProxy.invokeMethod>>Restoring
			 * Observables..."); for (int i=0; i<contexts.length; i++) { try {
			 * contexts[i].restoreDependencies(); } catch (Exception e) {
			 * e.printStackTrace(); } }
			 */
			return (Context) m.invoke(this, new Object[] { contexts });
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			throw new SignatureException("Method: " + method + " doesn't exist");
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
			throw new SignatureException("No permission to invoke a method: "
					+ method);
		} catch (InvocationTargetException e3) {
			e3.printStackTrace();
			throw new SignatureException("Exception ocurred while invoking "
					+ "a method: " + method);
		}
	}

	public boolean isValidMethod(String name) throws RemoteException {
		Method[] methods = getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(name))
				return true;
		}
		return false;
	}

	public String getProviderName() throws RemoteException {
		return providerName;
	}

	public Entry[] getAttributes() throws RemoteException {
		SorcerServiceInfo serviceType = new SorcerServiceInfo();
		serviceType.providerName = providerName;
		serviceType.repository = dataDir;
		serviceType.shortDescription = getProperty(P_DESCRIPTION);
		serviceType.location = getProperty(P_LOCATION);
		serviceType.groups = getProperty(P_GROUPS);
		serviceType.spaceGroup = getProperty(P_SPACE_GROUP);
		serviceType.publishedServices = SorcerUtil.tokenize(
				getProperty(P_INTERFACES), ",");
		serviceType.startDate = new Date().toString();
		serviceType.userDir = System.getProperty("user.dir");
		serviceType.userName = System.getProperty("user.name");

		Entry[] attrs = { new Name(getProviderName()), serviceType };
		return attrs;
	}

	public String getDataDirectory() {
		String baseDir = getProperty("proxy.baseDir");
		String dataDir = getProperty("proxy.dataDir");
		if (baseDir == null || dataDir == null)
			return null;
		baseDir.replace('/', File.separatorChar);
		dataDir.replace('/', File.separatorChar);
		if (!baseDir.endsWith(File.separator)) {
			baseDir += File.separator;
		}
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		return baseDir + dataDir;
	}

	public void restore() throws RemoteException {
		// do nothig
	}

	public ServiceID getServiceID() {
		try {
			return provider.getProviderID();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] getGroups() throws RemoteException {
		if (provider != null)
			return provider.getGroups();
		else
			return null;
	}

	public String getInfo() throws RemoteException {
		return SorcerUtil.arrayToString(getAttributes());
	}

	public Properties getProxyProperties() {
		return props;
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getProperty(String property, String defaultValue) {
		return props.getProperty(property, defaultValue);
	}

	public NetTask doTask(NetTask task) throws ExertionException,
			ExertionException, SignatureException, RemoteException {

		String providerId = task.getProcessSignature().getProviderName();
		Context ctxs;

		/*
		 * String actions = task.method.action(); GuardedObject go = new
		 * GuardedObject(task.method, new ServiceMethodPermission(task.userID,
		 * actions)); try { Object o = go.getObject(); Util.debug(this, "Got
		 * access to method: " + actions); } catch (AccessControlException ace)
		 * { throw new ExertionMethodException ("Can't access method: " +
		 * actions); }
		 */
		String str = getProperty("provider.proxy.class");
		if (str.equals(getClass().getName())) {
			try {
				((NetSignature) task.getProcessSignature()).setServicer(this);
				task = (NetTask) task.exert();
				String desc = getDescription();
				if (desc == null || desc.length() == 0)
					desc = providerName;
				Contexts.putOutValue(task.getContext(), TASK_PROVIDER, desc);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new ExertionException("ContextException caught:"
						+ ex.getMessage());
			}

			// entryTask.setContexts(ctxs);
			task.setStatus(new Integer(ExecState.DONE));
			return task;
		} else
			try {
				return (NetTask) provider.service(task, null);
			} catch (TransactionException te) {
				throw new ExertionException("transaction failure", te);
			}
	}

	public ServiceExertion dropTask(ServiceExertion task)
			throws ExertionException, SignatureException, RemoteException {
		return ((ServiceProvider) provider).dropTask(task);
	}

	public Job doJob(Job job) throws RemoteException, ExertionException {
		return (Job) ((ServiceProvider) provider).doExertion(job, null);
	}

	public Exertion service(Exertion exertion) throws RemoteException,
			ExertionException {
		try {
			if (((ServiceExertion) exertion).isJob())
				return doJob((Job) exertion);
			else if (((ServiceExertion) exertion).isTask())
				return doTask((NetTask) exertion);
			else
				throw new ExertionException("notsupported exertion type",
						exertion);
		} catch (Exception e) {
			throw new ExertionException("service failed", e);
		}
	}

	public Job dropJob(Job job) throws RemoteException, ExertionException {
		return ((ServiceProvider) provider).dropJob(job);
	}

	// for a testing purpose only
	public void hangup() throws RemoteException {
		String str = getProperty("provider.proxy.delay");
		if (str != null) {
			try {
				// delay is in seconds
				int delay = Integer.parseInt(str);
				Thread.sleep(delay * 1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	public String getDescription() throws RemoteException {
		return getProperty("proxy.description",
				getProperty("provider.description"));
	}

	public Properties getProviderProperties() {
		return props;
	}

	public void setProviderProperties(Properties props) {
		this.props = props;
	}

	public void fireEvent() throws RemoteException {
		provider.fireEvent();
	}

	public void destroy() throws RemoteException {
		provider.destroy();
	}

	/* -- Implement RemoteSecurity -- */

	/*
	 * public MethodConstraints getClientConstraints() { return
	 * ((RemoteSecurity) provider).getClientConstraints(); }
	 * 
	 * public RemoteSecurity setClientConstraints(MethodConstraints mc) { return
	 * new SorcerProxy((Provider) ((RemoteSecurity)
	 * provider).setClientConstraints(mc)); }
	 * 
	 * public MethodConstraints getServerConstraints() throws RemoteException {
	 * return ((RemoteSecurity) provider).getServerConstraints(); }
	 * 
	 * public Subject getServerSubject() throws RemoteException { return
	 * ((RemoteSecurity) provider).getServerSubject(); }
	 * 
	 * 
	 * Provide access to the stub to permit the ProxyTrustVerifier class to
	 * verify the proxy.
	 * 
	 * private ProxyTrust getSecureProxy() { return (ProxyTrust) provider; }
	 */
}
