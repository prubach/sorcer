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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
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

    static {
        Properties myProperties = new ParentFirstProperties(System.getProperties());
        myProperties.putAll(props);
        sorcerEnv = new SorcerEnv(myProperties);
        sorcerEnv.overrideFromEnvironment();
    }

    private void overrideFromEnvironment() {
        String portStr = System.getenv("SORCER_WEBSTER_PORT");
        if (portStr != null && !portStr.isEmpty()) {
            setWebsterPortProperty(Integer.parseInt(portStr));
        }
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
}
