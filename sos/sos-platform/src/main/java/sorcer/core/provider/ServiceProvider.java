/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
package sorcer.core.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Permission;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.constraint.MethodConstraints;
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
/*
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
*/
import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.discovery.DiscoveryGroupManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.ProxyAccessor;
import net.jini.id.ReferentUuid;
import net.jini.id.Uuid;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.UIDescriptor;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ServerProxyTrust;
import net.jini.security.proxytrust.TrustEquivalence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.*;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.dispatch.MonitoredTaskDispatcher;
import sorcer.core.proxy.Outer;
import sorcer.core.proxy.Partner;
import sorcer.core.proxy.Partnership;
import sorcer.core.service.IServiceBeanListener;
import sorcer.core.service.IServiceBuilder;
import sorcer.service.*;
import sorcer.util.InjectionHelper;
import sorcer.util.ObjectLogger;

import com.sun.jini.config.Config;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.thread.TaskManager;
import sorcer.util.StringUtils;

import static java.util.Collections.addAll;
import static sorcer.core.SorcerConstants.*;

/**
 * The ServiceProvider class is a type of {@link Provider} with dependency
 * injection defined by a Jini 2 configuration, proxy management, and own
 * service discovery management for registering its proxies. In the simplest
 * case, the provider exports and registers its own (outer) proxy with the
 * primary method {@link Service#service(Exertion, Transaction)}. The functionality of an
 * outer proxy can be extended by its inner server functionality with its Remote
 * inner proxy. In this case, the outer proxies have to implement {@link sorcer.core.proxy.Outer}
 * interface and each outer proxy is registered with the inner proxy allocated
 * with {@link Outer#getInner} invoked on the outer proxy. Obviously an outer
 * proxy can implement own interfaces with the help of its embedded inner proxy
 * that in turn can consit of multiple own inner proxies as needed. This class
 * implements the {@link sorcer.core.proxy.Outer} interface, so can extend its functionality by
 * inner proxying.
 * <p>
 * A smart proxy can be defined in the provider's Jini configuration by the
 * <code>smartProxy</code> entry. This proxy does not represent directly any
 * exported server, it is registered with lookup services as is. However, the
 * smart proxy (implementing {@link sorcer.core.proxy.Outer} interface) can extend its
 * functionality by setting the providers outer proxy as its inner proxy. Thus,
 * smart proxies that implement{@link sorcer.core.proxy.Outer}, called <i>semismart</i> contain
 * this provider's proxy as its inner proxy. Smart proxies that do not extend
 * its functionality via its inner proxy are called fat proxies. Fat proxies do
 * not make remote calls back to their providers and the providers just maintain
 * lookup service registrations.
 * <p>
 * An inner or outer proxy can be a surrogate of a service partner defined by
 * this provider using <code>server</code> configuration entry or two realted
 * entries: <code>serverType</code> and <code>serverName</code>. If the entry
 * <code>server</code> is defined and its exporter is defined by the entry
 * <code>serverExporter</code> then this provider will use the server's proxy as
 * the outer (primary proxy) and itself as the inner proxy. However if the
 * exporter is not defined then the provider's proxy is primary (outer) and the
 * server's proxy is the inner one.
 * <p>
 * On the other hand, if the <code>server</code> entry is not defined but at
 * least <code>serverType</code> is defined then the instance of server is
 * created and exported if the entry <code>serverExporter</code> is given. The
 * exported server proxy becomes the primary provider's proxy. However, if no
 * exporter is defined then server proxy becomes the inner proxy of this
 * provider's proxy (outer proxy). Thus, exported servers use outer proxies
 * while not exported user inner proxies of this provider. In this context, a
 * smart proxy implementing {@link sorcer.core.proxy.Outer} interface can get the outer proxy and
 * its inner proxy composed in either direction provider/server proxy
 * relationship.
 * <p>
 * A service method is a method returning {@link sorcer.core.context.ServiceContext} and having a
 * single parameter as {@link sorcer.core.context.ServiceContext}. Service beans are components that
 * implement interfaces in terms of service methods only. A list of service
 * beans can be specified in a provider's Jini configuration as the
 * <code>beans</code> entry. In this case a proxy implementing all interfaces
 * implemented by service beans are dynamically created and registered with
 * lookup services. Multiple SORECER servers can be deployed within a single
 * {@link sorcer.core.provider.ServiceProvider} as its own service beans.
 * 
 * @see Provider
 * @see net.jini.lookup.ServiceIDListener
 * @see ReferentUuid
 * @see sorcer.core.AdministratableProvider
 * @see net.jini.export.ProxyAccessor
 * @see net.jini.security.proxytrust.ServerProxyTrust
 * @see net.jini.core.constraint.RemoteMethodControl
 * @see com.sun.jini.start.LifeCycle
 * @see Partner
 * @see sorcer.core.proxy.Partnership
 * @see sorcer.core.RemoteContextManagement
 * @see sorcer.core.SorcerConstants
 */
