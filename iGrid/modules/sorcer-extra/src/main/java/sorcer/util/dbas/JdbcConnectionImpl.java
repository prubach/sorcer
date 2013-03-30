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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import sorcer.util.Stopwatch;

/**
 * Instances of this class provide actual connections to a database.
 */
public class JdbcConnectionImpl {
	private static Logger logger = Logger.getLogger(JdbcConnectionImpl.class
			.getName());
	private String poolName;
	private Connection jdbcConnection;
	private RemakeDBConnection rc;

	protected JdbcConnectionImpl(String dbRole) {
		poolName = ProtocolConnection.getPoolName(dbRole);
		// Util.debug(this,"_______________poolName="+poolName);
		// if Intersolv driver then modify driver name
		/*
		 * if (driver.indexOf("intersolv")>-1) { StringBuffer sb = new
		 * StringBuffer(driver); sb.append(";OSUser="); sb.append(username);
		 * sb.append(";OSPassword="); sb.append(password);
		 * sb.append(";Datasource=");
		 * sb.append(ApplicationDomain.props.getProperty
		 * ("applicationServer.datasource." + poolName)); driver =
		 * sb.toString(); }
		 */
		jdbcConnection = makeDBConnection();
		if (jdbcConnection == null) {
			if (rc != null && rc.connectionState) {
				jdbcConnection = rc.jdbcConnection;
				rc = null;
			}
		}
	}

	String getPoolName() {
		return poolName;
	}

	public Connection getConnection() {
		// PLs check JdbcConnectionPool.acquireImpl(). It checks if connection
		// is valid and if not
		// creates a valid one!
		try {
			jdbcConnection.setAutoCommit(false);
		} catch (SQLException sqle) {
			// try to recreate the connection
			sqle.printStackTrace();
			jdbcConnection = makeDBConnection();
		}
		return jdbcConnection;
	}

	public void close() {
		try {
			if (jdbcConnection != null)
				jdbcConnection.close();

			jdbcConnection = null;
		} catch (SQLException e) {
			jdbcConnection = null;
		}
	}

	public Connection makeDBConnection() {
		Connection connection;
		JdbcConnectionPool pool;
		String dataSourceName = JdbcConnectionPool.getDataSourceName(poolName);
		Context context = null;
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, JdbcConnectionPool
					.getContextFactory(poolName));
			env.put(Context.PROVIDER_URL, JdbcConnectionPool
					.getJNDIProviderURL(poolName));
			context = new InitialContext(env);
			connection = getConnection(context, dataSourceName);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			System.err.println("Not able to connect to databasesource "
					+ dataSourceName);
			connection = null;
			rc = new RemakeDBConnection(context, dataSourceName);

		} catch (NamingException nex) {
			nex.printStackTrace();
			System.err.println("Not able to connect to datasource "
					+ dataSourceName);
			connection = null;
		}
		return connection;
	}

	static Connection getConnection(Context context, String bindName)
			throws SQLException, NamingException {
		DataSource source = (DataSource) context.lookup(bindName);
		Connection connection = source.getConnection();
		return connection;
	}

	// This class will attempt to remake the JDK connection with
	// the application database every sleepTime seconds (by default).
	class RemakeDBConnection extends Thread {
		boolean connectionState = false, isInterrupted = false;
		Connection jdbcConnection;
		int sleepTime = 3000, timeout = 15000;
		Context context;
		String bindName;

		// Takes Context and bindName
		public RemakeDBConnection(Context context, String bindName) {
			this.context = context;
			this.bindName = bindName;
			start();
		}

		// The run method
		// This is the thread body that is attempting every sleepTime seconds
		// to remake the connection.
		public void run() {
			// Attempt to remake the connection
			Stopwatch stopwatch = new Stopwatch();
			while (!isInterrupted) {
				// Attempt to connect to a database
				logger.info("RemakeDBConnection: attempting to reconnect...");
				logger.info("Context  =" + context);
				logger.info("bindName =" + bindName);

				try {
					if ((jdbcConnection != null))
						jdbcConnection.close();
					jdbcConnection = JdbcConnectionImpl.getConnection(context,
							bindName);
					SQLWarning warning = null;
					try {
						warning = jdbcConnection.getWarnings();
						if (warning == null)
							System.out
									.println("No SQL warnings while connected to database");
						while (warning != null) {
							System.out.println("SQL Warning: " + warning);
							warning = warning.getNextWarning();
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					connectionState = true;
					break;
				} catch (Exception e) {
					System.err.println("Still not able to connect to database");
				}

				if (stopwatch.get() > timeout) {
					// allow to exit RemakeDBConnection thread gracefully
					isInterrupted = true;
					connectionState = false;
					System.err
							.println("Made five attempts, not able to connect to database ");
					break;
				}

				// Still here, let's wait awhile (sleepTime)
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // end while
			if (isInterrupted) {
				System.err.println("RemakeDBConnection:time-out: "
						+ stopwatch.get());
			} else
				System.err.println("RemakeDBConnection:successful: "
						+ stopwatch.get());
		}
	}
}
