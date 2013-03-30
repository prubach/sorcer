/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

package sorcer.core.requestor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.service.ConfigurationException;
import sorcer.tools.webster.InternalWebster;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

/**
 * This class defines the JUnit requestor's scaffolding. The init method of the
 * class initializes system properties, then if requested an internal Webster is
 * started. If one is running it attempts to get the webster roots paths.
 * 
 * @author M. W. Sobolewski
 */
 public class SorcerTester implements SorcerConstants {
	/** Logger for logging information about this instance */
	protected static final Logger logger = Logger
			.getLogger(SorcerTester.class.getName());

	public static String R_PROPERTIES_FILENAME = "requestor.properties";
	protected static SorcerTester tester = null;
	protected Properties props;
	protected int port;
	
	/**
	 * init method for the SorcerTester class
	 * @param args String array containing arguments for the init method
	 * @throws Exception
	 */
	public void init(String... args) throws Exception {		
		// Initialize system properties: configs/sorcer.env
		Sorcer.getEnvProperties();
		// Attempt to load the tester properties file
		String str = System.getProperty(R_PROPERTIES_FILENAME);
		logger.info(R_PROPERTIES_FILENAME + " = " + str);
		if (str != null) {
			loadProperties(str); // search the provider package
  		} else {
			throw new RuntimeException("No tester properties file available!");
		}
		// Determine if an internal web server is running if so obtain the root paths
		boolean isWebsterInt = false;
		String val = System.getProperty(SORCER_WEBSTER_INTERNAL);
		if (val != null && val.length() != 0) {
			isWebsterInt = val.equals("true");
		}
		if (isWebsterInt) {
			String roots = System.getProperty(SorcerConstants.WEBSTER_ROOTS);
			String[] tokens = null;
			if (roots != null)
				tokens = toArray(roots);
			try {
				InternalWebster.startWebster(tokens);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Loads tester properties from a <code>filename</code> file. 
	 * 
	 * @param filename
	 *            the properties file name see #getProperty
	 * @throws ConfigurationException 
	 */
	public void loadProperties(String filename) throws ConfigurationException {
		logger.info("loading requestor properties:" + filename);
		tester.props = Sorcer.loadProperties(filename);
	}

	public String getProperty(String key) {
		return tester.props.getProperty(key);
	}

	public Object setProperty(String key, String value) {
		return tester.props.setProperty(key, value);
	}
	
	public String getProperty(String property, String defaultValue) {
		return tester.props.getProperty(property, defaultValue);
	}
	
	/**
	 * Returns a URL for the tester's data server.
	 * 
	 * @return the current URL for the requestor's data server.
	 */
	public String getDataServerUrl() {
		return "http://" + getProperty(DATA_SERVER_INTERFACE) + ':' + getProperty(DATA_SERVER_PORT);
	}

	/**
	 * Returns the hostname of a requestor data server.
	 * 
	 * @return a data server name.
	 */
	public String getDataServerInterface() {
		return  System.getProperty(R_DATA_SERVER_INTERFACE);
		}
	

	/**
	 * Returns the port of a tester data server.
	 * 
	 * @return a data server port.
	 */
	public String getDataServerPort() {
		return  System.getProperty(R_DATA_SERVER_PORT);
		}
	
	/**
	 * Returns a URL for the SORCER class server.
	 * 
	 * @return the current URL for the SORCER class server.
	 */
	public String getWebsterUrl() {
		return "http://" + getWebsterInterface() + ':' + getWebsterPort();
	}

	/**
	 * Returns the hostname of a tester class server.
	 * 
	 * @return a webster host name.
	 */
	public String getWebsterInterface() {
		String hn = System.getenv("IGRID_WEBSTER_INTERFACE");

		if (hn != null && hn.length() > 0) {
			logger.finer("webster hostname as the system environment value: "
					+ hn);
			return hn;
		}

		hn = System.getProperty(R_WEBSTER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			logger
					.finer("webster hostname as '" + R_WEBSTER_INTERFACE + "' system property value: "
							+ hn);
			return hn;
		}

		hn = tester.props.getProperty(R_WEBSTER_INTERFACE);
		if (hn != null && hn.length() > 0) {
			logger
					.finer("webster hostname as '" + R_WEBSTER_INTERFACE + "' provider property value: "
							+ hn);
			return hn;
		}

		try {
			hn = Sorcer.getHostName();
			logger.finer("webster hostname as the local host value: " + hn);
		} catch (UnknownHostException e) {
			logger.severe("Cannot determine the webster hostname.");
		}

		return hn;
	}
	
	/**
	 * Checks which port to use for a tester class server.
	 * 
	 * @return a port number
	 */
	public int getWebsterPort() {
		if (port != 0)
			return port;

		String wp = System.getenv("IGRID_WEBSTER_PORT");
		if (wp != null && wp.length() > 0) {
			logger.finer("requestor webster port as 'IGRID_WEBSTER_PORT': " + wp);
			return new Integer(wp);
		}

		wp = System.getProperty(R_WEBSTER_PORT);
		if (wp != null && wp.length() > 0) {
			logger.finer("requestor webster port as System '" + R_WEBSTER_PORT + "': "
					+ wp);
			return new Integer(wp);
		}

		wp = tester.props.getProperty(R_WEBSTER_PORT);
		if (wp != null && wp.length() > 0) {
			logger.finer("requestor webster port as Sorcer '" + R_WEBSTER_PORT + "': "
					+ wp);
			return new Integer(wp);
		}

		try {
			port = Sorcer.getAnonymousPort();
			logger.finer("anonymous requestor webster port: " + wp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return port;
	}

	/**
	 * Returns the URL for the tester's <code>filename</code>
	 * 
	 * @return the current URL for the SORCER tester data server.
	 * @throws MalformedURLException 
	 */
	public URL getRequestorDataFileURL(String filename) throws MalformedURLException {
		return new URL("http://" + getDataServerUrl() + '/'
				+ getProperty(R_DATA_DIR) + '/' + filename);
	}

	public File getScrachFile(String filename) {
		return new File(getNewScratchDir() + File.separator + filename);
	}

	/**
	 * Returns a directory for requestor's scratch files
	 * 
	 * @return a scratch directory
	 */
	 public File getScratchDir() {
		 return Sorcer.getNewScratchDir();
	}
	
	/**
	 * Deletes a directory and all its files.
	 * 
	 * @param dir
	 *            to be deleted
	 * @return true if the directory is deleted
	 * @throws Exception
	 */
	public boolean deleteDir(File dir) throws Exception {
		return SorcerUtil.deleteDir(dir);
	}
		
	/**
	 * Returns a directory for requestor's scratch files
	 * 
	 * @return a scratch directory
	 */
	public File getNewScratchDir() {
		return Sorcer.getNewScratchDir();
	}

	public File getDataFile(String filename) {
		return new File(getDataDir() + File.separator + filename);
	}
	
	/**
	 * Returns a directory for requestor's data root.
	 * 
	 * @return a tester data root directory
	 */
	public File getDataRootDir() {
		return new File(getProperty(R_DATA_ROOT_DIR));
	}
	
	/**
	 * Returns a directory for requestor's data.
	 * 
	 * @return a tester data directory
	 */
	public File getDataDir() {
		//return new File(getProperty(R_DATA_ROOT_DIR) + File.separator + getProperty(R_DATA_DIR));
		return new File(System.getProperty(DOC_ROOT_DIR));
	}

	/**
	 * Returns the URL for a specified data file.
	 * 
	 * @param dataFile
	 *            a file
	 * @return a URL
	 * @throws MalformedURLException
	 */
	public String getDataFileUrl(File dataFile) throws MalformedURLException {
		String dataURL = getDataServerUrl();
		String path = dataFile.getAbsolutePath();
		int index = path.indexOf(Sorcer.getProperty(R_DATA_DIR));
		return dataURL + File.separator + path.substring(index);
	}

	/**
	 * Returns the requestor's scratch directory
	 * 
	 * @return a scratch directory
	 */
	 public File getUserHomeDir() {
		return new File(System.getProperty("user.home"));
	}
	
	/**
	 * Returns the requestor's scratch directory.
	 * 
	 * @return a scratch directory
	 */
	 public File getSorcerHomeDir() {
		return new File(System.getenv("IGRID_HOME"));
	}
	
	 public Properties getProperties() {
		 return tester.props;
	 }
	 
	/**
	 * Returns the URL of a scratch file at the tester HTTP data server.
	 * 
	 * @param scratchFile
	 * @return the URL of a scratch file
	 * @throws MalformedURLException
	 */
	public URL getScratchURL(File scratchFile)
			throws MalformedURLException {
		return Sorcer.getScratchURL(scratchFile);
	}

	/**
	 * Returns the URL of a dataFile at the tester HTTP data server.
	 * 
	 * @param dataFile
	 * @return the URL of a data file
	 * @throws MalformedURLException
	 */
	public static URL getDataURL(File dataFile)
			throws MalformedURLException {
		return Sorcer.getDataURL(dataFile);
	}
	
	protected static String[] toArray(String arg) {
		StringTokenizer token = new StringTokenizer(arg, " ,;");
		String[] array = new String[token.countTokens()];
		int i = 0;
		while (token.hasMoreTokens()) {
			array[i] = token.nextToken();
			i++;
		}
		return (array);
	}
		
}
