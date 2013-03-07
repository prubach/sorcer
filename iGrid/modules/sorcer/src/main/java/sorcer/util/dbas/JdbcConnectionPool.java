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

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.util.SorcerUtil;

public class JdbcConnectionPool {
	Logger logger = Logger.getLogger(JdbcConnectionPool.class.getName());

	// The one instance of this class
	private static JdbcConnectionPool thePool;

	// poolName ->
	// InitialContext.Factory|JNDIProvider.url|dataSource.name|poolSize
	private static Hashtable poolMap = new Hashtable();

	// This hash table is associates connection names with the a
	// corresponding Stack that contains a pool of connections for
	// an underlying database.
	private static Hashtable poolDictionary = new Hashtable();

	// This constructor is private to prevent other classes from
	// creating instances of this class.
	private JdbcConnectionPool() {
	}

	/**
	 * Initialize the pool
	 */

	public static void initialize() {
		loadProperties();
		thePool = new JdbcConnectionPool();
	}

	public static void loadProperties() {
		Properties props = ApplicationDomain.props;
		String key = null, value = null;
		String[] tokens = null;
		for (Enumeration e = props.keys(); e.hasMoreElements();) {
			key = (String) e.nextElement();
			if (key.startsWith("pool")) {
				value = props.getProperty(key);
				tokens = SorcerUtil.tokenize(value, SorcerConstants.SEP);
				poolMap.put(tokens[0], new String[] { tokens[1], tokens[2],
						tokens[3], tokens[4] });
			}
		}
	}

	/**
	 * Return the one instance of this class.
	 */
	public static JdbcConnectionPool getInstance() {
		return thePool;
	}

	/**
	 * Return a JdbcConnectionImpl from the apropriate pool or create one if the
	 * pool is empty.
	 * 
	 * @param dbName
	 *            The name of the database that a JdbcConnectionImpl is to be
	 *            supplied for.
	 */
	public synchronized JdbcConnectionImpl acquireImpl(String dbRole) {
		String poolName = ProtocolConnection.getPoolName(dbRole);
		logger.info("PoolName : " + poolName + "dbRole : " + dbRole);
		Stack pool = (Stack) poolDictionary.get(poolName);
		JdbcConnectionImpl impl = null;

		if (pool != null) {
			if (!pool.empty())
				impl = (JdbcConnectionImpl) pool.pop();
		}

		// // if null, No JdbcConnectionImpl in pool/ invalid connection, create
		// one.
		try {
			if (impl == null || impl.getConnection() == null
					|| impl.getConnection().isClosed())
				impl = new JdbcConnectionImpl(dbRole);
		} catch (SQLException ex) {
			// If it gets here it means database access Error!
			// try again to get a new connection
			impl = new JdbcConnectionImpl(dbRole);
		}
		return impl;
	}

	/**
	 * Add a JdbcConnectionImpl to the appropriate pool.
	 */
	public synchronized void releaseImpl(JdbcConnectionImpl impl) {
		String poolName = impl.getPoolName();
		logger.info("________________________________release Impl  poolName="
				+ poolName);
		Stack pool = (Stack) poolDictionary.get(poolName);
		if (pool == null) {
			pool = new Stack();
			pool.push(impl);
			poolDictionary.put(poolName, pool);
		} else if (pool.size() <= maxSize(poolName))
			pool.push(impl);
		else
			impl.close();
		printSize(poolName);
	}

	public void printSize(String poolName) {
		Stack pool = (Stack) poolDictionary.get(poolName);
		if (pool == null)
			logger.info("\n\nCurrent poolMap=" + poolMap + " Pool Name="
					+ poolName + " Pool Size :null");
		else
			logger.info("\n\nCurrent poolMap=" + poolMap + " Pool Name="
					+ poolName + " Pool Size : " + pool.size() + "MaxSize="
					+ maxSize(poolName) + "\n\n");
	}

	/**
	 *Returns Max Size for a given poolName
	 */
	public static int maxSize(String poolName) {
		if (poolName == null || !poolMap.containsKey(poolName))
			return 0;
		else
			return Integer.parseInt(((String[]) poolMap.get(poolName))[3]);
	}

	/**
	 *Returns Context Factory for a role
	 **/
	public static String getContextFactory(String poolName) {
		if (poolName == null || !poolMap.containsKey(poolName))
			return null;
		else
			return ((String[]) poolMap.get(poolName))[0];
	}

	/**
	 *Returns JNDIProvider for a role
	 **/
	public static String getJNDIProviderURL(String poolName) {
		if (poolName == null || !poolMap.containsKey(poolName))
			return null;
		else
			return ((String[]) poolMap.get(poolName))[1];
	}

	/**
	 *Returns DataSourceName for a role
	 **/
	public static String getDataSourceName(String poolName) {
		if (poolName == null || !poolMap.containsKey(poolName))
			return null;
		else
			return ((String[]) poolMap.get(poolName))[2];
	}
}
