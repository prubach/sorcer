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
package sorcer.core;

import org.apache.commons.io.FileUtils;
import sorcer.org.rioproject.net.HostUtil;
import sorcer.service.ConfigurationException;
import sorcer.util.GenericUtil;
import sorcer.util.ParentFirstProperties;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

import static sorcer.core.SorcerConstants.*;

public class SorcerEnv {

	final static Logger logger = Logger.getLogger(SorcerEnv.class.getName());
	/**
	 * Collects all the properties from sorcer.env, related properties from a
	 * provider properties file, provider Jini configuration file, and JVM
	 * system properties.
	 */
	protected static Properties props;

	/*
	 * location of the SORCER environment properties file loaded by this
	 * environment
	 */
	protected static String loadedEnvFile;

	/**
	 * Stores the description of where from the sorcer env was loaded
	 */
	protected static String envFrom;

    /**
     * Indicates if Booter is used in this environment.
     */
    private static boolean bootable = false;


    /**
	 * Loads the environment from the SORCER file configuration sorcer.env.
	 */
	static {
		loadBasicEnvironment();

	}

	/**
	 * Returns the home directory of the Sorcer environment.
	 * 
	 * @return a path of the home directory
	 */
	public static File getHomeDir() {
		String hd = System.getenv("SORCER_HOME");

		if (hd != null && hd.length() > 0) {
            try {
                hd = new File(hd).getCanonicalPath();
            } catch (IOException io) {
            }
			System.setProperty(SORCER_HOME, hd);
			return new File(hd);
		}

		hd = System.getProperty(SORCER_HOME);
		if (hd != null && hd.length() > 0) {
            try {
                hd = new File(hd).getCanonicalPath();
            } catch (IOException io) {
            }
			return new File(hd);
		}

		hd = props.getProperty(SORCER_HOME);
		if (hd != null && hd.length() > 0) {
            try {
                hd = new File(hd).getCanonicalPath();
            } catch (IOException io) {
            }
			return new File(hd);
		}
		throw new IllegalArgumentException(hd
				+ " is not a valid 'sorcer.home' directory");
	}
	
	/** 
	 * @return Sorcer Local jar repo location 
	 */
	public static String getRepoDir() {
		String repo = props.getProperty(S_SORCER_REPO);
        if (repo!=null) repo = repo.trim();
		
		if (repo != null && !repo.isEmpty()) {
			try {
				File repoDir = new File(repo);
				if (repoDir.exists() && repoDir.isDirectory())
					return repo;
			} catch (Throwable t) {
				logger.throwing(
						SorcerEnv.class.getName(),
						"The given Sorcer Jar Repo Location: " + repo + " does not exist or is not a directory!",
						t);
			}			
		} else {
			// Fall back to default location in user's home/.m2
			try {
				File repoDir = new File(System.getProperty("user.home")+"/.m2/repository");
                if (repoDir.exists() && repoDir.isDirectory())
					return repoDir.getAbsolutePath();
                else {
                    FileUtils.forceMkdir(repoDir);
                    return repoDir.getAbsolutePath();
                }
            } catch (Throwable t) {
				logger.throwing(
						SorcerEnv.class.getName(),
						"The given Sorcer Jar Repo Location: " + repo + " does not exist or is not a directory!",
						t);
			}						
		}
		return null;
	}
	
	
	/**
	 * Loads the environment properties from the default filename (sorcer.env)
	 * or as given by the system property <code>sorcer.env.file</code> and
	 * service context types from the default filename (node.types) or the
	 * system property <code>sorcer.formats.file</code>.
	 */
	protected static void loadBasicEnvironment() {
		// Try and load from path given in system properties
		String envFile = null;
		props = new Properties();

		try {
			envFile = System.getProperty("sorcer.env.file");
			envFrom = "(default)";
			String cdtFrom = "(default)";

			if (envFile != null) {
				props.load(new FileInputStream(envFile));
				envFrom = "(system property)";
				update(props);
				loadedEnvFile = envFile;
				
			} else {
				envFile = S_ENV_FIENAME;
				String envPath = getHomeDir() + "/configs/" + envFile;
				System.setProperty(S_KEY_SORCER_ENV, envPath);
				props = loadProperties(envFile);
				update(props);
				envFrom = "(Sorcer resource)";
			}
			logger.fine("SORCER env properties:\n"
					+ GenericUtil.getPropertiesString(props));

		} catch (Throwable t) {
			logger.throwing(
					SorcerEnv.class.getName(),
					"Unable to find/load SORCER environment configuration files",
					t);
		}
	}
	
