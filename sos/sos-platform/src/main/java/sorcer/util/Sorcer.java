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
package sorcer.util;

import sorcer.core.Provider;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.org.rioproject.net.HostUtil;
import sorcer.resolver.Resolver;
import sorcer.service.Context;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Sorcer utility class provides the global environment configuration for
 * the SORCER environment. The class is initialized only once by a static
 * initializer when the Sorcer class is loaded.
 * <p>
 * This includes all the information that is specific to the SORCER environment
 * and is shared among all provider components or even across multiple
 * providers.
 * <p>
 * The information is collected from Sorcer/config/sorcer.env mainly and can be
 * updated by specific values from a provider Jini configuration file, provider
 * properties file, and JVM system properties.
 * <p>
 * A sorcer.env file is searched for in the <code>Sorcer.class</code> directory
 * (sorcer/util/sorcer.env), or in the path given by the JVM system property
 * <code>sorcer.env.file</code>. In development the last option is recommended.
 * <p>
 * The priorities for loading properties are as follows:
 * <ol>
 * <li>First, SORCER environment properties (sorcer.env) are read by the
 * {@link sorcer.core.provider.ServiceProvider}
 * <li>Second, provider configuration defined in Jini configuration file is
 * loaded and it can override any relevant settings in the existing Sorcer
 * object. Provider specific configuration is collected in ProviderConfig
 * {@link sorcer.core.provider.ProviderDelegate}.
 * <li>Third, application-specific provider properties are loaded if specified
 * by attribute <code>properties</code> in the Jini configuration file and they
 * can override relevant sorcer.env properties. While a collection of Jini
 * configuration properties is predefined, in the provider properties file,
 * custom properties can be defined and accessed via
 * {@link sorcer.core.provider.ServiceProvider#getProperty(String key)}.
 * <li>Finally, JVM system properties (<code>sorcer.env.file</code>), if
 * specified, can override settings in the existing Env object.
 * </ol>
 * <p>
 * The SORCER environment includes dataContext data types. These types are similar
 * to MIME types and are loaded like the environment properties
 * <code>sorcer.env</code> described above. They associate applications to a
 * format of data contained in dataContext data nodes. Data types can be either
 * loaded from a file (default name <code>data.formats</code>) or database. A
 * JVM system property <code>sorcer.formats.file</code> can be used to indicate
 * the location and name of a data type file. Data types are defined in service
 * contexts by a particular composite attribute
 * <code>dnt|application|modifiers</code>, see examples in
 * <code>Sorcer/data.formats</code>. Data type associations (for example
 * <code>dnt|etds|object|Hashtable.output</code>) can be used to lookup data
 * nodes in service contexts {@link Context#getMarkedValues(String)}
 *
 */
@SuppressWarnings("rawtypes")
public class Sorcer extends SorcerEnv implements SorcerConstants{

	final static Logger logger = Logger.getLogger(Sorcer.class.getName());

	/**
	 * General database prefix for the SORCER database schema.
	 */
	private static String dbPrefix = "SOC";
	private static String sepChar = ".";

	/**
	 * Default name 'provider.properties' for a file defining provider
	 * properties.
	 */
	public static String PROVIDER_PROPERTIES_FILENAME = "provider.properties";

	/**
	 * Default name 'data.formats' for a file defining service dataContext node
	 * types.
	 */
	private static String CONTEXT_DATA_FORMATS = "data.formats";

	/**
	 * Default name 'servid.per' for a file storing a service registration ID.
	 */
	private static String serviceIdFilename = "servid.per";

	/** Port for a code sever (webster) */
	private static int port = 0;

    /**
	 * Loads the environment from the SORCER file configuration sorcer.env.
	 */
	static {
		loadEnvironment();
	}

	private Sorcer() {
		// system environment utility class
	}

	
	/**
	 * Loads the environment properties from the default filename (sorcer.env)
	 * or as given by the system property <code>sorcer.env.file</code> and
	 * service dataContext types from the default filename (node.types) or the
	 * system property <code>sorcer.formats.file</code>.
	 */
	protected static void loadEnvironment() {
		
		String cntFile = null;
		
		try {
			String envFile = System.getProperty("sorcer.env.file");
			String cdtFrom = "(default)";
			
			if (props==null) SorcerEnv.loadBasicEnvironment();
			cntFile = System.getProperty("sorcer.formats.file");
			
			updateCodebase();
			logger.finer("Sorcer codebase: "
					+ System.getProperty("java.rmi.server.codebase"));
	
			if (cntFile != null) {
				loadDataNodeTypes(cntFile);
				cdtFrom = "(system property)";
			} else {
				cntFile = CONTEXT_DATA_FORMATS;
				loadDataNodeTypes(cntFile);
				envFrom = "(Sorcer resource)";
			}
			logger.finer("Sorcer loaded " + envFrom + " properties " + envFile);
			
			logger.finer("Data formats loaded " + cdtFrom + " from: "
					+ CONTEXT_DATA_FORMATS);
	
			logger.finer("* Sorcer provider accessor:"
					+ Sorcer.getProperty(SorcerConstants.S_SERVICE_ACCESSOR_PROVIDER_NAME));
		} catch (Throwable t) {
			logger.throwing(
					Sorcer.class.getName(),
					"Unable to find/load SORCER environment configuration files",
					t);
		}
	}

	/**
	 * Returns the hostname of a SORCER class server.
	 * 
	 * @return a webster host name.
	 */
	public static String getWebsterInterface() {
		String hn = System.getenv("SORCER_WEBSTER_INTERFACE");

		if (hn != null && hn.length() > 0) {
			return hn;
		}

		hn = System.getProperty(P_WEBSTER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			return hn;
		}

		hn = props.getProperty(P_WEBSTER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			return hn;
		}

		try {
			hn = HostUtil.getInetAddress().getHostAddress();
		} catch (UnknownHostException e) {
			logger.severe("Cannot determine the webster hostname.");
		}

		return hn;
	}

	/**
	 * Checks which port to use for a SORCER class server.
	 *
	 * @return a port number
	 */
	public static int getWebsterPort() {
		if (port != 0)
			return port;

		String wp = System.getenv("SORCER_WEBSTER_PORT");
		if (wp != null && wp.length() > 0) {
			return new Integer(wp);
		}

		wp = System.getProperty(P_WEBSTER_PORT);
		if (wp != null && wp.length() > 0) {
			return new Integer(wp);
		}

		wp = props.getProperty(P_WEBSTER_PORT);
		if (wp != null && wp.length() > 0) {
			return new Integer(wp);
		}

		try {
			port = getAnonymousPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return port;
	}

	/**
	 * Returns the start port to use for a SORCER code server.
	 * 
	 * @return a port number
	 */
	public static int getWebsterStartPort() {
		String hp = System.getenv("SORCER_WEBSTER_START_PORT");
		if (hp != null && hp.length() > 0) {
			return new Integer(hp);
		}

		hp = System.getProperty(P_WEBSTER_START_PORT);
		if (hp != null && hp.length() > 0) {
			return new Integer(hp);
		}

		hp = props.getProperty(P_WEBSTER_START_PORT);
		if (hp != null && hp.length() > 0) {
			return new Integer(hp);

		}

		return 0;
	}

	/**
	 * Returns the end port to use for a SORCER code server.
	 * 
	 * @return a port number
	 */
	public static int getWebsterEndPort() {
		String hp = System.getenv("SORCER_WEBSTER_END_PORT");

		if (hp != null && hp.length() > 0) {
			return new Integer(hp);
		}

		hp = System.getProperty(P_WEBSTER_END_PORT);
		if (hp != null && hp.length() > 0) {
			return new Integer(hp);
		}

		hp = props.getProperty(P_WEBSTER_END_PORT);
		if (hp != null && hp.length() > 0) {
			return new Integer(hp);
		}

		return 0;
	}

	/**
	 * Returns a registered service ID received previously from a LUS and
	 * persisted in a file.
	 * 
	 * @return service ID filename
	 */
	public static String getServiceIdFilename() {
		return serviceIdFilename;
	}

	/**
	 * Return
	 * <code>true<code> if multicast for lookup discovery is enabled, otherwise <code>false<code>.
	 * 
	 * @return true if multicast is enabled, default is true.
	 */
	public static boolean isMulticastEnabled() {
		return props.getProperty(MULTICAST_ENABLED, "true").equals("true");
	}

	/**
	 * Does a ServiceDiscoveryManager use a lookup cache?
	 * 
	 * @return true if ServiceDiscoveryManager use a lookup cache.
	 */
	public static boolean isLookupCacheEnabled() {
		return props.getProperty(LOOKUP_CACHE_ENABLED, "false").equals("true");
	}

	/**
	 * Returns required wait duration for a ServiceDiscoveryManager.
	 * 
	 * @return wait duration
	 */
	public static Long getLookupWaitTime() {
		return Long.parseLong(getProperty(LOOKUP_WAIT, "500"));
	}

	/**
	 * Returns a number of min lookup matched for a ServiceDiscoveryManager.
	 * 
	 * @return number of min lookup matches
	 */
	public static int getLookupMinMatches() {
		return Integer.parseInt(getProperty(LOOKUP_MIN_MATCHES, "1"));
	}

	/**
	 * Returns a number of max lookup matched for a ServiceDiscoveryManager.
	 * 
	 * @return number of max lookup matches
	 */
	public static int getLookupMaxMatches() {
		return Integer.parseInt(getProperty(LOOKUP_MAX_MATCHES, "999"));
	}

	/**
	 * Loads data node (value) types from the SORCER data store or file. Data
	 * node types specify application types of data nodes in service contexts.
	 * It is analogous to MIME types in SORCER. Each type has a format
	 * 'cnt/application/format/modifiers' or in the association format
	 * 'cnt|application|format|modifiers' when used with {@link Context}.
	 * 
	 * @param filename
	 *            name of file containing service dataContext node type definitions.
	 */
	private static void loadDataNodeTypes(String filename) {
		try {
			// Try in local directory first
			props.load((new FileInputStream(new File(filename))));

		} catch (Throwable t1) {
			try {
				// try to look for sorcer.env in SORCER_HOME/configs
				props.load((new FileInputStream(new File(System.getenv("SORCER_HOME")+"/configs/" + filename))));
				logger.fine("loaded data nodes from: " + System.getenv("SORCER_HOME") +"/configs/" + filename);
			} catch (Exception e) {
				try {
					// Can not access "filename" give try as resource
					// sorcer/util/data.formats
					InputStream stream = Sorcer.class.getResourceAsStream(filename);
					if (stream != null)
						props.load(stream);
					else
						logger.severe("could not load data node types from: "
								+ filename);
				} catch (Throwable t2) {
					logger.severe("could not load data node types: \n"
							+ t2.getMessage());
					logger.throwing(Sorcer.class.getName(), "loadDataNodeTypes", t2);
				}
			}

		}
	}

	/**
	 * Returns the properties. Implementers can use this method instead of the
	 * access methods to cache the environment and optimize performance. Name of
	 * properties are defined in sorcer.util.SORCER.java
	 * 
	 * @return the props
	 */
	public static Properties getSorcerProperites() {
		return props;
	}

	/**
	 * Returns a URL for the SORCER class server.
	 * 
	 * @return the current URL for the SORCER class server.
	 */
	public static String getWebsterUrl() {
		return "http://" + getWebsterInterface() + ':' + getWebsterPort();
	}

	/**
	 * Returns a URL for the SORCER class server.
	 *
	 * @return the current URL for the SORCER class server.
	 */
	public static URL getWebsterUrlURL() {
		try {
			return new URL("http", getWebsterInterface(), getWebsterPort(), "");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getDatabaseStorerUrl() {
		//return "sos://" + DatabaseStorer.class.getName() + '/' + getActualDatabaseStorerName();
		return "sos://DatabaseStorer/" + getActualDatabaseStorerName();
	}

	/**
	 * Returns a URL for the SORCER data server.
	 * 
	 * @return the current URL for the SORCER data server.
	 */
	public static String getDataServerUrl() {
		return "http://" + getDataServerInterface() + ':' + getDataServerPort();
	}

	/**
	 * Returns the hostname of a data server.
	 * 
	 * @return a data server name.
	 */
	public static String getDataServerInterface() {
		String hn = System.getenv("DATA_SERVER_INTERFACE");

		if (hn != null && hn.length() > 0) {
			logger.finer("data server hostname as the system environment value: "
					+ hn);
			return hn;
		}

		hn = System.getProperty(DATA_SERVER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			logger.finer("data server hostname as 'data.server.interface' system property value: "
					+ hn);
			return hn;
		}

		hn = props.getProperty(DATA_SERVER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			logger.finer("data server hostname as 'data.server.interface' provider property value: "
					+ hn);
			return hn;
		}

		try {
			hn = getHostName();
			logger.finer("data.server.interface hostname as the local host value: "
					+ hn);
		} catch (UnknownHostException e) {
			logger.severe("Cannot determine the data.server.interface hostname.");
		}

		return hn;
	}

	/**
	 * Returns the port of a provider data server.
	 * 
	 * @return a data server port.
	 */
	public static int getDataServerPort() {
		String wp = System.getenv("DATA_SERVER_PORT");
		if (wp != null && wp.length() > 0) {
			// logger.finer("data server port as 'DATA_SERVER_PORT': " + wp);
			return new Integer(wp);
		}

		wp = System.getProperty(DATA_SERVER_PORT);
		if (wp != null && wp.length() > 0) {
			// logger.finer("data server port as System 'data.server.port': "
			// + wp);
			return new Integer(wp);
		}

		wp = props.getProperty(DATA_SERVER_PORT);
		if (wp != null && wp.length() > 0) {
			System.out
					.println("data server port as Sorcer 'data.server.port': "
							+ wp);
			return new Integer(wp);
		}

		// logger.severe("Cannot determine the 'data.server.port'.");
		throw new RuntimeException("Cannot determine the 'data.server.port'.");
	}

	/**
	 * Returns the hostname of a provider data server.
	 * 
	 * @return a data server name.
	 */
	public String getProviderDataServerInterface() {
		return System.getProperty(P_DATA_SERVER_INTERFACE);
	}

	/**
	 * Returns the port of a provider data server.
	 * 
	 * @return a data server port.
	 */
	public String getProviderDataServerPort() {
		return System.getProperty(P_DATA_SERVER_PORT);
	}

	/**
	 * 
	 * Specify a URL for the SORCER application server; default is
	 * http://127.0.0.1:8080/
	 * 
	 * @return the current URL for the SORCER application server.
	 */
	public static String getPortalUrl() {
		return props.getProperty("http://" + P_PORTAL_HOST) + ':'
				+ props.getProperty(P_PORTAL_PORT);
	}

	/**
	 * Gets the service locators for unicast discovery.
	 * 
	 * @return and array of strings as locator URLs
	 */
	public static String[] getLookupLocators() {
		String locs = props.getProperty(P_LOCATORS);
		return (locs != null && locs.length() != 0) ? toArray(locs)
				: new String[] {};
	}

	/**
	 * Returns the Jini Lookup Service groups for this environment.
	 * 
	 * @return an array of group names
	 */
	public static String[] getLookupGroups() {
		String[] ALL_GROUPS = null; // Jini ALL_GROUPS
		String groups = props.getProperty(P_GROUPS);
		if (groups == null || groups.length() == 0)
			return ALL_GROUPS;
		String[] providerGroups = toArray(groups);
		return providerGroups;
	}

	/**
	 * Gets a system Cataloger name for this environment.
	 * 
	 * @return a name of the system Cataloger
	 */
	public static String getCatalogerName() {
		return props.getProperty(P_CATALOGER_NAME, "Cataloger");
	}
	
	/**
	 * Returns an the actual Cataloger name, eventually suffixed, to use with this environment.
	 * 
	 * @return a Cataloger actual name
	 */
	public static String getActualCatalogerName() {		
		return getActualName(getCatalogerName());
	}
	
	/**
	 * Gets an exertion space group name to use with this environment.
	 * 
	 * @return a of space group name
	 */
	public static String getSpaceGroup() {
		return props.getProperty(P_SPACE_GROUP, getLookupGroups()[0]);
	}

	/**
	 * Returns an exertion space name to use with this environment.
	 * 
	 * @return a not suffixed space name
	 */

	public static String getSpaceName() {
		return props.getProperty(P_SPACE_NAME, "Exert Space");
	}

	/**
	 * Returns an the actual space name, eventually suffixed, to use with this environment.
	 * 
	 * @return a space actual name
	 */
	public static String getActualSpaceName() {		
		return getActualName(getSpaceName());
	}
	
	/**
	 * Returns whether this cache store should be in a database or local file.
	 * 
	 * @return true if cached to file
	 */
	public static boolean getPersisterType() {
		if ((props.getProperty(S_PERSISTER_IS_DB_TYPE)).equals("true"))
			return true;
		else
			return false;
	}

	/**
	 * Checks which host to use for RMI.
	 * 
	 * @return a name of ExertMonitor provider
	 */
	public static String getExertMonitorName() {
		return props.getProperty(EXERT_MONITOR_NAME, "Exert Monitor");
	}
	
	public static String getActualExertMonitorName() {		
		return getActualName(getExertMonitorName());
	}
	
	public static String getDatabaseStorerName() {
		return props.getProperty(DATABASE_STORER_NAME, "Database Storage");
	}
	
	public static String getActualDatabaseStorerName() {		
		return getActualName(getDatabaseStorerName());
	}
	
	public static String getDataspaceStorerName() {
		return props.getProperty(DATASPACE_STORER_NAME, "Dataspace Storage");
	}
	
	public static String getActualDataspaceStorerName() {		
		return getActualName(getDataspaceStorerName());
	}
	
	public static String getSpacerName() {
		return props.getProperty(SPACER_NAME, "Spacer");
	}
	
	public static String getActualSpacerName() {
		return getActualName(getSpacerName());
	}
	
	/**
	 * Checks which host to use for RMI.
	 * 
	 * @return a hostname
	 */
	public static String getRmiHost() {
		return props.getProperty(S_RMI_HOST);
	}

	/**
	 * Checks which port to use for RMI.
	 * 
	 * @return a port number
	 */
	public static String getRmiPort() {
		return props.getProperty(S_RMI_HOST, "1099");
	}

	/**
	 * Specifies a host to be used for the SORCER SORCER application server. A
	 * default host name is localhost.
	 * 
	 * @return a hostname
	 */
	public static String getPortalHost() {
		return props.getProperty(P_PORTAL_HOST);
	}

	/**
	 * Specifies a port to be used for the SORCER application server. A default
	 * port is 8080.
	 * 
	 * @return a port number
	 */
	public static String getPortalPort() {
		return props.getProperty(P_PORTAL_PORT);
	}

	/**
	 * Checks whether a certain boolean property is set.
	 * 
	 * @param property
	 * @return true if property is set
	 */
	public static boolean isOn(String property) {
		return props.getProperty(property, "false").equals("true");
	}

	/**
	 * Should we use the Oracle DB for the Persister service provider?
	 * 
	 * @return true if we should
	 */
	public static boolean isDbOracle() {
		return props.getProperty(S_IS_DB_ORACLE, "false").equals("true");
	}

	/**
	 * Return true if a modified name is used.
	 * 
	 * @return true if name is suffixed
	 */
	public static boolean nameSuffixed() {
		return props.getProperty(S_IS_NAME_SUFFIXED, "false").equals("true");
	}

	/**
	 * Gets the value of a certain property.
	 * 
	 * @param property
	 * @return the string value of that property
	 */
	public static String getProperty(String property) {
		String p = props.getProperty(property);
		return p;
	}

	/**
	 * Gets the value for a certain property or the default value if property is
	 * not set.
	 * 
	 * @param property
	 * @param defaultValue
	 * @return the string value of that property
	 */
	public static String getProperty(String property, String defaultValue) {
		return props.getProperty(property, defaultValue);
	}

	/**
	 * All database table names start with this schema prefix.
	 * 
	 * @return
	 */
	public static String getDBPrefix() {
		return dbPrefix;
	}

	public static void setDBPrefix(String prefix) {
		dbPrefix = prefix;
	}

	public static String getSepChar() {
		return sepChar;
	}

	public static void setSepChar(String character) {
		sepChar = character;
	}

	/**
	 * @param tableBaseName
	 * @return
	 */
	public static String seqIdPath(String tableBaseName) {
		return dbPrefix + "_" + tableBaseName + "." + tableBaseName + "_Seq_Id";
	}

	/**
	 * @param tableBaseName
	 * @return
	 */
	public static String seqName(String tableBaseName) {
		return dbPrefix + "_" + tableBaseName + "_seq";
	}

	/**
	 * @param tableBaseName
	 * @return
	 */
	public static String seqIdName(String tableBaseName) {
		return tableBaseName + "_Seq_Id";
	}

	/**
	 * @param tableBaseName
	 * @return
	 */
	public static String tableName(String tableBaseName) {
		return dbPrefix + "_" + tableBaseName;
	}

	/**
	 * Uses getRMIHost and getRMIPort to return the RMI registry URL.
	 */
	public static String getRmiUrl() {
		return "rmi://" + Sorcer.getRmiHost() + ":" + Sorcer.getRmiPort() + "/";
	}

	/**
	 * Returns the name of the JNDI dataContext factory.
	 * 
	 * @return a fully qualified name of class of dataContext factory
	 */
	public static String getContextFactory() {
		return "com.sun.jndi.rmi.registry.RegistryContextFactory";
	}

	/**
	 * Returns the JNDI dataContext provider URL string.
	 * 
	 * @return URL string of the JNDI dataContext provider
	 */
	public static String getContextProviderUrl() {
		return getRmiUrl();
	}

	/**
	 * Returns the properties. Implementers can use this method instead of the
	 * access methods to cache the environment and optimize performance.
	 * 
	 * @return the instance of Properties
	 */
	public static Properties getEnvProperties() {
		return props;
	}

	/**
	 * Returns the filename of SORCER environment configuration.
	 *
	 * @return environment configuration filename
	 */
	public static String getEnvFilename() {
		return SorcerConstants.S_ENV_FIENAME;
	}

	/**
	 * Returns the default configuration filename of SORCER provider.
	 * 
	 * @return default configuration filename
	 */
	public static String getConfigFilename() {
		return PROVIDER_PROPERTIES_FILENAME;
	}

	/**
	 * Appends properties from the parameter properties <code>properties</code>
	 * and makes them available as the global SORCER environment properties.
	 * 
	 * @param properties
	 *            the additional properties used to update the
	 *            <code>Sorcer<code>
	 *            properties
	 */
	public static void appendProperties(Properties properties) {
		props.putAll(properties);

	}

	/**
	 * Updates this environment properties from the provider properties
	 * <code>properties</code> and makes them available as the global SORCER
	 * environment properties.
	 * 
	 * @param properties
	 *            the additional properties used to update the
	 *            <code>Sorcer<code>
	 *            properties
	 */
	public static void updateFromProperties(Properties properties) {

		try {
			String val = null;

			val = properties.getProperty(SORCER_HOME);
			if (val != null && val.length() != 0)
				props.put(SORCER_HOME, val);

			val = properties.getProperty(S_JOBBER_NAME);
			if (val != null && val.length() != 0)
				props.put(S_JOBBER_NAME, val);

			val = properties.getProperty(S_CATALOGER_NAME);
			if (val != null && val.length() != 0)
				props.put(S_CATALOGER_NAME, val);

			val = properties.getProperty(S_COMMANDER_NAME);
			if (val != null && val.length() != 0)
				props.put(S_COMMANDER_NAME, val);

			val = properties.getProperty(SORCER_HOME);
			if (val != null && val.length() != 0)
				props.put(SORCER_HOME, val);

			val = properties.getProperty(S_RMI_HOST);
			if (val != null && val.length() != 0)
				props.put(S_RMI_HOST, val);
			val = properties.getProperty(S_RMI_PORT);
			if (val != null && val.length() != 0)
				props.put(S_RMI_PORT, val);

			val = properties.getProperty(P_WEBSTER_INTERFACE);
			if (val != null && val.length() != 0)
				props.put(P_WEBSTER_INTERFACE, val);
			val = properties.getProperty(P_WEBSTER_PORT);
			if (val != null && val.length() != 0)
				props.put(P_WEBSTER_PORT, val);

			val = properties.getProperty(P_PORTAL_HOST);
			if (val != null && val.length() != 0)
				props.put("P_PORTAL_HOST", val);
			val = properties.getProperty(P_PORTAL_PORT);
			if (val != null && val.length() != 0)
				props.put(P_PORTAL_PORT, val);

			// provider data
			val = properties.getProperty(DATA_SERVER_INTERFACE);
			if (val != null && val.length() != 0)
				props.put(DATA_SERVER_INTERFACE, val);
			val = properties.getProperty(DATA_SERVER_PORT);
			if (val != null && val.length() != 0)
				props.put(DATA_SERVER_PORT, val);
			val = properties.getProperty(P_DATA_DIR);
			if (val != null && val.length() != 0)
				props.put(P_DATA_DIR, val);

			val = properties.getProperty(P_SCRATCH_DIR);
			if (val != null && val.length() != 0)
				props.put(P_SCRATCH_DIR, val);

			val = properties.getProperty(LOOKUP_WAIT);
			if (val != null && val.length() != 0)
				props.put(LOOKUP_WAIT, val);

			val = properties.getProperty(LOOKUP_CACHE_ENABLED);
			if (val != null && val.length() != 0)
				props.put(LOOKUP_CACHE_ENABLED, val);

			val = properties.getProperty(P_SERVICE_ID_PERSISTENT);
			if (val != null && val.length() != 0)
				props.put(P_SERVICE_ID_PERSISTENT, val);

			val = properties.getProperty(P_SPACE_GROUP);
			if (val != null && val.length() != 0)
				props.put(P_SPACE_GROUP, val);

			val = properties.getProperty(P_SPACE_NAME);
			if (val != null && val.length() != 0)
				props.put(P_SPACE_NAME, val);

		} catch (AccessControlException ae) {
			ae.printStackTrace();
		}
	}

	/**
	 * Returns the provider's data root directory.
	 * 
	 * @return a provider data root directory
	 */
	public File getDataRootDir() {
		return new File(getProperty(P_DATA_ROOT_DIR));
	}

	/**
	 * Returns the provider's data directory.
	 * 
	 * @return a provider data directory
	 */
	public static File getDataDir() {
		return getDocRootDir();
	}

	/**
	 * Returns a directory for provider's HTTP document root directory.
	 * 
	 * @return a HTTP document root directory
	 */
	public static File getDocRootDir() {
		return new File(System.getProperty(DOC_ROOT_DIR));
	}

	public static File getDataFile(String filename) {
		return new File(getDataDir() + File.separator + filename);
	}

	/**
	 * Returns a directory for providers's scratch files
	 * 
	 * @return a scratch directory
	 */
	static public File getUserHomeDir() {
		return new File(System.getProperty("user.home"));
	}

	/**
	 * Returns a directory for providers's scratch files
	 * 
	 * @return a scratch directory
	 */
	static public File getScratchDir() {
		return Sorcer.getNewScratchDir();
	}

	private static synchronized String getUniqueId() {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-HHmmss");
		long time = System.currentTimeMillis();

		String uid = UUID.randomUUID().toString();
		// return sdf.format(time) + "-" + Long.toHexString(time);
		return sdf.format(time) + "-" + uid;
	}

	/**
	 * Returns a directory for providers's new scratch files
	 * 
	 * @return a scratch directory
	 */
	static public File getNewScratchDir() {
		return getNewScratchDir("");
	}

	static public File getNewScratchDir(String scratchDirNamePrefix) {
		logger.info("scratch_dir = " + System.getProperty(SCRATCH_DIR));
		logger.info("dataDir = " + getDataDir());
		String dirName = getDataDir() + File.separator
				+ System.getProperty(SCRATCH_DIR) + File.separator
				+ getUniqueId();
		File tempdir = new File(dirName);
		File scratchDir = null;
		if (scratchDirNamePrefix == null || scratchDirNamePrefix.length() == 0) {
			scratchDir = tempdir;
		} else {
			scratchDir = new File(tempdir.getParentFile(), scratchDirNamePrefix
					+ tempdir.getName());
		}
		scratchDir.mkdirs();
		return scratchDir;
	}

	public static String getAbsoluteScrachFilename(String filename) {
		return Sorcer.getNewScratchDir() + File.separator + filename;
	}

	/**
	 * Returns the URL of a scratch file at the provider HTTP data server.
	 * 
	 * @param scratchFile
	 * @return the URL of a scratch file
	 * @throws MalformedURLException
	 */
	public static URL getScratchURL(File scratchFile)
			throws MalformedURLException {
		String dataUrl = Sorcer.getDataServerUrl();
		String path = scratchFile.getAbsolutePath();

		String scratchDir = System.getProperty(SCRATCH_DIR);
		logger.info("before scratchDir = " + scratchDir);

		File scratchDirFile = new File(scratchDir);
		scratchDir = scratchDirFile.getPath();
		logger.info("after scratchDir = " + scratchDir);

		int index = path.indexOf(scratchDir);

		logger.info("dataUrl = " + dataUrl);
		logger.info("path = " + path);
		logger.info("scratchDir = " + scratchDir);

		logger.info("DOC_ROOT_DIR = " + System.getProperty(DOC_ROOT_DIR));
		logger.info("substring = "
				+ path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1));
		if (index < 0) {
			throw new MalformedURLException("Scratch file: " + path
					+ " is not in: " + scratchDir);
		}
		String url = dataUrl + File.separator
				+ path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1);
		url = url.replaceAll("\\\\+", "/");
		logger.info("url = " + url);

		return new URL(url);
	}

	/**
	 * Returns the URL of a dataFile at the provider HTTP data server.
	 * 
	 * @param dataFile
	 * @return the URL of a data file
	 * @throws MalformedURLException
	 */
	public static URL getDataURL(File dataFile) throws MalformedURLException {
		String dataUrl = Sorcer.getDataServerUrl();
		String path = dataFile.getAbsolutePath();
		String docDir = System.getProperty(DOC_ROOT_DIR);
		int index = path.indexOf(docDir);
		if (index < 0) {
			throw new MalformedURLException("Data file: " + path
					+ " is not in: " + docDir);
		}
		return new URL(dataUrl + File.separator
				+ path.substring(System.getProperty(DOC_ROOT_DIR).length() + 1));
	}

	/**
	 * Get an anonymous port.
	 * 
	 * @return An anonymous port created by invoking {@link #getPortAvailable()}.
	 *         Once this method is called the return value is set statically for
	 *         future reference
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 */
	public static int getAnonymousPort() throws IOException {
		if (port == 0)
			port = getPortAvailable();
		return port;
	}

	private static void storeEnvironment(String filename) {
		props.setProperty(P_WEBSTER_PORT, "" + port);
		try {
			props.setProperty(P_WEBSTER_INTERFACE, getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		try {
			if (filename != null)
				props.store(new FileOutputStream(filename),
						"SORCER auto-generated environment properties");
			else
				props.store(new FileOutputStream(loadedEnvFile),
						"SORCER auto-generated environment properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get an anonymous port
	 * 
	 * @return An available port created by instantiating a
	 *         <code>java.net.ServerSocket</code> with a port of 0
	 * 
	 * @throws IOException
	 *             If an available port cannot be obtained
	 */
	public static int getPortAvailable() throws java.io.IOException {
		java.net.ServerSocket socket = new java.net.ServerSocket(0);
		int port = socket.getLocalPort();
		socket.close();
		return port;
	}

	public static void setCodeBaseByArtifacts(String[] artifactCoords) {
		String[] jars = new String[artifactCoords.length];
		for (int i=0;i<artifactCoords.length;i++) {
			jars[i] = Resolver.resolveRelative(artifactCoords[i]);
		}
		setCodeBase(jars);		
	}


	public static void updateCodebase() {
		String codebase = System.getProperty("java.rmi.server.codebase");
		if (codebase == null)
			return;
		String pattern = "${localhost}";
		if (codebase.indexOf(pattern) >= 0) {
			try {
				String val = codebase.replace(pattern, getHostAddress());
				System.setProperty("java.rmi.server.codebase", val);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	public static void setCodeBase(String[] jars) {
		String url = getWebsterUrl();		
		String codebase = "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < jars.length - 1; i++) {
			sb.append(url).append("/").append(jars[i]).append(" ");
		}
		sb.append(url).append("/").append(jars[jars.length - 1]);
		codebase = sb.toString();
		System.setProperty("java.rmi.server.codebase", codebase);
		if (logger.isLoggable(Level.FINE))
			logger.fine("Setting codbase 'java.rmi.server.codebase': "
					+ codebase);
	}

	/**
	 * Convert a comma, space, and '|' delimited String to array of Strings
	 * 
	 * @param arg
	 *            The String to convert
	 * 
	 * @return An array of Strings
	 */
	public static String[] toArray(String arg) {
		StringTokenizer token = new StringTokenizer(arg, " ," + APS);
		String[] array = new String[token.countTokens()];
		int i = 0;
		while (token.hasMoreTokens()) {
			array[i] = token.nextToken();
			i++;
		}
		return (array);
	}

	public static void destroy(String providerName, Class serviceType) {
		Provider prv = (Provider) ProviderLookup.getService(providerName,
				serviceType);
		if (prv != null)
			try {
				prv.destroy();
			} catch (Throwable t) {
				// a dead provider will be not responding anymore
				//t.printStackTrace();
			}
	}

	public static void destroyNode(String providerName, Class serviceType) {
		Provider prv = (Provider) ProviderLookup.getService(providerName,
				serviceType);
		if (prv != null)
			try {
				prv.destroyNode();
			} catch (Throwable t) {
				// a dead provider will be not responding anymore
				//t.printStackTrace();
			}
	}

	public static String getNameSuffix() {
		String suffix = props.getProperty(S_NAME_SUFFIX);
		if (suffix == null )
			suffix = getDefaultNameSuffix(3);
		return suffix;
	}

	public static String getDefaultNameSuffix(int suffixLength) {
		return System.getProperty("user.name").substring(0, suffixLength)
				.toUpperCase();
	}
	
	public static String getSuffixedName(String name) {
		String suffix = props.getProperty(S_NAME_SUFFIX,
				getDefaultNameSuffix(3));
		return name + "-" + suffix;
	}

	public static String getActualName(String name) {
		if (nameSuffixed())
			return getSuffixedName(name);
		return name;
	}

	public static String getSuffixedName(String name, int suffixLength) {
		return name + "-" + getDefaultNameSuffix(suffixLength);
	}

	/**
	 * Load dataContext node (value) types from default 'node.types'. SORCER node
	 * types specify application types of data nodes in SORCER service contexts.
	 * It is an analog of MIME types in SORCER. Each type has a format
	 * 'cnt/application/format/modifiers'.
	 */
	public static void loadContextNodeTypes(Hashtable<?, ?> map) {
		if (map != null && !map.isEmpty()) {
			String idName = null, cntName = null;
			String[] tokens;
			for (Enumeration<?> e = map.keys(); e.hasMoreElements();) {
				idName = (String) e.nextElement();
				tokens = toArray(idName);
				cntName = ("".equals(tokens[1])) ? tokens[0] : tokens[1];
				props.put(cntName,
						Context.DATA_NODE_TYPE + APS + map.get(idName));
			}
		}
	}
}