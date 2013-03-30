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

package sorcer.util.dbas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.acl.NotOwnerException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.util.DataReader;
import sorcer.util.SaveOutput;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

public class ApplicationDomain implements SorcerConstants {
	private static Logger logger = Logger.getLogger(ApplicationDomain.class
			.getName());
	// password for administration interface
	static protected String password = ADMIN;
	public static final int DEFAULT_PORT = 6001;
	// appName is to denote the application, but appPrefix is intended to
	// identify dynamically loaded extension classes. If appPrefix is not set
	// then appPrefix == appName
	public static String appName, fullAppName, appPrefix, asDir = ".",
			uploadDir = ".", secureUploadDir = ".";
	public static Vector uploadDirs;
	public static ASAclManager aclm;
	// Key GroupName Value -> Vector containing Logins
	public static Hashtable groups;
	// Key Role Name -> Vector containing Permission|Object_Type|sign
	public static Hashtable roles;

	// public static String initialContextFactory,jndiProvider,dataSourceName;
	// public static Hashtable permissions;
	public static Properties props, headers;
	public static Hashtable queries;
	protected static String logFile = "as.log", propsFile = "as.def",
			sqlFile = "as.sql";
	static BufferedWriter out;
	public static boolean isMultiProject = false, isClassAccess = false,
			isApproval = false, isReview = false, isSubscribe = false;

	// key object type value Hashtable { key +/-permission value id}
	public static Hashtable permissions;

	// basic initialization
	public static void init(String[] args) {
		if (args != null)
			processArgs(args);
		loadProperties();
		ProtocolConnection.initialize();
		initQueryTable();
		initialize();
	}

	private static void initialize() {
		// Create access control manager
		createAclManager();
		Properties sysProps = System.getProperties();
		logOperation("started", "ApplicationDomain.class", appName, sysProps
				.getProperty("user.name")
				+ ", java version: " + sysProps.getProperty("java.version"));
	}

	public static JdbcConnectionPool getPool() {
		return JdbcConnectionPool.getInstance();
	}

	protected static void createAclManager() {
		// Create access control manager
		try {
			String aclMangerName;
			int i = appPrefix.indexOf('.');
			if (i > 0)
				aclMangerName = appPrefix.substring(i + 1) + "ASAclManager";
			else
				aclMangerName = appPrefix + "ASAclManager";

			if (isOn("aclManager.isExtended")) {
				try {
					aclm = (ASAclManager) Class.forName(
							appPrefix.toLowerCase() + ".dbas." + aclMangerName)
							.newInstance();
				} catch (Exception e) {
					System.err.println("Failed to create: "
							+ appPrefix.toLowerCase() + ".dbas."
							+ aclMangerName);
					e.printStackTrace();
					return;
				}
			} else
				aclm = new ASAclManager();
		} catch (NotOwnerException e) {
			System.err.println("Exception creating application ACL manager");
		}
	}

	protected void createNotifier() {
		// Create notification manager
		Notifier notifier;
		String notifierName;
		int i = appPrefix.indexOf('.');
		if (i > 0)
			notifierName = appPrefix.substring(i + 1) + "Notifier";
		else
			notifierName = appPrefix + "Notifier";

		if (isNotifierEnabled()) {
			try {
				notifier = (Notifier) Class.forName(
						appPrefix.toLowerCase() + ".dbas." + notifierName)
						.newInstance();
				notifier.initialize(this);
			} catch (Exception e) {
				System.err.println("Failed to create: "
						+ appPrefix.toLowerCase() + ".dbas." + notifierName);
				e.printStackTrace();
				return;
			}
		} else
			System.err.println("Not able to create application notifier");
	}

	public static String getPermissionID(String ObjectType,
			String permissionName, boolean isPossitive) {
		// First see if permission exists in "all" object type.
		if (permissions == null)
			return null;
		Hashtable permissionIDMap = (Hashtable) permissions.get(CALL);
		if (permissionIDMap.get((isPossitive ? "+" : "-") + permissionName) == null)
			permissionIDMap = (Hashtable) permissions.get(ObjectType);

		return (String) permissionIDMap.get((isPossitive ? "+" : "-")
				+ permissionName);
	}