public class ServiceProvider implements Identifiable, Provider, ServiceIDListener,
		ReferentUuid, AdministratableProvider, ProxyAccessor, ServerProxyTrust,
		RemoteMethodControl, LifeCycle, Partner, Partnership,
		RemoteContextManagement {
	// RemoteMethodControl is needed to enable Proxy Constraints

    /** Logger and configuration component name for service provider. */
    static final String PROVIDER = ServiceProvider.class.getName();

    /** Logger for logging information about this instance */
    protected static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

	public static final String COMPONENT = ServiceProvider.class.getName();

	protected ProviderDelegate delegate;

	static final String DEFAULT_PROVIDER_PROPERTY = "provider.properties";

	protected TaskManager threadManager;

	int loopCount = 0;

	/** The login context, for logging out */
	private LoginContext loginContext;

	/** The provider's JoinManager. */
	private JoinManager joinManager;

	private LookupDiscoveryManager ldmgr;

	/** Object to notify when this service is destroyed, or null. */
	private LifeCycle lifeCycle;

	// all providers in the same shared JVM
	private static List<ServiceProvider> providers = new ArrayList<ServiceProvider>();

	private String[] accessorGroups = DiscoveryGroupManagement.ALL_GROUPS;
	
	private volatile boolean running = true;

    @Inject
    private IServiceBeanListener beanListener;
    private ProviderServiceBuilder serviceBuilder;

    // service processor specific configurations
    public final Map<Object, Object> configurations = new HashMap<Object, Object>();

    protected ServiceProvider() {
		providers.add(this);
        InjectionHelper.injectMembers(this);
        serviceBuilder = new ProviderServiceBuilder(this);
        delegate = new ProviderDelegate(beanListener, serviceBuilder);
		delegate.provider = this;
	}

	/**
	 * Required constructor for Jini 2 NonActivatableServiceDescriptors
	 * 
	 * @param args
	 * @param lifeCycle
	 * @throws Exception
	 */
	public ServiceProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		this();
		// load Sorcer environment properties via static initializer
		SorcerEnv.getProperties();
        ClassLoader serviceClassLoader = Thread.currentThread().getContextClassLoader();
		final Configuration config = ConfigurationProvider.getInstance(args, serviceClassLoader);
		delegate.setJiniConfig(config);
		String providerProperties = null;
		try {
			providerProperties = (String) Config.getNonNullEntry(config,
					COMPONENT, "properties", String.class, "");
		} catch (ConfigurationException e) {
			logger.warn("init error", e);
		}
		// configure the provider's delegate
		delegate.getProviderConfig().init(true, providerProperties);
		delegate.configure(config);
		// decide if thread management is needed for ServiceExerter
		setupThreadManager();
		init(args, lifeCycle);
	}

	// this is only used to instantiate provider impl objects and use their
	// methods
	public ServiceProvider(String providerPropertiesFile) throws RemoteException {
		this();
		delegate.getProviderConfig().loadConfiguration(providerPropertiesFile);
	}
	
	
	// Implement ServerProxyTrust
	/**
	 * @throws UnsupportedOperationException
	 *             if the server proxy does not implement both
	 *             {@link net.jini.core.constraint.RemoteMethodControl}and {@linkTrustEquivalence}
	 */

	public Permission[] getGrants(Class<?> cl, Principal[] principals) {
		return null;
	}

	public void grant(Class<?> cl, Principal[] principals, Permission[] permissions) {
	}

	public boolean grantSupported() {
		return false;
	}

	public TrustVerifier getProxyVerifier() {
		return delegate.getProxyVerifier();
	}

	@Override
	public MethodConstraints getConstraints() {
		//return ((RemoteMethodControl) delegate.getProxy()).getConstraints();
		return null;
	}

	 
	@Override
    public RemoteMethodControl setConstraints(MethodConstraints constraints) {
		//return (RemoteMethodControl)delegate.getProxy();
		return (RemoteMethodControl) delegate.getProxy();
	}

	/**
	 * Returns an object that implements whatever administration interfaces are
	 * appropriate for the particular service.
	 * 
	 * @return an object that implements whatever administration interfaces are
	 *         appropriate for the particular service.
	 */
	@Override
	public Object getAdmin() {
		return delegate.getAdmin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.OuterProxy#setAdmin(java.lang.Object)
	 */
	public void setAdmin(Object proxy) {
		delegate.setAdmin(proxy);
	}

	/**
	 * Get the current attribute sets for the service.
	 * 
	 * @return the current attribute sets for the service
	 */
	public Entry[] getLookupAttributes() {
		return joinManager.getAttributes();
	}

	/**
	 * Add attribute sets for the service. The resulting set will be used for
	 * all future joins. The attribute sets are also added to all
	 * currently-joined lookup services.
	 * 
	 * @param: attrSets the attribute sets to add
	 * @see net.jini.admin.JoinAdmin#addLookupAttributes(net.jini.core.entry.Entry[])
	 */
	public void addLookupAttributes(Entry[] attrSets) {
		joinManager.addAttributes(attrSets, true);
		logger.debug("Added attributes");
	}

	/**
	 * Modify the current attribute sets, using the same semantics as
	 * ServiceRegistration.modifyAttributes. The resulting set will be used for
	 * all future joins. The same modifications are also made to all
	 * currently-joined lookup services.
	 * 
	 * @param attrSetTemplates
	 *            - the templates for matching attribute sets
	 * @param attrSets
	 *            the modifications to make to matching sets
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#modifyLookupAttributes(net.jini.core.entry.Entry[],
	 *      net.jini.core.entry.Entry[])
	 */
	public void modifyLookupAttributes(Entry[] attrSetTemplates,
			Entry[] attrSets) {
		joinManager.modifyAttributes(attrSetTemplates, attrSets, true);
		logger.debug("Modified attributes");
	}

	/**
	 * Get the list of groups to join. An empty array means the service joins no
	 * groups (as opposed to "all" groups).
	 * 
	 * @return an array of groups to join. An empty array means the service
	 *         joins no groups (as opposed to "all" groups).
	 * 
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#getLookupGroups()
	 */
	public String[] getLookupGroups() {
		return ldmgr.getGroups();
	}

	/**
	 * Add new groups to the set to join. Lookup services in the new groups will
	 * be discovered and joined.
	 * 
	 * @param groups
	 *            groups to join
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#addLookupGroups(String[])
	 */
	public void addLookupGroups(String[] groups) {
		try {
			ldmgr.addGroups(groups);
            logger.debug("Added lookup groups: {}", groups);
		} catch (Exception e) {
            logger.warn("Error while adding groups", e);
        }
	}

	/**
	 * Remove groups from the set to join. Leases are cancelled at lookup
	 * services that are not members of any of the remaining groups.
	 * 
	 * @param groups
	 *            groups to leave
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#removeLookupGroups(String[])
	 */
	public void removeLookupGroups(String[] groups) {
		try {
			ldmgr.removeGroups(groups);
            logger.debug("Removed lookup groups: {}", groups);
		} catch (Exception e) {
            logger.warn("Error while removing groups", e);
		}
	}

	/**
	 * Replace the list of groups to join with a new list. Leases are cancelled
	 * at lookup services that are not members of any of the new groups. Lookup
	 * services in the new groups will be discovered and joined.
	 * 
	 * @param groups
	 *            groups to join
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#setLookupGroups(String[])
	 */
	public void setLookupGroups(String[] groups) {
		try {
			ldmgr.setGroups(groups);
            logger.debug("Set lookup groups: {}", groups);
		} catch (Exception e) {
            logger.warn("Error while setting groups", e);
		}
	}

	/**
	 * Get the list of locators of specific lookup services to join.
	 * 
	 * @return the list of locators of specific lookup services to join
	 * @exception java.rmi.RemoteException
	 * @see net.jini.admin.JoinAdmin#getLookupLocators()
	 */
	public LookupLocator[] getLookupLocators() {
		return ldmgr.getLocators();
	}

	/**
	 * Add locators for specific new lookup services to join. The new lookup
	 * services will be discovered and joined.
	 * 
	 * @param locators
	 *            locators of specific lookup services to join
	 * @exception RemoteException
	 * @see net.jini.admin.JoinAdmin#addLookupLocators(net.jini.core.discovery.LookupLocator[])
	 */
	public void addLookupLocators(LookupLocator[] locators)
			throws RemoteException {
		// for (int i = locators.length; --i >= 0; ) {
		// locators[i] = (LookupLocator)
		// locatorPreparer.prepareProxy(locators[i]);
		// }
		ldmgr.addLocators(locators);
        logger.debug("Added lookup locators: {}", locators);
	}

	/**
	 * Remove locators for specific lookup services from the set to join. Any
	 * leases held at the lookup services are cancelled.
	 * 
	 * @param locators
	 *            locators of specific lookup services to leave
	 * @exception RemoteException
	 * @see net.jini.admin.JoinAdmin#removeLookupLocators(net.jini.core.discovery.LookupLocator[])
	 */
	public void removeLookupLocators(LookupLocator[] locators)
			throws RemoteException {
		// for (int i = locators.length; --i >= 0; ) {
		// locators[i] = (LookupLocator) locatorPreparer.prepareProxy(
		// locators[i]);
		// }
		ldmgr.removeLocators(locators);
        logger.debug("Removed lookup locators: {}", locators);
	}

	// Inherit java doc from super type
	public void setLookupLocators(LookupLocator[] locators)
			throws RemoteException {
		// for (int i = locators.length; --i >= 0; ) {
		// locators[i] = (LookupLocator)
		// locatorPreparer.prepareProxy(locators[i]);
		// }
		ldmgr.setLocators(locators);
			logger.debug("Set lookup locators: {}",
					StringUtils.arrayToString(locators));
	}

	/**
	 * Method invoked by a server to inform the LifeCycle object that it can
	 * release any resources associated with the server.
	 * 
	 * @param impl
	 *            Object reference to the implementation object created by the
	 *            NonActivatableServiceDescriptor. This reference must be equal,
	 *            in the "==" sense, to the object created by the
	 *            NonActivatableServiceDescriptor.
	 * @return true if the invocation was successfully processed and false
	 *         otherwise.
	 */
	public boolean unregister(Object impl) {
		logger.info("Unregistering service");
		if (this == impl)
			try {
				this.destroy();
			} catch (RemoteException re) {
				re.printStackTrace();
			}
		return true;
	}

	/**
	 * Unexport the service provider appropriately.
	 * 
	 * @param force
	 *            terminate in progress calls if necessary
	 * @return true if unexport succeeds
	 * @throws RemoteException 
	 */
	boolean unexport(boolean force) throws NoSuchObjectException {
		return delegate.unexport(force);
	}

	/**
	 * Returns a proxy object for this object. This value should not be null.
	 * Implements the <code>ServiceProxyAccessor</code> interface.
	 * 
	 * @return a proxy object reference
	 * @exception java.rmi.RemoteException
	 */
	public Object getServiceProxy() throws RemoteException {
		return getProxy();
	}

	/**
	 * Returns a proxy object for this provider. If the smart proxy is alocated
	 * then returns a non exported object to be registerd with loookup services.
	 * However, if a smart proxy implements {@link Outer} then the
	 * provider's proxy is set as its inner proxy. Otherwise the {@link java.rmi.Remote}
	 * outer proxy of this provider is returned.
	 * 
	 * @return a proxy, or null
	 * @see Provider#getProxy()
	 */
	public Object getProxy() {
		return delegate.getProxy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.OuterProxy#getInnerProxy()
	 */
	public Remote getInner() {
		return delegate.getInner();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.OuterProxy#setInnerProxy(java.rmi.Remote)
	 */
	public void setInner(Object innerProxy) throws ProviderException {
		delegate.setInner(innerProxy);
	}

	/**
	 * Returns a string representation of this service provider.
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		String className = getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);
		return className + "[" + (delegate!=null ? delegate.getServiceID() : "delegate not set") + "]";
	}

	/**
	 * Simple container for an alternative return a value so we can provide more
	 * detailed diagnostics.
	 */
/*
	class InitException extends Exception {
		private static final long serialVersionUID = 1;

		private InitException(String message, Throwable nested) {
			super(message, nested);
		}
	}
*/

	/**
	 * Portion of construction that is common between the activatable and not
	 * activatable cases. This method performs the minimum number of operations
	 * before establishing the Subject, and logs errors.
	 */
	public void init(String[] configOptions, LifeCycle lifeCycle)
			throws Exception {
		logger.trace("init");
		this.lifeCycle = lifeCycle;

        if (beanListener != null)
            beanListener.preProcess(serviceBuilder);

		try {
			// Take the login context entry from the configuration file, if this
			// entry is null, server will start without a subject
			loginContext = (LoginContext) delegate.getDeploymentConfig().getEntry(
					PROVIDER, "loginContext", LoginContext.class, null);
			if (loginContext == null) {
				initAsSubject();
			} else {
				loginContext.login();

				try {
					// Starting the Service with a subject
					Subject.doAsPrivileged(loginContext.getSubject(),
							new PrivilegedExceptionAction() {
								public Object run() throws Exception {
									initAsSubject();
									return null;
								}
							}, null);
				} catch (PrivilegedActionException e) {
					throw e.getCause();
				}
			}
			logger.info("Provider service started: {} {}", getProviderName(), this);

			// allow for enough time to export the provider's proxy and stay alive
			//new Thread(ProviderDelegate.threadGroup, new KeepAwake()).start();
		} catch (Throwable e) {
			initFailed(e);
		}
	}

	/**
	 * Log information about failing to initialize the service and rethrow the
	 * appropriate exception.
	 * 
	 * @param throwable
	 *            exception produced by the failure
	 */
	static void initFailed(Throwable throwable) throws Exception {
		String message = null;
/*
		if (throwable instanceof InitException) {
			message = throwable.getMessage();
            throwable = throwable.getCause();
		}
*/
		if (logger.isWarnEnabled()) {
			if (message != null) {
                logger.warn("Unable to start provider service: {}", message, throwable);
            } else {
				logger.warn("Unable to start provider service", throwable);
			}
		}
		if (throwable instanceof Exception) {
			throw (Exception) throwable;
		} else if (throwable instanceof Error) {
			throw (Error) throwable;
		} else {
			IllegalStateException ise = new IllegalStateException(
                    throwable.getMessage());
			ise.initCause(throwable);
			throw ise;
		}
	}

	/**
	 * Common construction for activatable and non-activatable cases, run under
	 * the proper Subject. If used, provider properties file has to declared as
	 * "properties" in the provider's Jini configuration file. However
	 * provider's properties can be defined directly in Jini provider's
	 * configuration file - the latter recommended.
	 */
	private void initAsSubject() {
		boolean done = false;
		// Initialize all properties of a provider, from provider properties
		// file and from provider's Jini configuration file
		try {
			delegate.init(this);
		
			// Use locators specified in the Jini configuration file, otherwise
			// from the environment configuration
			// String[] lookupLocators = (String[]) Config.getNonNullEntry(
			// delegate.getJiniConfig(), PROVIDER, "lookupLocators",
			// String[].class, new String[] {});
			String[] lookupLocators = new String[] {};
			String locators = delegate.getProviderConfig().getProperty(P_LOCATORS);
			if (locators != null && locators.length() > 0) {
				lookupLocators = locators.split("[ ,]");
			}
			logger.debug("provider lookup locators: "
                    + (lookupLocators.length == 0 ? "no locators" : Arrays
                    .toString(lookupLocators)));

			String[] lookupGroups = (String[]) Config.getNonNullEntry(
					delegate.getDeploymentConfig(), PROVIDER, "lookupGroups",
					String[].class, new String[] {});
			if (lookupGroups.length == 0)
				lookupGroups = DiscoveryGroupManagement.ALL_GROUPS;
			logger.debug("provider lookup groups: "
                    + (lookupGroups != null ? "all groups" : Arrays
                    .toString(lookupGroups)));

			String[] accessorGroups = (String[]) Config.getNonNullEntry(
					delegate.getDeploymentConfig(), PROVIDER, "accessorGroups",
					String[].class, new String[] {});
			if (accessorGroups.length == 0)
				accessorGroups = lookupGroups;
			logger.debug("service accessor groups: "
                    + (accessorGroups != null ? "all groups" : Arrays
                    .toString(accessorGroups)));

			Entry[] serviceAttributes = getAttributes();

            ServiceID sid = getProviderID();
			if (sid != null) {
				delegate.setProviderUuid(sid);
			} else {
				logger.debug("Provider does not provide ServiceID, using default");
			}
			logger.debug("ServiceID: " + delegate.getServiceID());
			LookupLocator[] locs = new LookupLocator[lookupLocators.length];
			for (int i = 0; i < locs.length; i++) {
				locs[i] = new LookupLocator(lookupLocators[i]);
			}
			// Make sure to turn off multicast discovery if requested
			String[] groups;
			if (SorcerEnv.isMulticastEnabled()) {
				if (lookupGroups != null && lookupGroups.length > 0)
					groups = lookupGroups;
				else
					groups = delegate.groupsToDiscover;
				logger.info("Using multicast");
			} else {
				groups = LookupDiscoveryManager.NO_GROUPS;
				logger.warn("USING UNICAST ONLY");
			}

			logger.info(">>>LookupDiscoveryManager with groups: "
					+ Arrays.toString(groups) + "\nlocators: "
					+ Arrays.toString(locs));
			ldmgr = new LookupDiscoveryManager(groups, locs, null);
			/* registers provider's proxy so this provider is discoverable or not*/
			boolean discoveryEnabled = (Boolean) Config.getNonNullEntry(
					delegate.getDeploymentConfig(), COMPONENT, ProviderDelegate.DISCOVERY_ENABLED,
					boolean.class, true);	
			logger.info(ProviderDelegate.DISCOVERY_ENABLED + ": " + discoveryEnabled);
			Object proxy = null;
			if (discoveryEnabled) {
				proxy = delegate.getProxy();
			} else {
				proxy = delegate.getAdminProxy();
			}
			logger.info("*** PROXY>>>>>registering proxy for: "
					+ getProviderName() + ":" + proxy);

			joinManager = new JoinManager(proxy, serviceAttributes, sid,
					ldmgr, null);

            if (beanListener != null)
                beanListener.preProcess(serviceBuilder);

			done = true;
		} catch (Throwable e) {
			logger.warn("Error initializing service: ", e);
		} finally {
			if (!done) {
				try {
					unexport(true);
				} catch (Exception e) {
					logger.warn("unable to unexport after failure during startup", e);
				}
			}
		}
	}

	/** A trust verifier for secure dynamic and smart proxies. */
	final static class ProxyVerifier implements TrustVerifier, Serializable {
		private final RemoteMethodControl serverProxy;

		private final Uuid serverUuid;

		/**
		 * Create the verifier, throwing UnsupportedOperationException if the
		 * server proxy does not implement both RemoteMethodControl and
		 * TrustEquivalence.
		 */
		public ProxyVerifier(Object serverProxy, Uuid serverUuid) {
			if (serverProxy instanceof RemoteMethodControl
					&& serverProxy instanceof TrustEquivalence) {
				this.serverProxy = (RemoteMethodControl) serverProxy;
			} else {
				throw new UnsupportedOperationException();
			}
			this.serverUuid = serverUuid;
		}

		/** Implement TrustVerifier */
		public boolean isTrustedObject(Object obj, Context ctx)
				throws RemoteException {
			if (obj == null || ctx == null) {
				throw new NullPointerException();
			} else if (!(obj instanceof ProxyAccessor)) {
				return false;
			} else if (!(obj instanceof ReferentUuid))
				return false;

			if (!serverUuid.equals(((ReferentUuid) obj).getReferentUuid()))
				return false;

			RemoteMethodControl otherServerProxy = (RemoteMethodControl) ((ProxyAccessor) obj)
					.getProxy();
			MethodConstraints mc = otherServerProxy.getConstraints();
			TrustEquivalence trusted = (TrustEquivalence) serverProxy
					.setConstraints(mc);
			return trusted.checkTrustEquivalence(otherServerProxy);
		}
	}

	/**
	 * Returns a UI descriptor for this provider to be included in a UI
	 * descriptor for your provider. This method should be implemented in
	 * sublcasses inmplementing the Jini ServiceUI framwork.
	 * 
	 * @return an UI descriptor for your provider. <code>null</code> if not
	 *         overwritten in subclasses.
	 */
	public UIDescriptor getMainUIDescriptor() {
		return null;
	}

    /**
	 * Returns an array of additional service UI descriptors to be included in a
	 * Jini service item that is registerd with lookup services. By default a
	 * generic ServiceProvider service UI is provided with: attribute viewer,
	 * context and task editor for this service provider.
	 * 
	 * @return an array of service UI descriptors
	 */
	public UIDescriptor[] getServiceUIEntries() {
        return new UIDescriptor[0];
	}

	public Map<?, ?> getServiceComponents() {
		return delegate.getServiceComponents();
	}

	public void setServiceComponents(Map<?, ?> serviceComponents) {
		delegate.setServiceComponents(serviceComponents);
	}

	public boolean isSpaceSecurityEnabled() {
		return delegate.isSpaceSecurityEnabled();
	}

	public boolean isMonitorable() {
		return delegate.isMonitorable();
	}

	public Context<?> getContext(Context<?> context) throws RemoteException {
		// TODO can be extended for finding service type and its selector in the
		// context parameter
		try {
			context.putValue(ContextManagement.CONTEXT_REQUEST_PATH,
					getContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}

		return context;
	}

	public java.util.logging.Logger getContextLogger() {
		return java.util.logging.Logger.getLogger("private.context." + delegate.getProviderName());
	}

	public java.util.logging.Logger getProviderLogger() {
		return java.util.logging.Logger.getLogger("private.provider." + delegate.getProviderName());
	}

	public java.util.logging.Logger getRemoteLogger() {
        try {
            String loggerName = (String) getProviderConfiguration().getEntry(
                    ServiceProvider.COMPONENT, ProviderDelegate.REMOTE_LOGGER_NAME,
                    String.class,
                    "remote.sorcer.provider-" + delegate.getProviderName());
            return java.util.logging.Logger.getLogger(loggerName);
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
	}

	/**
	 * Defines rediness of the provider: true if this provider is ready to
	 * process the incoming exertion, otherwise false.
	 * 
	 * @return true if the provider is redy to execute the exertion
	 */
	public boolean isReady(Exertion exertion) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.ContextManagement#getContextScript()
	 */
	@Override
	public String getContextScript() throws RemoteException {
		// implement context management in subcllases
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.getContextScript();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.ContextManagement#getContext()
	 */
	@Override
	public Context<?> getContext() throws RemoteException {
		// implement context management in subcllases
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.getContext();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.core.ContextManagement#getMethodContextScript(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public String getMethodContextScript(String interfaceName, String methodName)
			throws RemoteException {
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.getMethodContextScript(interfaceName,
					methodName);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.core.ContextManagement#currentContextList(java.lang.String
	 * )
	 */
	@Override
	public String[] currentContextList(String interfaceName)
			throws RemoteException {
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.currentContextList(interfaceName);
		else {
			return providerCurrentContextList(interfaceName);
		}
	}

	private String[] providerCurrentContextList(String interfaceName)
			throws RemoteException {
		boolean contextLoaded = false;
		try {
			FileInputStream fis = new FileInputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				theContextMap = (HashMap<String, Context>) in.readObject();
			} catch (ClassNotFoundException e) {
				logger.warn("currentContextList", e);
			}
			in.close();
			fis.close();
			contextLoaded = true;
		} catch (IOException e) {
			logger.warn("currentContextList", e);
			contextLoaded = false;
		}
		String[] toReturn = new String[0];
		if (contextLoaded) {
			Set<String> keys = theContextMap.keySet();
			String[] temp = new String[keys.size()];

			int j = 0;
			for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
				temp[j] = iter.next();
				j++;
			}

			int counter = 0;
			for (int i = 0; i < temp.length; i++)
				if (temp[i].startsWith(interfaceName))
					counter++;
			toReturn = new String[counter];
			counter = 0;
			for (int i = 0; i < temp.length; i++)
				if (temp[i].startsWith(interfaceName)) {
					toReturn[counter] = temp[i].substring(interfaceName
							.length() + 2);
					counter++;
				}
		}
		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.core.ContextManagement#deleteContext(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean deleteContext(String interfaceName, String methodName)
			throws RemoteException {
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.deleteContext(interfaceName, methodName);
		else {
			return providerDeleteContext(interfaceName, methodName);
		}
	}

	private boolean providerDeleteContext(String interfaceName,
			String methodName) throws RemoteException {
		boolean contextLoaded = false;
		try {
			FileInputStream fis = new FileInputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				theContextMap = (HashMap<String, Context>) in.readObject();
			} catch (ClassNotFoundException e) {
				logger.warn("deleteContext", e);
			}
			in.close();
			contextLoaded = true;
		} catch (IOException e) {
			logger.warn("deleteContext", e);
			contextLoaded = false;
		}

		try {
			if (theContextMap.containsKey(interfaceName + ".." + methodName))
				theContextMap.remove(interfaceName + ".." + methodName);
			// theContextMap.put(interfaceName+".."+methodName, theContext);

			FileOutputStream fos = new FileOutputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(theContextMap);
			out.close();
			fos.close();
		} catch (IOException e) {
			logger.warn("deleteContext", e);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.core.ContextManagement#getMethodContext(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Context<?> getMethodContext(String interfaceName, String methodName)
			throws RemoteException {
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.getMethodContext(interfaceName, methodName);
		else {
			return providerGetMethodContext(interfaceName, methodName);
		}
	}

	private Context<?> providerGetMethodContext(String interfaceName,
			String methodName) throws RemoteException {
		logger.info("user directory is " + System.getProperty("user.dir"));
		boolean contextLoaded = false;
		try {
			FileInputStream fis = new FileInputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				theContextMap = (HashMap<String, Context>) in.readObject();
			} catch (ClassNotFoundException e) {
				logger.warn("getMethodContext",
                        e);
			}
			in.close();
			fis.close();
			contextLoaded = true;
		} catch (IOException ioe) {
			// logger.throwing(this.getClass().getName(), "getMethodContext",
			// ioe);
			// logger.info("no context file availabe for the provider: " +
			// getProviderName());
			contextLoaded = false;
		}
		Context context = new ServiceContext();
		if (contextLoaded
				&& theContextMap.containsKey(interfaceName + ".." + methodName)) {
			context = theContextMap.get(interfaceName + ".." + methodName);
		}
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.core.ContextManagement#saveMethodContext(java.lang.String,
	 * java.lang.String, sorcer.service.Context)
	 */
	@Override
	public boolean saveMethodContext(String interfaceName, String methodName,
			Context theContext) throws RemoteException {
		ContextManagement contextManager = delegate.getContextManager();
		if (contextManager != null)
			return contextManager.saveMethodContext(interfaceName, methodName,
					theContext);
		else {
			return providerSaveMethodContext(interfaceName, methodName,
					theContext);
		}
	}

	private boolean providerSaveMethodContext(String interfaceName,
			String methodName, Context<?> theContext) throws RemoteException {
		boolean contextLoaded = false;
		try {
			FileInputStream fis = new FileInputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				theContextMap = (HashMap<String, Context>) in.readObject();
			} catch (ClassNotFoundException e) {
				logger.warn("saveMethodContext",
                        e);
			}
			in.close();
			contextLoaded = true;
		} catch (IOException e) {
			logger.warn("saveMethodContext", e);
			contextLoaded = false;
		}
		theContextMap.put(interfaceName + ".." + methodName, theContext);
		try {

			FileOutputStream fos = new FileOutputStream("../configs/"
					+ ContextManagement.CONTEXT_FILENAME);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(theContextMap);
			out.close();
			fos.close();
		} catch (IOException e) {
			logger.warn("put", e);
			return false;
		}
		return true;
	}

	public String[] getAccessorGroups() {
		return accessorGroups;
	}

	public void setAccessorGroups(String[] accessorGroups) {
		this.accessorGroups = accessorGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.Provider#mutualExclusion()
	 */
	@Override
	public boolean mutualExclusion() throws RemoteException {
		return delegate.mutualExclusion;
	}

	protected synchronized void doTimeKeeping(double callTimeSec) {
		totalCallTime += callTimeSec;
		avgExecTime = totalCallTime / numCalls;
		logger.info("execution time = " + (callTimeSec) + " [s]");
		logger.info("average execution time = " + (avgExecTime) + " [s]");
	}

	// fields for thread metrics
	//
	private int numThreads = 0;
	private ArrayList<String> threadIds = new ArrayList<String>();
	private int numCalls = 0;
	private double avgExecTime = 0;
	private double totalCallTime = 0;

	protected synchronized String doThreadMonitor(String serviceIdString) {
		String prefix;
		if (serviceIdString == null) {
			numCalls++;
			numThreads++;
			prefix = "adding thread";
			serviceIdString = Integer.toString(numCalls);
			threadIds.add(serviceIdString);
		} else {
			numThreads--;
			prefix = "subtracting thread";
			threadIds.remove(serviceIdString);
		}
		logger.info("\n\n***provider class = " + this.getClass()
				+ "\n***" + prefix + ": total calls = " + numCalls
				+ "\n***" + prefix + ": number of threads running = "
				+ numThreads + "\n***" + prefix + ": thread ids running = "
				+ threadIds);

		return serviceIdString;
	}

	public void init() throws RemoteException, ConfigurationException {
		delegate.init(this);
	}

	public void init(String propFile) throws RemoteException, ConfigurationException {
		delegate.init(this, propFile);
	}

	public void init(ProviderDelegate delegate) {
		this.delegate = delegate;
	}

	public ServiceID getProviderID() throws RemoteException {
		return delegate.getServiceID();
	}

	/**
	 * Implements {@link net.jini.lookup.ServiceIDListener}.
	 * <p>
	 * This function is called when a service ID is assigned. It also tries to
	 * persist the service ID.
	 * <p>
	 * TODO: This functionality is similar / identical / linked to
	 * {@link ProviderDelegate#restore()}. Investigate.
	 * 
	 * @param sid
	 *            The assigned ServiceID
	 * 
	 * @see net.jini.lookup.ServiceIDListener#serviceIDNotify(net.jini.core.lookup.ServiceID)
	 */
	public void serviceIDNotify(ServiceID sid) {
		logger.info("Service has been assigned service ID: " + sid.toString());
		delegate.setProviderUuid(sid);
		try {
			// args ->fileName, object, isAbsolutePath
			String fileName = getServiceIDFile();
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			ObjectLogger.persist(fileName, sid, true);
		} catch (Exception e) {
			// e.printStackTrace();
			logger.info("Cannot write service ID to persistent storage. So writting to present directory");
			try {
				File file = new File("provider.sid");
				if (!file.exists())
					file.createNewFile();
				ObjectLogger.persist("provider.sid", sid, true);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Returns a server delegate for this server.
	 * 
	 * @return this server delegate
	 */
	public ProviderDelegate getDelegate() {
		return delegate;
	}

	private String getServiceIDFile() {
		String packagePath = this.getClass().getName();
		packagePath = packagePath.substring(0, packagePath.lastIndexOf("."))
				.replace('.', File.separatorChar);
		String sidFile = new StringBuffer(SorcerEnv.getWebsterUrl())
				.append(File.separatorChar)
				.append(packagePath)
				.append(packagePath.substring(packagePath
						.lastIndexOf(File.separatorChar))).append(".sid")
				.toString();
		return sidFile;
	}

	public long getLeastSignificantBits() throws RemoteException {
		return (delegate.getServiceID() == null) ? -1 : delegate.getServiceID()
				.getLeastSignificantBits();
	}

	public long getMostSignificantBits() throws RemoteException {
		return (delegate.getServiceID() == null) ? -1 : delegate.getServiceID()
				.getMostSignificantBits();
	}

	/**
	 * A provider responsibility is to check a task completeness in paricular
	 * the relevance of the task's context.
	 */
	public boolean isValidTask(Exertion task) throws RemoteException,
			ExertionException {
		return true;
	}

	public String getInfo() throws RemoteException {
		return StringUtils.arrayToString(getAttributes());
	}

	public boolean isValidMethod(String name) throws RemoteException,
			SignatureException {
		return delegate.isValidMethod(name);
	}

	public Context invokeMethod(String method, Context context)
			throws ExertionException {
		return delegate.invokeMethod(method, context);
	}

	public Exertion invokeMethod(String methodName, Exertion ex)
			throws ExertionException {
		return delegate.invokeMethod(methodName, ex);
	}

	/**
	 * This method calls on an ExertionProcessor which executes the exertion
	 * accordingly to its compositional type.
	 * 
	 * @param exertion
	 *            Exertion
	 * @return Exertion
	 * @throws sorcer.service.ExertionException
	 * @see sorcer.service.Exertion
	 * @see sorcer.service.Conditional
	 * @see sorcer.core.provider.ControlFlowManager
	 * @throws java.rmi.RemoteException
	 * @throws sorcer.service.ExertionException
	 */
	public Exertion doExertion(final Exertion exertion, Transaction txn)
			throws ExertionException {
		// create an instance of the ExertionProcessor and call on the
		// process method, returns an Exertion
		ControlFlowManager cfm = null;
		if (this instanceof Jobber) {
			cfm = new ControlFlowManager(exertion, delegate, (Jobber) this);
		} else if (this instanceof Spacer) {
			cfm = new ControlFlowManager(exertion, delegate, (Spacer) this);
		} else if (this instanceof Concatenator) {
			cfm = new ControlFlowManager(exertion, delegate, (Concatenator) this);
		} else if (exertion instanceof Task && exertion.isMonitorable()) {
			if (exertion.isWaitable())
				return doMonitoredTask(exertion, null);
			else {
				// asynchronous execution
				Thread dt = new Thread(new Runnable() {
					public void run() {
						doMonitoredTask(exertion, null);
					}
				});
				try {
					exertion.getContext().putValue(
							ControlContext.EXERTION_WAITED_FROM,
							StringUtils.getDateTime());
				} catch (ContextException e) {
					// ignore it
					e.printStackTrace();
				}
				dt.setDaemon(true);
				dt.start();
				return exertion;
			}
		} else {
			cfm = new ControlFlowManager(exertion, delegate);
		}
		return cfm.process(threadManager);
	}

	public Exertion service(Exertion exertion) throws RemoteException,
			ExertionException {
		return doExertion(exertion, null);
	}

	public Exertion service(Exertion exertion, Transaction txn)
			throws RemoteException {
		// TODO transaction handling to be implemented when needed
		// TO DO HANDLING SUSSPENDED exertions
		// if (((ServiceExertion) exertion).monitorSession != null) {
		// new Thread(new ServiceThread(exertion, this)).start();
		// return exertion;
		// }

		// when service Locker is used
		if (delegate.mutualExlusion()) {
			Object mutexId = ((ControlContext)exertion.getControlContext()).getMutexId();
			if (mutexId == null) {
				exertion.getControlContext().appendTrace(
						"mutex required by: " + getProviderName() + ":"
								+ getProviderID());
				return exertion;
			} else if (!(mutexId.equals(delegate.getServiceID()))) {
				exertion.getControlContext().appendTrace(
						"invalid mutex for: " + getProviderName() + ":"
								+ getProviderID());
				return exertion;
			}
		}
		// allow provider to leave a trace
		// exertion.getControlContext().appendTrace(
		// delegate.mutualExlusion() ? "mutex in: "
		// + getProviderName() + ":" + getProviderID()
		// : "in: " + getProviderName() + ":"
		// + getProviderID());
		Exertion out = exertion;
		try {
			out = doExertion(exertion, txn);
		} catch (ExertionException e) {
			e.printStackTrace();
			((ServiceExertion) out).reportException(new ExertionException(
					getProviderName() + " failed", e));
		}
		return out;
	}

	public ServiceExertion dropTask(ServiceExertion task)
			throws RemoteException, ExertionException, SignatureException {
		return delegate.dropTask(task);
	}

	public Job dropJob(Job job) throws RemoteException, ExertionException {
		return delegate.dropJob(job);
	}

    protected Set<Entry> attributes = new HashSet<Entry>();

    public Entry[] getAttributes() throws RemoteException {
        Set<Entry> result = new HashSet<Entry>(attributes);
        addAll(result, delegate.getAttributes());
        Entry uiDescriptor = getMainUIDescriptor();
        if (uiDescriptor != null)
            result.add(uiDescriptor);
        addAll(result, getServiceUIEntries());
        return result.toArray(new Entry[result.size()]);
    }

	public List<Object> getProperties() throws RemoteException {
		return delegate.getProperties();
	}

	public Properties getProviderProperties() throws RemoteException {
		return delegate.getProviderProperties();
	}

	public Configuration getProviderConfiguration() {
		return delegate.getProviderConfiguration();
	}

	public String getDescription() throws RemoteException {
		return delegate.getDescription();
	}

	public String[] getGroups() throws RemoteException {
		return delegate.getGroups();
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#getName()
	 */
	@Override
	public String getName() {
		return delegate.getProviderName();
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#getId()
	 */
	@Override
	public Object getId() {
		return delegate.getServiceID();
	}
	
	public String getProviderName() throws RemoteException {
		return delegate.getProviderName();
	}

	public void restore() throws RemoteException {
		delegate.restore();
	}

	public void loadConfiguration(String filename) throws RemoteException {
		delegate.getProviderConfig().loadConfiguration(filename);
	}

	/** for a testing purpose only. */
	public void hangup() throws RemoteException {
		delegate.hangup();
	}

	public File getScratchDir() {
		return delegate.getScratchDir();
	}

	public File getScratchDir(String scratchDirNamePrefix) {
		return delegate.getScratchDir(scratchDirNamePrefix);
	}
	
	// this methods generates a scratch directory with a prefix on the unique
	// directory name
	// and puts the directory into the context under a fixed path (path is in
	// SorcerConstants)
	public File getScratchDir(Context context, String scratchDirNamePrefix)
			throws RemoteException {
		File scratchDir;
		try {
			scratchDir = delegate.getScratchDir(context, scratchDirNamePrefix);
		} catch (Exception e) {
			throw new RemoteException("***error: problem getting scratch "
					+ "directory and adding path/url to context"
					+ "\ncontext name = " + context.getName() + "\ncontext = "
					+ context + "\nscratchDirNamePrefix = "
					+ scratchDirNamePrefix + "\nexception = " + e.toString());
		}
		return scratchDir;
	}

	// method adds scratch dir to context
	public File getScratchDir(Context context) throws RemoteException {
		return getScratchDir(context, "");
		// File scratchDir;
		// try {
		// scratchDir = delegate.getScratchDir(context);
		// } catch (Exception e) {
		// throw new RemoteException("***error: problem getting scratch "
		// + "directory and adding path/url to context, exception "
		// + "follows:\n" + e.toString());
		// }
		// return scratchDir;
	}

	public URL getScratchURL(File scratchFile) throws MalformedURLException {
		return delegate.getScratchURL(scratchFile);
	}

	public String getProperty(String key) {
		return delegate.getProviderConfig().getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return delegate.getProviderConfig().getProperty(key, defaultValue);
	}

	public void notifyInformation(Exertion task, String message)
			throws RemoteException {
		delegate.notifyInformation(task, message);
	}

	public void notifyException(Exertion task, String message, Exception e)
			throws RemoteException {
		delegate.notifyException(task, message, e);
	}

	public void notifyExceptionWithStackTrace(Exertion task, Exception e)
			throws RemoteException {
		delegate.notifyExceptionWithStackTrace(task, e);
	}

	public void notifyException(Exertion task, Exception e)
			throws RemoteException {
		delegate.notifyException(task, e);
	}

	public void notifyWarning(Exertion task, String message)
			throws RemoteException {
		delegate.notifyWarning(task, message);
	}

	public void notifyFailure(Exertion task, Exception e)
			throws RemoteException {
		delegate.notifyFailure(task, e);
	}

	public void notifyFailure(Exertion task, String message)
			throws RemoteException {
		delegate.notifyFailure(task, message);
	}

	// task/job monitoring API
	public void stop(UEID ref, Subject subject) throws RemoteException,
			UnknownExertionException, AccessDeniedException {
		delegate.stop(ref, subject);
	}

	/**
	 * MonitorManagers call suspend a MonitorableServicer. Once suspend is
	 * called, the monitorables must suspend immediatly and return the suspended
	 * state of the context.
	 * 
	 * @throws UnknownExertionException
	 *             if the exertion is not executed by this provider.
	 * 
	 * @throws java.rmi.RemoteException
	 *             if there is a communication error
	 * 
	 */

	public void suspend(UEID ref, Subject subject) throws RemoteException,
			UnknownExertionException, AccessDeniedException {
		delegate.suspend(ref, subject);
	}

	/**
	 * Resume if the resume functionality is supported by the provider Else
	 * start from the begining.
	 * 
	 * @throws sorcer.service.UnknownExertionException
	 *             if the exertion is not executed by this provider.
	 * 
	 * @throws java.rmi.RemoteException
	 *             if there is a communication error
	 * 
	 */
	public void resume(Exertion ex) throws RemoteException, ExertionException {
		service(ex);
	}

	/**
	 * Step if the step functionality is supported by the provider Else start
	 * from the begining.
	 * 
	 * @throws sorcer.service.UnknownExertionException
	 *             if the exertion is not executed by this provider.
	 * 
	 * @throws java.rmi.RemoteException
	 *             if there is a communication error
	 * 
	 */
	public void step(Exertion ex) throws RemoteException, ExertionException {
		service(ex);
	}

	/**
	 * Calls the delegate to update the monitor with the current context.
	 * 
	 * @param context
	 * @throws MonitorException
	 * @throws RemoteException
	 */
/*
	public void changed(Context<?> context, Object aspect) throws RemoteException,
			MonitorException {
		delegate.changed(context, aspect);
	}
*/

    /**
     * Destroy the service, if possible, including its persistent storage.
     *
     * @see Provider#destroy()
     */
    public void destroy() throws RemoteException {
        logger.info("Destroying service " + getProviderName());
        if (ldmgr != null)
            ldmgr.terminate();
        if (joinManager != null)
            joinManager.terminate();

        if (threadManager != null)
            threadManager.terminate();

        unexport(true);
        delegate.destroy();
        if (lifeCycle != null) {
            lifeCycle.unregister(this);
        }

        // stop KeepAwake thread
        running = false;
    }
	
	public boolean isBusy() {
		boolean isBusy = false;
		if (threadManager != null)
			isBusy = isBusy || threadManager.getPending().size() > 0;
		isBusy = isBusy || delegate.exertionStateTable.size() > 0;
		return isBusy;
	}
	
	/**
	 * Destroy all services in this node (virtual machine) by calling each
	 * destroy().
	 * 
	 * @see Provider#destroy()
	 */
	public void destroyNode() throws RemoteException {
		for (ServiceProvider provider : providers) {
			provider.destroy();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/** {@inheritDoc} */
	public Uuid getReferentUuid() {
		return delegate.getProviderUuid();
	}

	public void updatePolicy(Policy policy) throws RemoteException {
		if (SorcerEnv.getProperty("sorcer.policer.mandatory").equals("true")) {
			Policy.setPolicy(policy);
		} else {
			logger.info("sorcer.policer.mandatory property in sorcer.env is false");
		}
	}

	public HashMap<String, Context> theContextMap = new HashMap<String, Context>();

	public boolean loadContextDatabase() throws RemoteException {
		try {
			FileInputStream fis = new FileInputStream("context.cxnt");
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				theContextMap = (HashMap<String, Context>) in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			in.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected void setupThreadManager() {
		// TaskManger(int maxThreads, long timeout, float loadFactor)
		// 10, 1000 * 15, 3.0f
		Configuration config = delegate.getDeploymentConfig();
		int maxThreads = 10, waitIncrement = 50;
		long timeout = 1000 * 15;
		float loadFactor = 3.0f;
		boolean threadManagement = false;
		try {
			threadManagement = (Boolean) config.getEntry(
					ServiceProvider.COMPONENT, THREAD_MANAGEMNT, boolean.class,
					false);
		} catch (Exception e) {
			// do nothing, default value is used
			// e.printStackTrace();
		}
		logger.info("threadManagement: " + threadManagement);
		if (!threadManagement) {
			return;
		}

		try {
			maxThreads = (Integer) config.getEntry(ServiceProvider.COMPONENT,
					MAX_THREADS, int.class);
		} catch (Exception e) {
			logger.warn("setupThreadManger#maxThreads", e);
		}
		logger.info("maxThreads: " + maxThreads);
		try {
			timeout = (Long) config.getEntry(ServiceProvider.COMPONENT,
					MANAGER_TIMEOUT, long.class);
		} catch (Exception e) {
			logger.warn("setupThreadManger#timeout", e);
		}
		logger.info("timeout: " + timeout);
		try {
			loadFactor = (Float) config.getEntry(ServiceProvider.COMPONENT,
					LOAD_FACTOR, float.class);
		} catch (Exception e) {
			logger.warn("setupThreadManger#loadFactor", e);
		}
		logger.info("loadFactor: " + loadFactor);
		try {
			waitIncrement = (Integer) config.getEntry(
					ServiceProvider.COMPONENT, WAIT_INCREMENT, int.class,
					waitIncrement);
		} catch (Exception e) {
			logger.warn("setupThreadManger#waitIncrement", e);
		}
		logger.info("waitIncrement: " + waitIncrement);

		ControlFlowManager.WAIT_INCREMENT = waitIncrement;

		 /**
	     * Create a task manager.
	     *
	     * @param maxThreads maximum number of threads to use on tasks
	     * @param timeout idle time before a thread exits 
	     * @param loadFactor threshold for creating new threads.  A new
	     * thread is created if the total number of runnable tasks (both active
	     * and pending) exceeds the number of threads times the loadFactor,
	     * and the maximum number of threads has not been reached.
	     */
		threadManager = new TaskManager(maxThreads, timeout, loadFactor);
	}

	public final static String DB_HOME = "dbHome";
	public final static String THREAD_MANAGEMNT = "threadManagement";
	public final static String MAX_THREADS = "maxThreads";
	public final static String MANAGER_TIMEOUT = "threadTimeout";
	public final static String LOAD_FACTOR = "loadFactor";
	// wait for a TaskThread result in increments
	public final static String WAIT_INCREMENT = "waitForResultIncrement";

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.Provider#getJavaSystemProperties()
	 */
	@Override
	public Properties getJavaSystemProperties() throws RemoteException {
		return System.getProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.provider.Provider#isContextValid(sorcer.service.Context,
	 * sorcer.service.Signature)
	 */
	@Override
	public boolean isContextValid(Context<?> dataContext, Signature forSignature) {
		return true;
	}

	public java.util.logging.Logger getLogger() {
		return java.util.logging.Logger.getLogger(ServiceProvider.class.getName());
	}

	private final int SLEEP_TIME = 250;

	public Exertion doMonitoredTask(Exertion task, Transaction txn) {
		MonitoredTaskDispatcher dispatcher = null;
		try {
			dispatcher = new MonitoredTaskDispatcher(task, null, false, this, null, null);
			while (dispatcher.getState() != Exec.DONE
					&& dispatcher.getState() != Exec.FAILED
					&& dispatcher.getState() != Exec.SUSPENDED) {
				Thread.sleep(SLEEP_TIME);
			}
        } catch (Throwable e) {
            logger.warn("Error while dispatching task", e);
            ((ControlContext)task.getControlContext()).addException(e);
		}
		return dispatcher.getExertion();
	}


/*    // Implement RIO's MonitorableService interface to allow Rio's ProvisionMonitor to check if provider is alive
    @Override
    public void ping() throws RemoteException {
    }

    @Override
    public Lease monitor(long duration) throws LeaseDeniedException, RemoteException {
        if (duration <= 0)
            throw new LeaseDeniedException("lease duration of ["+duration+"] is invalid");
        String phonyResource = this.getClass().getName() + ":"+ System.currentTimeMillis();
        ServiceResource serviceResource = new ServiceResource(phonyResource);
        Lease lease = monitorLandlord.newLease(serviceResource, duration);
        return (lease);
    }

    @Override
    public void startHeartbeat(String[] configArgs) throws ConfigurationException, RemoteException {
        if (heartbeatClient == null)
            heartbeatClient = new HeartbeatClient(getReferentUuid());
        heartbeatClient.addHeartbeatServer(configArgs);
    }*/

}