	/**
	 * Tries to load properties from a <code>filename</code>, first in a local
	 * directory. If there is no file in the local directory then load the file
	 * from the classpath at sorcer/util/sorcer.env.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static Properties loadProperties(String filename)
			throws ConfigurationException {
		Properties properties = new Properties();
		try {
			// Try in user home directory first
			properties.load((new FileInputStream(new File(filename))));
			logger.fine("loaded properties from: " + filename);

		} catch (Exception e) {
			try {
				// try to look for sorcer.env in SORCER_HOME/configs
				properties.load((new FileInputStream(new File(System.getenv("SORCER_HOME")+"/configs/" + filename))));
				logger.fine("loaded properties from: " + System.getenv("SORCER_HOME") +"/configs/" + filename);
			} catch (Exception ee) {
				try {
					// No file give, try as resource sorcer/util/sorcer.env
					InputStream stream = SorcerEnv.class.getResourceAsStream(filename);
					if (stream != null)
						properties.load(stream);
					else
						logger.severe("could not load properties as Sorcer resource file>"
								+ filename + "<" );
				} catch (Throwable t2) {
					throw new ConfigurationException(e);
				}
			}
		}
		reconcileProperties(properties);

		return properties;
	}

	private static void reconcileProperties(Properties properties)
			throws ConfigurationException {

		update(properties);

		// set the document root for HTTP server either for provider or
		// requestor

		String rootDir = null, dataDir = null;
		rootDir = properties.getProperty(P_DATA_ROOT_DIR);
		dataDir = properties.getProperty(P_DATA_DIR);
		if (rootDir != null && dataDir != null) {
			System.setProperty(DOC_ROOT_DIR, rootDir + File.separator + dataDir);
		} else {
			rootDir = properties.getProperty(R_DATA_ROOT_DIR);
			dataDir = properties.getProperty(R_DATA_DIR);
			if (rootDir != null && dataDir != null) {
				System.setProperty(DOC_ROOT_DIR, rootDir + File.separator
						+ dataDir);
			}
		}
		dataDir = properties.getProperty(P_SCRATCH_DIR);
		if (dataDir != null) {
			System.setProperty(SCRATCH_DIR, dataDir);
		} else {
			dataDir = properties.getProperty(R_SCRATCH_DIR);
			if (dataDir != null) {
				System.setProperty(SCRATCH_DIR, dataDir);
			}
		}

		String httpInterface = null, httpPort = null;
		httpInterface = properties.getProperty(P_DATA_SERVER_INTERFACE);
		httpPort = properties.getProperty(P_DATA_SERVER_PORT);
		if (httpInterface != null) {
			System.setProperty(DATA_SERVER_INTERFACE, httpInterface);
			System.setProperty(DATA_SERVER_PORT, httpPort);
		} else {
			httpInterface = properties.getProperty(R_DATA_SERVER_INTERFACE);
			httpPort = properties.getProperty(R_DATA_SERVER_PORT);
			if (httpInterface != null) {
				System.setProperty(DATA_SERVER_INTERFACE, httpInterface);
				System.setProperty(DATA_SERVER_PORT, httpPort);
			}
		}
	}
	

	public static Properties loadProperties(InputStream inputStream)
			throws ConfigurationException {
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
		reconcileProperties(properties);
		return properties;
	}

	public static Properties loadPropertiesNoException(String filename) {
		Properties props = null;
		try {
			props = loadProperties(filename);
		} catch (ConfigurationException e) {
			logger.warning(e.toString());
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Overwrites defined properties in sorcer.env (sorcer.home,
	 * provider.webster.interface, provider.webster.port) with those defined as
	 * JVM system properties.
	 * 
	 * @param properties
	 * @throws ConfigurationException
	 */
	private static void update(Properties properties)
			throws ConfigurationException {
		Enumeration<?> e = properties.propertyNames();
		String key, value, evalue = null;
		String pattern = "${" + "localhost" + "}";
//		String userDirPattern = "${user.home}";
		// first substitute for this localhost
		while (e.hasMoreElements()) {
			key = (String) e.nextElement();
			value = properties.getProperty(key);
			if (value.equals(pattern)) {
				try {
					value = getHostAddress();
				} catch (UnknownHostException ex) {
					ex.printStackTrace();
				}
				properties.put(key, value);
			}
	/*		if (value.equals(userDirPattern)) {
				value = System.getProperty("user.home");				
				properties.put(key, value);
			}*/
		}
		// now substitute other entries accordingly 
		e = properties.propertyNames();
		while (e.hasMoreElements()) {
			key = (String) e.nextElement();
			value = properties.getProperty(key);
			evalue = expandStringProperties(value, true);
			// try SORCER env properties
			if (evalue == null)
				evalue = expandStringProperties(value, false);
			if (evalue != null)
				properties.put(key, evalue);
			if (value.equals(pattern)) {
				try {
					evalue = getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				properties.put(key, evalue);
			}
		}
	}
		
	/**
	 * Return the SORCER environment properties loaded by default from the
	 * 'sorcer.env' file.
	 * 
	 * @return The SORCER environment properties
	 */
	public static Properties getProperties() {
		return props;
	}

	/**
	 * Expands properties embedded in a string with ${some.property.name}. Also
	 * treats ${/} as ${file.separator}.
	 */
	public static String expandStringProperties(String value,
			boolean isSystemProperty) throws ConfigurationException {
		int p = value.indexOf("${", 0);
		if (p == -1) {
			return value;
		}
		int max = value.length();
		StringBuffer sb = new StringBuffer(max);
		int i = 0; /* Index of last character we copied */
		while (true) {
			if (p > i) {
				/* Copy in anything before the special stuff */
				sb.append(value.substring(i, p));
				i = p;
			}
			int pe = value.indexOf('}', p + 2);
			if (pe == -1) {
				/* No matching '}' found, just add in as normal text */
				sb.append(value.substring(p, max));
				break;
			}
			String prop = value.substring(p + 2, pe);
			if (prop.equals("/")) {
				sb.append(File.separatorChar);
			} else {
				try {
					String val = null;
					if (isSystemProperty) {
						val = prop.length() == 0 ? null : System
								.getProperty(prop);
						if (val == null) {
							// try System env
							val = prop.length() == 0 ? null : System
									.getenv(prop);
						}
					} else {
						val = prop.length() == 0 ? null : props
								.getProperty(prop);
					}
					if (val != null) {
						sb.append(val);
					} else {
						return null;
					}
				} catch (SecurityException e) {
					throw new ConfigurationException(e);
				}
			}
			i = pe + 1;
			p = value.indexOf("${", i);
			if (p == -1) {
				/* No more to expand -- copy in any extra. */
				if (i < max) {
					sb.append(value.substring(i, max));
				}
				break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Return the local host address using
	 * <code>java.net.InetAddress.getLocalHost().getHostAddress()</code>
	 * 
	 * @return The local host address
	 * @throws java.net.UnknownHostException
	 *             if no IP address for the local host could be found.
	 */
	public static String getHostAddress() throws java.net.UnknownHostException {
		return getLocalHost().getHostAddress();
	}

	/**
	 * Return the local host address for a passed in host using
	 * {@link java.net.InetAddress#getByName(String)}
	 * 
	 * @param name
	 *            The name of the host to return
	 * 
	 * @return The local host address
	 * 
	 * @throws java.net.UnknownHostException
	 *             if no IP address for the host name could be found.
	 */
	public static String getHostAddress(String name)
			throws java.net.UnknownHostException {
		return java.net.InetAddress.getByName(name).getHostAddress();
	}

	/**
	 * Return the local host name
	 * <code>java.net.InetAddress.getLocalHost().getHostName()</code>
	 * 
	 * @return The local host name
	 * @throws java.net.UnknownHostException
	 *             if no hostname for the local host could be found.
	 */
	public static String getHostName() throws java.net.UnknownHostException {
		return getLocalHost().getCanonicalHostName();
		// return java.net.InetAddress.getLocalHost().getHostName();
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		return HostUtil.getInetAddress();
	}

	public static String[]getWebsterRoots(){
		String websterRootStr = System.getProperty(SorcerConstants.WEBSTER_ROOTS, getRepoDir());
		return websterRootStr.split(";");
	}

	/**
	 * Return the local host address based on the value of a system property.
	 * using {@link java.net.InetAddress#getByName(String)}. If the system
	 * property is not resolvable, return the default host address obtained from
	 * {@link java.net.InetAddress#getLocalHost()}
	 * 
	 * @param property
	 *            The property name to use
	 * 
	 * @return The local host address
	 * 
	 * @throws java.net.UnknownHostException
	 *             if no IP address for the host name could be found.
	 */
	public static String getHostAddressFromProperty(String property)
			throws java.net.UnknownHostException {
		String host = getHostAddress();
		String value = System.getProperty(property);
		if (value != null) {
			host = java.net.InetAddress.getByName(value).getHostAddress();
		}
		return (host);
	}

    /**
     * <p>
     * Return <code>true</code> is a SORCER {@link sorcer.provider.boot.Booter}
     * is used, otherwise <code>false</code>,
     * </p>
     *
     * @return the bootable
     */
    public static boolean isBootable() {
        return bootable;
    }

    /**
     * <p>
     * Assigns <code>true</code> by the {@link sorcer.boot.provider.Booter} when
     * used.
     * </p>
     *
     * @param bootable
     *            the bootable to set
     */
    public static void setBootable(boolean bootable) {
        SorcerEnv.bootable = bootable;
    }

	public static String getSorcerVersion() {
		return props.getProperty(S_VERSION_SORCER, SORCER_VERSION);
	}


    public static String getRioVersion() {
        return props.getProperty(S_VERSION_RIO, RIO_VERSION);
    }

    /***
     * Helper method adding root to each of the jars, and joining the result with spaces
     * @param root
     * @param jars
     * @return
     */
    public static String getCodebase(URL root, String[] jars) {
        Collection<String> cb = new ArrayList<String>(jars.length);
        for (String jar : jars) {
            cb.add(getCodebase(root, jar).toExternalForm());
        }
        return StringUtils.join(cb, SorcerConstants.CODEBASE_SEPARATOR);
    }

    public static URL getCodebase(URL root, String jar) {
        try {
            return new URL(root, jar);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static URL getCodebaseRoot() {
        try {
            // TODO: allow to override hostAddress from sorcer.env
            return getCodebaseRoot(getHostAddress(), sorcerEnv.getWebsterPortProperty());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Could not obtain local address", e);
        }
    }

    /**
     * Prepare codebase root URL - URL with only hostname and port
     * @param address
     * @param port
     * @return
     */
    public static URL getCodebaseRoot(String address, int port) {
        try {
            return new URL("http",address,port,"");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not prepare codebase root URL", e);
        }
    }


    protected SorcerEnv(){
        properties = props;
    }

    //experimental code - SorcerEnv as instantiable class

    private static SorcerEnv sorcerEnv;
    private Properties properties;

    public SorcerEnv(Properties props){
        properties = props;
    }

    public SorcerEnv(Map<String, String> props) {
        properties = new Properties();
        properties.putAll(props);
    }

    static {
        Properties myProperties = new ParentFirstProperties(System.getProperties());
        myProperties.putAll(props);
        sorcerEnv = new SorcerEnv(myProperties);
        sorcerEnv.overrideFromEnvironment(System.getenv());
    }

    private void overrideFromEnvironment(Map<String, String> env) {
        String portStr = env.get(E_WEBSTER_PORT);
        if (portStr != null && !portStr.isEmpty()) {
            setWebsterPortProperty(Integer.parseInt(portStr));
        }
        String sorcerHome = env.get(SorcerConstants.SORCER_HOME);
        if(sorcerHome != null)
            setSorcerHome(sorcerHome);
    }

    public static SorcerEnv getEnvironment(){
        return sorcerEnv;
    }

    public boolean isWebsterInternal() {
        return Boolean.parseBoolean(properties.getProperty(SORCER_WEBSTER_INTERNAL, "false"));
    }

    public void setWebsterInternal(boolean internal){
        properties.setProperty(SORCER_WEBSTER_INTERNAL, Boolean.toString(internal));
    }

    public String getRequestorWebsterCodebase(){
        return properties.getProperty(R_CODEBASE);
    }

    public void setRequestorWebsterCodebase(String codebase){
        properties.setProperty(R_CODEBASE, codebase);
    }

    public int getWebsterPortProperty() {
        return Integer.parseInt(properties.getProperty(P_WEBSTER_PORT, "0"));
    }

    public void setWebsterPortProperty(int port){
        properties.setProperty(P_WEBSTER_PORT, Integer.toString(port));
    }

    public String getWebsterRootsString(){
        return properties.getProperty(SorcerConstants.WEBSTER_ROOTS);
    }

    public void setWebsterRootsString(String roots){
        properties.setProperty(SorcerConstants.WEBSTER_ROOTS, roots);
    }

    public String getSorcerHome() {
        return properties.getProperty(SorcerConstants.SORCER_HOME);
    }

    public void setSorcerHome(String sorcerHome) {
        properties.setProperty(SorcerConstants.SORCER_HOME, sorcerHome);
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
     * Return true if a modified name is used.
     *
     * @return true if name is suffixed
     */
    public static boolean nameSuffixed() {
        return props.getProperty(S_IS_NAME_SUFFIXED, "false").equals("true");
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
}