	public static void usage() {
		System.err
				.println("usage: java jgapp.dbas.ApplicationDomain [-port <num>] "
						+ "[-debug] {-debugKey <key>] [-assert] [-dir <server properties dir>] "
						+ "[-props <filename>] [-admin <passwd>]");
		System.err.println("\t debug          = OFF");
		System.err.println("\t debugKey       = system.debug");
		System.err.println("\t assert         = OFF");
		System.err.println("\t properties dir = .");
		System.err.println("\t properties filename = as.def");
		System.err.println("\t admin          = " + ADMIN);
		System.exit(-1);
	}

	private static void processArgs(String[] argv) {
		// process command line
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].equals("help")) {
				usage();
			}
			if (argv[i].equals("-dir")) {
				asDir = argv[i + 1];
				i++;
			}
			if (argv[i].equals("-props")) {
				propsFile = argv[i + 1];
				i++;
			}
			if (argv[i].equals("-admin")) {
				password = argv[i + 1];
				i++;
			}
			if (argv[i].equals("-port")) {
				if (props != null)
					props.put("applicationServer.port", argv[i + 1]);
				i++;
			}
		}
	}

	private static void loadProperties() {
		String str, val;
		String[] tokens;
		props = new Properties();
		headers = new Properties();
		File file;

		FileInputStream fin;
		try {
			logger.info("loadProperties:asDir = " + asDir + ", propsFile = "
					+ propsFile);
			fin = new FileInputStream(asDir + File.separator + propsFile);
			props.load(fin);
			fin.close();

			file = new File(asDir + "/as.headers");
			if (file.exists()) {
				fin = new FileInputStream(file);
				headers.load(fin);
				fin.close();
			}

			if (isOutputLoged()) {
				String otputLog = props.getProperty(
						"applicationServer.outputLog", appName + ".txt");
				otputLog = ApplicationDomain.asDir + File.separator + otputLog;
				System.out.println("otputLog=" + otputLog);
				SaveOutput.start(otputLog);
			}

			if (isLogged()) {
				logFile = props.getProperty("applicationServer.logFile",
						logFile);
				if (!logFile.equalsIgnoreCase("System.out"))
					out = new BufferedWriter(new FileWriter(
							ApplicationDomain.asDir + File.separator + logFile,
							true));
			}

			// get file upload directories
			int index;
			Enumeration e = props.keys();
			while (e.hasMoreElements()) {
				str = (String) e.nextElement();
				if (str.startsWith("dir")) {
					if (uploadDirs == null)
						uploadDirs = new Vector();
					uploadDirs.addElement((String) props.getProperty(str));
					System.out.println("upload dir:" + props.getProperty(str));
				}
			}
		} catch (java.io.IOException e) {
			System.out
					.println("Exception reading application properties: " + e);
			System.exit(1);
		}

		// Initialize the jndi properties for datasources
		// initialContextFactory =
		// props.getProperty("provider.initialContext.factory");
		// jndiProvider = props.getProperty("provider.JNDIProvider.url");
		// dataSourceName = props.getProperty("provider.dataSource.name");

		// Initialize the server properties
		str = props.getProperty("applicationServer.isDebugged", "false");
		str = props.getProperty("applicationServer.debugKey", "system.debug");

		str = props.getProperty("applicationServer.isAsserted", "false");

		// if (Util.isDebugged) {
		// //Display the properties
		// System.out.println("ApplicationDomain properties: ");
		// Enumeration enu = props.propertyNames();
		// while (enu.hasMoreElements()) {
		// String propKey = (String)enu.nextElement();
		// System.out.println(propKey + ": " +
		// props.getProperty(propKey));
		// }
		//          
		// System.out.println("AplicationServer:Header properties:");
		// enu = headers.propertyNames();
		// while (enu.hasMoreElements()) {
		// String propKey = (String)enu.nextElement();
		// System.out.println(propKey + ": " + headers.getProperty(propKey));
		// }
		// }

		// Set upload accdirectory, used by DocumentCmd
		String clientPropsFile = props
				.getProperty("applicationServer.clientProperties");
		// System.out.println("+++++++++++++++++++++++clientPropsFile "+
		// clientPropsFile);
		if (clientPropsFile == null)
			System.err.println("Can not find client properties");
		else {
			Properties clientProps = new Properties();
			try {
				fin = new FileInputStream(clientPropsFile);
				clientProps.load(fin);
				fin.close();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			ApplicationDomain.appName = clientProps.getProperty(
					"launcher.appName", "GApp");
			System.out.println("VVVVVVVVVVVVV AKKKI  appNAme"
					+ ApplicationDomain.appName);
			ApplicationDomain.fullAppName = clientProps.getProperty(
					"launcher.fullAppName", "Generic Application");
			System.out.println("VVVVVVVVVVVVV AKKKI fullAppName"
					+ ApplicationDomain.fullAppName);

			isClassAccess = "true".equals(clientProps.getProperty(
					"launcher.classAccess", "false"));
			props.put("applicationServer.useSSO", clientProps.getProperty(
					"launcher.useSSO", "false"));

			str = clientProps.getProperty("applicationServlet.url");
			if (str != null)
				props.put("applicationServlet.url", str);

			props.put("applicationServer.SSOlogin.url", clientProps
					.getProperty("launcher.SSOlogin.url", "false"));

			isMultiProject = clientProps.getProperty("launcher.multiProject",
					"false").equals("true");

			isApproval = clientProps.getProperty("launcher.approval", "false")
					.equals("true");

			isReview = clientProps.getProperty("launcher.review", "false")
					.equals("true");

			isSubscribe = clientProps
					.getProperty("launcher.subscribe", "false").equals("true");

			// if appPrefix not defined use it as appName
			str = clientProps.getProperty("launcher.appPrefix",
					ApplicationDomain.appName);

			ApplicationDomain.appPrefix = str;

			// set database prefix
			Sorcer.setDBPrefix(clientProps.getProperty("launcher.dbPrefix",
					appName));

			// set Forest path separator
			Sorcer.setSepChar(clientProps.getProperty("launcher.sepChar", "."));

			str = clientProps.getProperty("applicationServer.port", Integer
					.toString(DEFAULT_PORT));
			props.put("applicationServer.port", str);

			str = clientProps.getProperty("launcher.isExtra", "false");
			props.put("isExtra", str);

			str = clientProps.getProperty("http.host");
			if (str != null)
				props.put("http.host", str);

			str = clientProps.getProperty("http.port", "80");
			props.put("http.port", str);

			str = clientProps.getProperty("launcher.applicationServlet.url");
			if (str != null)
				props.put("applicationServlet.url", str);

			str = clientProps
					.getProperty("launcher.applicationServlet.urlbase");
			if (str != null)
				props.put("applicationServlet.urlbase", str);

			str = clientProps.getProperty("launcher.archiveDir", "upload");
			props.put("launcher.archiveDir", str);

			str = clientProps.getProperty("launcher.accessClass", "false");
			props.put("accessClass", str);

			str = clientProps.getProperty("launcher.superApp");
			if (str != null)
				props.put("launcher.superApp", str);

			str = clientProps.getProperty("launcher.baseDir");
			if (!(str.charAt(str.length() - 1) == File.separatorChar))
				str = str + File.separatorChar;

			String upload = str
					+ clientProps.getProperty("launcher.archiveDir");
			ApplicationDomain.uploadDir = upload;

			upload = str + clientProps.getProperty("launcher.secureArchiveDir");
			ApplicationDomain.secureUploadDir = upload;

			logger.info("Application appName=" + appName);
			logger.info("Application prefix=" + appPrefix);
			logger.info("dbPrefix=" + Sorcer.getDBPrefix());
			logger.info("uploadDir: " + uploadDir);
			logger.info("secureUploadDir: " + secureUploadDir);

		}
	}

	static private void initQueryTable(Connection con) {
		if (isQueryPrepared()) {
			try {
				Enumeration e = queries.keys();
				while (e.hasMoreElements()) {
					String s = (String) e.nextElement();
					queries.put(s, con
							.prepareStatement((String) queries.get(s)));
					queries = new Hashtable();
				}
			} catch (SQLException e) {
				System.out.println("JDBC connection exception " + e);
			}
		}
	}

	static public void initQueryTable() {
		File aFile = new File(asDir + File.separator + sqlFile);
		if (aFile.exists()) {
			DataReader reader = new DataReader(aFile);
			queries = reader.getSQLQueries();
		}
	}

	public static void log(String message) {
		if (out != null)
			synchronized (out) {
				try {
					String date = "[" + new java.util.Date() + "] ";
					out.write(date);
					out.write(message);
					out.newLine();
					out.flush();
				} catch (java.io.IOException e) {
					System.err.println("Error writing the file " + logFile);
				}
			}
	}

	public static void logOperation(String operation, String objType,
			String objName, String userName, String contextName,
			String contextType) {
		StringBuffer description = new StringBuffer();

		description.append(operation);
		description.append(" by ");
		description.append(userName);
		description.append(": ");
		description.append(objType);
		description.append(" (");
		description.append(objName);

		if (contextName != null) {
			description.append(") from: ");
			description.append(contextType);
			description.append(" (");
			description.append(contextName);
		}
		description.append(")");

		log(description.toString());
	}

	public static void logOperation(String operation, String objType,
			String objName, String userName) {
		ApplicationDomain.logOperation(operation, objType, objName, userName,
				null, null);
	}

	public static void logSQLOperation(String query, String userName) {
		String operation = SorcerUtil.firstToken(query, " ");
		if (!operation.trim().equalsIgnoreCase("select"))
			ApplicationDomain.logOperation(operation, "SQL query", query,
					userName, null, null);
	}

	protected void finalize() throws Throwable {
		try {
			// close buffered writer for the log file
			if (out != null)
				out.close();
		} catch (java.io.IOException ex) {
			System.out.println("Error closing the file " + logFile);
		}
		super.finalize();
	}

	public static Properties getProperties() {
		return props;
	}

	public static boolean isMonitored() {
		// Determine if user connections are monitored
		return props.getProperty("applicationServer.isMonitored", "false")
				.equalsIgnoreCase("true");
	}

	public void isMonitored(boolean val) {
		// Determine if user connections are monitored
		props.put("applicationServer.isMonitored", (new Boolean(val))
				.toString());
	}

	public String appName() {
		return appName;
	}

	public String passwd() {
		return password;
	}

	public static boolean isOn(String property) {
		return props.getProperty(property, "false").equals("true");
	}

	public static String getProperty(String property) {
		return props.getProperty(property);
	}

	public static String getProperty(String property, String defaultVal) {
		return props.getProperty(property, defaultVal);
	}

	public static boolean useSSO() {
		return isOn("applicationServer.useSSO");
	}

	public static boolean isLogged() {
		return isOn("applicationServer.isLogged");
	}

	public static boolean isOutputLoged() {
		return isOn("applicationServer.isOutputLogged");
	}

	public static boolean isOracle() {
		return props.getProperty("applicationServer.isOracle", "true").equals(
				"true");
	}

	public static String defaultDbRole() {
		System.out
				.println("jgapp.dbas.ApplicationDomain::defaultRole() protocolConnection.defaultDbRole:"
						+ props.getProperty("protocolConnection.defaultDbRole"));
		return props.getProperty("protocolConnection.defaultDbRole");
	}

	public static String defaultPool() {
		return props.getProperty("protocolConnection.defaultDbPool");
	}

	// if true use GE proprietary access classes 1-4
	public static boolean isAccessClass() {
		return isOn("accessClass");
	}

	// public static boolean isJdbcPoolEnabled() {
	// return isOn("protocolConnection.isJdbcPoolEnabled");
	// }

	public static boolean isNotifierEnabled() {
		return isOn("notifier.isEnabled");
	}

	public static boolean isNotifierMonitored() {
		return isOn("notifier.isMonitored");
	}

	public static boolean isQueryPrepared() {
		return queries.size() > 0;
	}
}
