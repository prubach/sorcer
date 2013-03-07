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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.ProtocolStream;
import sorcer.util.ServletProtocolStream;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

// This class is the thread that handles all communication with a client
// It also notifies the ConnectionWatcher when the connection is dropped.
public class ProtocolConnection extends Thread implements SorcerConstants {
	private static Logger logger = Logger.getLogger(ProtocolConnection.class
			.getName());
	static JdbcConnectionPool connectionPool;
	// dbRoles correspond to system Roles. Used for verifications for DB SQL
	// Queries.
	// Key->roleName Value->String[]{poolName, dbPermissions}
	public static Hashtable dbRoles = new Hashtable();

	// GAppPrincipal using this protocolConnection currently
	SorcerPrincipal principal;
	protected ProtocolStream stream;

	static final int ACLGUARDED = 1;
	static final int ACLPERMISSIONGRANTED = 2;

	// Initialize the streams and start the thread
	public ProtocolConnection() {
		// setConnectionProperties();
		stream = null;
		// As this is created this way on behalf of system,
		principal = new SorcerPrincipal();
		principal.setName(SERVLET);
		principal.setRole(SYSTEM);
		// principal.setAppRole(Const.SYSTEM);
	}

	public ProtocolConnection(ThreadGroup group, String name) {
		super(group, name);
		// setConnectionProperties();
		principal = new SorcerPrincipal();
		principal.setName(SERVLET);
		principal.setRole(SYSTEM);
		// principal.setAppRole(Const.SYSTEM);
		stream = null;
	}

	public static void initialize() {
		loadProperties();
		JdbcConnectionPool.initialize();
		connectionPool = JdbcConnectionPool.getInstance();
	}

	public static void loadProperties() {
		Properties props = ApplicationDomain.props;
		String key = null, value = null;
		String[] tokens = null;
		for (Enumeration e = props.keys(); e.hasMoreElements();) {
			key = (String) e.nextElement();
			if (key.startsWith("dbrole")) {
				value = props.getProperty(key);
				tokens = SorcerUtil.tokenize(value, SEP);
				// token 0->roleName, 1->poolName, 2->permissions
				// key -> roleName Value->String[]{poolName, Permissions}
				dbRoles.put(tokens[0], new String[] { tokens[1], tokens[2] });
			}
		}
	}

	/**
	 *Returns poolName for a particular dbRole
	 **/
	public static String getPoolName(String dbRole) {
		if (dbRoles == null || dbRoles.isEmpty() || dbRole == null
				|| dbRoles.get(dbRole) == null)
			dbRole = ApplicationDomain.defaultDbRole();
		logger.info("jgapp.dbas.ProtocolConnection::getPoolName() dbRole:"
				+ dbRole);
		logger.info("PoolName : " + ((String[]) dbRoles.get(dbRole))[0]);
		return ((String[]) dbRoles.get(dbRole))[0];
	}

	public static String getDbPermissions(String dbRole) {
		return (dbRoles == null || dbRoles.isEmpty()) ? null
				: ((String[]) dbRoles.get(dbRole))[1];
	}

