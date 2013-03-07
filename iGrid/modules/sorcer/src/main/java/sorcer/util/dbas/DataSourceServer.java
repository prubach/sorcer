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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import oracle.jdbc.pool.OracleDataSource;

public class DataSourceServer {
	public DataSourceServer() {
		Context context = null;

		try {
			// create and store parameters which are used to create the context
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.fscontext.RefFSContextFactory");
			env.put(Context.PROVIDER_URL, "file:///local/home/sorcer/database");
			// create the context
			context = new InitialContext(env);
			// call method to create and bind the data source
			createDataSource(context, "SOC-JDBC");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create and bind the data source
	static void createDataSource(Context ctx, String bindName)
			throws SQLException, NamingException {
		// Create an OracleDataSource object
		OracleDataSource dataSource = new OracleDataSource();
		dataSource.setUser("sorcer");
		dataSource.setPassword("sorcer20B");
		dataSource.setDriverType("thin");
		dataSource.setNetworkProtocol("tcp");
		dataSource.setServerName("pipal.cs.ttu.edu");
		dataSource.setPortNumber(1521);
		dataSource.setDatabaseName("sorcer");
		ctx.rebind(bindName, dataSource);

	}

	public static void main(String[] args) throws SQLException, NamingException {
		// start the server
		new DataSourceServer();
		System.out.println("Completed");
	}
}