	// Provide the service for all Protocol class commands
	public void run() {
		int cmd;
		try {
			// Loop forever, or until the connection is broken!
			while (true) {
				processCmd();
				stream.flush();
				// Yield to the other threads and drop into
				// my blocking readInt()
				yield();
			} // end while
		} catch (java.io.IOException e) {
			System.err
					.println("ProtocolConnection: error stream communication");
			e.printStackTrace();
		}
		// When we're done, for whatever reason, be sure to close
		// the socket, and to notify the ConnectionWatcher object. Note that
		// we have to use synchronized first to lock the watcher
		// object before we can call notify() for it.
		finally {
			try {
				stream.close();
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}// end run

	protected void processCmd() throws IOException {
		try {
			// read in a Protocol command
			int cmd = stream.readInt();
			logger.info("processCmd():cmd=" + cmd);

			switch (cmd) {
			case LOGIN: {
				login();
				break;
			}
			case IS_ALIVE: {
				stream.writeLine(stream.readLine());
				stream.done();
				break;
			}
			case AUTHORIZE:
				authorize();
				break;
			case ACL_ISAUTHORIZED:
				isAuthorized(cmd);
				break;
			case CHANGEPASSWD:
				changePassword();
				break;
			case LOG:
				log();
				break;
			// case CONNECT: //connect to database
			// connect();
			// break;
			// case DSCONNECT: //connect to a datasource
			// dsConnect();
			// break;
			// case SETPOOL:
			// setPoolName();
			// break;
			case EXECQUERY:
				// if (isDBConnectionOK())
				executeQuery(cmd);
				break;
			case EXECPQUERY:
				// if (isDBConnectionOK())
				executePageQuery(cmd);
				break;
			case EXECPREPQUERY:
				// if (isDBConnectionOK())
				executeQueryFor(cmd);
				break;
			case EXECUPDATE:
				// if (isDBConnectionOK())
				executeUpdate(cmd);
				break;
			case EXECPREPUPDATE:
				executeUpdateFor(cmd);
				break;
			case EXECPROVIDER:
				// if (isDBConnectionOK())
				executeProviderCmd(cmd);
				break;
			case EXECCMD:
				// if (isDBConnectionOK())
				executeCmd(cmd);
				break;
			case EXECDEFAULT:
				// if (isDBConnectionOK())
				executeDefaultCmd(cmd);
				break;
			default:
				stream.writeLine("ERROR:Invalid server request: " + cmd);
				stream.done();
			}
		} catch (Exception e) {
			e.printStackTrace();
			stream.writeLine("ERROR:Exception: " + e.getMessage());
			stream.done();
		}

	}

	protected void processCmd(int cmd, Object[] args) throws IOException {
		try {
			if (cmd > OBJECT_CMD_START) {
				executeDefaultCmd(cmd, args);
				return;
			} else {
				switch (cmd) {
				case LOGIN:
					login((String) args[0]);
					break;
				case AUTHORIZE:
					authorize((String) args[0]);
					break;
				case ACL_ISAUTHORIZED:
					isAuthorized(((Integer) args[0]).intValue(),
							(String) args[0]);
					break;
				// case CHANGEPASSWD:
				// changePassword((String)args[0]);
				// break;
				case LOG:
					log((String) args[0]);
					break;
				case EXECQUERY:
					// if (isDBConnectionOK())
					executeQuery(cmd);
					break;
				case EXECUPDATE:
					// if (isDBConnectionOK())
					executeUpdate(cmd);
					break;
				case EXECDEFAULT:
					// if (isDBConnectionOK()) {
					int subcmd = ((Integer) args[0]).intValue();
					String[] data = new String[args.length - 1];
					for (int i = 0; i < data.length; i++)
						data[i] = (String) args[i + 1];
					executeDefaultCmd(subcmd, data);
					// }
					break;
				default:
					stream.writeLine("ERROR:Invalid server request: " + cmd);
					stream.done();
				}
			}

			stream.done();
		} catch (Exception e) {
			stream.writeLine("ERROR:ProtocolConnection>>Exception: "
					+ e.getMessage());
			stream.done();
		}

	}

	protected void login() throws IOException, SQLException {
		String args = stream.readLine().trim();
		login(args);
	}

	protected void login(String arg) throws IOException, SQLException {
		principal = new SorcerPrincipal();
		principal.setName(SorcerUtil.firstToken(arg, SEP));
		principal.setPassword(SorcerUtil.secondToken(arg, SEP));
		principal.setRole(ApplicationDomain.defaultDbRole());
		if (verifyUser()) {
			// debug(this, "Logged.....Principal="+principal.asString());
			stream.writeLine("logged" + SEP + principal.asString());
			stream.done();
			ApplicationDomain.logOperation("login", principal.getRole(),
					getDbPermissions(principal.getRole()), principal.getName());
		} else {
			stream.done();

			if (principal.getRole() == null)
				ApplicationDomain.logOperation("login failed", principal
						.getRole(), getDbPermissions(principal.getRole()),
						principal.getName());
		}
	}

	protected void log() throws IOException {
		log(stream.readLine());
	}

	protected void log(String message) throws IOException {
		ApplicationDomain.log(message);
		stream.writeLine("log");
		stream.done();
	}

	protected void authorize() throws IOException {
		authorize(stream.readLine());
	}

	protected void authorize(String arg) throws IOException {
		// get all args into array
		String[] args = SorcerUtil.tokenize(arg, SEP);
		boolean r = ApplicationDomain.aclm.isAuthorizedToAccessResource(
				args[0], args[1], args[2]);
		stream.writeLine(String.valueOf(r));
		stream.done();
	}

	protected void isAuthorized(int cmd) throws IOException {
		isAuthorized(cmd, stream.readLine());
	}

	protected void isAuthorized(int cmd, String arg) throws IOException {
		// get all args into array
		String[] args = SorcerUtil.tokenize(arg, SEP);

		if (!ApplicationDomain.aclm.isAclCached(args[1], args[2])) {
			ApplicationProtocolStatement aps = createAppProtocolStatement();
			// AclCmd checks for ACL and puts it in acls in AclManager
			Object outStr = aps.executeDefaultCmd(GET_ACL, args);
			aps.close();
		}
		// Util.debug(this,"ApplicationDomain.aclm="+
		// ApplicationDomain.aclm+args[0]+args[1]+args[2]+args[3]);
		boolean r = ApplicationDomain.aclm.isAuthorized(args[0], args[1],
				args[2], args[3]);

		logger.info("in isAuthorized() cmd = " + cmd + "arg =" + arg
				+ " result=" + r + " UserDBRole =" + principal.getRole());
		stream.writeLine(String.valueOf(r));
		stream.done();
	}

	protected void logged(String userLogin, String userPasswd, String userRole,
			String userID) {
		// implement in subclass if necessary.
	}

	// Used from server side by the commands.
	/*
	 * public int isAuthorized(String objectType,String objectId,String
	 * operation) { //1 acl Guarded //2 PERMISSIONGRANTED int aclStatusMask = 0;
	 * String[] args = {userLogin,objectType,objectId,operation}; if (
	 * !ApplicationDomain.aclm.isAclCached(objectType,objectId) ) {
	 * ApplicationProtocolStatement aps = createAppProtocolStatement(); //AclCmd
	 * checks for ACL and puts it in acls in AclManager Object outStr =
	 * aps.executeDefaultCmd(ACL_AUTHORIZE,args); aps.close(); }
	 * 
	 * if (ApplicationDomain.aclm.isAclCached(objectType,objectId)) {
	 * aclStatusMask = aclStatusMask | ACLGUARDED;
	 * 
	 * if (ApplicationDomain.aclm.isAuthorized(userLogin, objectType, objectId,
	 * operation)) aclStatusMask = aclStatusMask | ACLPERMISSIONGRANTED; }
	 * 
	 * if (userRole==Const.ADMIN || userRole==Const.ROOT) aclStatusMask =
	 * aclStatusMask | ACLPERMISSIONGRANTED;
	 * 
	 * return aclStatusMask; }
	 * 
	 * 
	 * public static boolean isAclGuarded(int mask) { return ((mask &
	 * ACLGUARDED)!=0); }
	 * 
	 * 
	 * public static boolean isAclPermissionGranted(int mask) { return ((mask &
	 * ACLPERMISSIONGRANTED)!=0); }
	 */

	protected void changePassword() throws IOException, SQLException {
		changePassword(stream.readLine().trim());
	}

	protected void changePassword(String arg) throws IOException, SQLException {
		if (setUserPasswd(arg)) {
			stream.writeLine("passwordChanged:" + principal.getName() + ':'
					+ arg);
			stream.done();
		} else {
			stream.done();
		}
	}

	protected boolean setUserPasswd(String userPasswd)
			throws java.sql.SQLException {
		return createAppProtocolStatement().setUserPassword(userPasswd);
	}

	// protected void connect() throws IOException {
	// connectToDatasource();
	// stream.writeLine("connected to datasource");
	// stream.done();
	// }

	// protected void setPoolName() throws IOException {
	// String inline;
	// inline = stream.readLine();
	// poolRole = inline.trim();
	// if (poolRole.equalsIgnoreCase("null") ||
	// poolRole.equalsIgnoreCase(Const.CLEAR ))
	// poolRole = null;
	// stream.writeLine("set pool name: " + poolRole);
	// stream.done();
	// }

	// protected void dsConnect() throws IOException {
	// String inline;
	// inline = stream.readLine();
	// inline = inline.trim();
	// String[] items = Util.tokenize(inline, delim);
	// if (connectToDatasource())
	// stream.writeLine("connected to datasource");
	// else {
	// System.err.println("ERROR:connecting to datasource URL: " + items[1]);
	// stream.writeLine("ERROR:connecting to datasource URL " + items[1]);
	// }
	// stream.done();
	// }

	boolean isAuthorizedToSubmitQuery(String query) {
		String operation = SorcerUtil.firstToken(query, " ");
		logger.info("isAuthorizedToSubmitQuery:query: " + query + "principal="
				+ principal);
		boolean access = ApplicationDomain.aclm.isAuthorizedToExecuteSQL(
				operation.toLowerCase(), principal.getRole());

		// ApplicationProtocolStatement aps = createAppProtocolStatement();
		// String[] args = { query };
		// Object outStr = aps.executeDefaultCmd(ACL_ISQUERYAUTHORIZED, args);

		return access;
	}

	boolean isAuthorizedToAccessResource(String resourceName, String operation,
			String principal) {
		boolean access = ApplicationDomain.aclm.isAuthorizedToAccessResource(
				resourceName, operation.toLowerCase(), principal);
		// debug(this, "operation: " + operation + ", resourceName: " +
		// resourceName + ", userRole: " + userRole + ", access: " + access);
		return access;
	}

	boolean verifyAccess(String str) {
		char firstChar = str.charAt(0);
		String ups = getDbPermissions(principal.getRole());
		if (ups.indexOf(firstChar) >= 0)
			return true;
		else
			return false;
	}

	boolean verifyUser() throws SQLException {
		ApplicationProtocolStatement aps = createAppProtocolStatement();
		// debug(this, "verifyUser=" + principal.asString());
		boolean b = aps.validateUserLogin();
		aps.close();
		return b;
	}

	protected void executeQuery(int cmd) throws IOException {
		// Stopwatch stopwatch = new Stopwatch();
		String inline;
		inline = stream.readLine();
		// debug(this, "executeQuery:" + inline);

		if (isAuthorizedToSubmitQuery(inline)) {
			ProtocolStatement ps = new ProtocolStatement(this, stream);
			ps.executeQuery(inline);
			ApplicationDomain.logSQLOperation(inline, principal.getName());
		} else {
			// debug(this, "executeQuery:accessError=" + userLogin);
		}
		stream.done();
		// System.out.println("time:" + stopwatch.get());
	}

	protected void executePageQuery(int cmd) throws IOException {
		// Stopwatch stopwatch = new Stopwatch();
		String inline;
		inline = stream.readLine();
		// debug(this, "executeQuery:" + inline);
		int page = stream.readInt();
		int pageSize = stream.readInt();

		if (isAuthorizedToSubmitQuery(inline)) {
			ProtocolStatement ps = new ProtocolStatement(this, stream, page,
					pageSize);
			ps.executePageQuery(inline);
			ApplicationDomain.logSQLOperation(inline, principal.getName());
		} 

		stream.done();
		// System.out.println("time:" + stopwatch.get());
	}

	protected void executeQueryFor(int cmd) throws IOException {
		String inline;
		inline = stream.readLine();

		if (!verifyAccess(inline)) {
			stream.done();
			return;
		}

		// debug(this, "executeQueryFor:" + inline);

		ProtocolStatement ps = new ProtocolStatement(this, stream);
		ps.executeQueryFor(inline);

		stream.done();
	}

	protected void executeUpdate(int arg) throws IOException {
		String inline, outStr;
		inline = stream.readLine();

		// debug(this, "executeUpdate:" + inline);

		inline = inline.trim();
		ProtocolStatement ps = new ProtocolStatement(this, stream);
		outStr = ps.executeUpdate(inline);

		// debug(this, "executeUpdate:" + outStr);

		stream.writeLine(outStr);
		stream.done();
	}

	protected void executeUpdateFor(int cmd) throws IOException {
		String inline, outStr;
		inline = stream.readLine();
		if (!verifyAccess(inline)) {
			stream.done();
			return;
		}

		// debug(this, "executeUpdateFor:" + inline);

		inline = inline.trim();
		ProtocolStatement ps = new ProtocolStatement(this);
		outStr = ps.executeUpdateFor(inline);

		// debug(this, "executeUpdateFor:" + outStr);

		stream.writeLine(outStr);
		stream.done();
	}

	private ApplicationProtocolStatement createAppProtocolStatement() {
		String appProtocolStatement;
		ApplicationProtocolStatement aps = null;
		String prefix = ApplicationDomain.appPrefix;
		int i = prefix.indexOf('.');
		if (i > 0)
			appProtocolStatement = prefix.substring(i + 1)
					+ "ProtocolStatement";
		else
			appProtocolStatement = prefix + "ProtocolStatement";

		try {
			aps = (ApplicationProtocolStatement) Class.forName(
					prefix.substring(0, i).toLowerCase() + ".dbas."
							+ appProtocolStatement).newInstance();
			aps.initialize(this, stream);
		} catch (Exception e) {
			// (new OSEnvironment()).print();
			e.printStackTrace();
			System.err.println("Failed to create1: " + prefix.toLowerCase()
					+ ".dbas." + appProtocolStatement);
			try {
				aps = aps = (ApplicationProtocolStatement) Class.forName(
						"jgapp.dbas.GAppProtocolStatement").newInstance();
				aps.initialize(this, stream);
			} catch (Exception ex) {
				System.err
						.println("Failed to create2: jgapp.dbas.GAppProtocolStatement");

			}
		}
		return aps;
	}

	protected void executeProviderCmd(int cmdName) {
		// should be implemented by analogy to executeDefaultCmd
	}

	protected void executeCmd(int cmdId) throws IOException {
		String outStr;
		int cmd = stream.readInt();

		ApplicationProtocolStatement aps = createAppProtocolStatement();
		outStr = aps.executeCmd(cmd, null);

		if (outStr != null && outStr.length() != 0)
			stream.writeLine(outStr);

		stream.done();
		aps.close();
	}

	protected void executeDefaultCmd(int dCmd) throws IOException {
		String outStr;
		int cmd = stream.readInt();
		logger.info("executeDefaultCmd():dCmd:" + dCmd + " cmd=" + cmd);

		// get all lines and pack them in an array args
		int no = stream.readInt();
		String[] args = new String[no];

		for (int j = 0; j < no; j++) {
			args[j] = stream.readLine();
			// debug(this,"executeDefaultCmd:args["+j+"]="+args[j]);
		}

		if (stream instanceof ServletProtocolStream) {
			for (int j = 0; j < no; j++) {
				args[j] = SorcerUtil.parseString(args[j]);
			}
		}
		ApplicationProtocolStatement aps = createAppProtocolStatement();

		outStr = (String) aps.executeDefaultCmd(cmd, args);
		logger.info("executeDefaultCmd:result:" + outStr);

		if (outStr != null && outStr.length() != 0) {
			stream.writeLine(outStr);
			// debug(this, "written:outStr=" + outStr);
		}
		stream.done();
		aps.close();
	}

	protected void executeDefaultCmd(int cmd, Object[] args) throws IOException {
		Object outObj = null;
		ApplicationProtocolStatement aps = createAppProtocolStatement();
		outObj = aps.executeDefaultCmd(cmd, args);
		if (outObj != null) {
			stream.writeObject(outObj);
			// stream.writeLine(outStr);
			// debug(this, "written:outStr=" + outStr);
		}
		stream.done();
		aps.close();
	}

	protected boolean savePassword(String userPasswd)
			throws java.sql.SQLException {
		ApplicationProtocolStatement aps = createAppProtocolStatement();
		boolean b = aps.setUserPassword(userPasswd);
		aps.close();
		return b;
	}

	public static boolean isExtended() {
		return ApplicationDomain.props.getProperty(
				"protocolConnection.isExtended", "false").equalsIgnoreCase(
				"true");
	}

	public static String getDefautPoolName() {
		return ApplicationDomain.props.getProperty(
				"protocolConnection.defaultPoolName", SYSTEM);
	}

	/*
	 * public static String getDefaultDbRole() { return
	 * ApplicationDomain.props.getProperty("protocolConnection.defaultDbRole",
	 * Const.SYSTEM); }
	 */

	public boolean isServletProtocolConnection() {
		return false;
	}

	public JdbcConnectionImpl acquireImpl() {
		System.out.println(getClass().getName()
				+ "::acquireImpl() START. principal:" + principal);

		if (principal == null) { // return null;
			principal = new SorcerPrincipal();
			principal.setName(SERVLET);
			principal.setRole(SYSTEM);
			// principal.setAppRole(SYSTEM);
		}

		// String poolName = getPoolName(
		JdbcConnectionImpl woot = connectionPool.acquireImpl(principal
				.getRole());
		System.out.println(getClass().getName() + "::acquireImpl() woot:"
				+ woot + " role:" + principal.getRole());

		return woot;
		// return connectionPool.acquireImpl(principal.getRole());//poolName);
	}

	public static void releaseImpl(JdbcConnectionImpl impl) {
		connectionPool.releaseImpl(impl);
	}

	public SorcerPrincipal getPrincipal() {
		return principal;
	}

	public String userLogin() {
		return principal.getName();
	}

	public String userRole() {
		return principal.getRole();
	}

} // end class Connection

