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

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceClient {
	Connection conn;

	public DataSourceClient() {
		Context context;

		try {
			// create and store parameters which are used to create the context
			FileInputStream fin = new FileInputStream(ApplicationDomain.asDir
					+ "/" + "pools.properties");
			Properties props = new Properties();
			props.load(fin);
			Hashtable env = new Hashtable();
			String initialContextFactory = props
					.getProperty("provider.initialContext.factory");
			String jndiProviderURL = props
					.getProperty("provider.JNDIProvider.url");
			String dataSourceName = props
					.getProperty("provider.dataSource.name");

			env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			env.put(Context.PROVIDER_URL, jndiProviderURL);
			// create the context
			context = new InitialContext(env);
			// call method to get DataSource and Connection
			getDataSource(context, dataSourceName);
			// call query method
			// query();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get the DataSource and use it to get a connection
	void getDataSource(Context context, String bindName) throws SQLException,
			NamingException {
		// Don't need url or username/password in client,
		// all that is needed is the bind name for lookup
		DataSource source = (DataSource) context.lookup(bindName);
		conn = source.getConnection();
		System.out.println("Connection : " + conn);
	}

	// use the connection to query the patients table
	void query() throws SQLException {
		Statement statement = conn.createStatement();
		String sql = "select * from patients";
		ResultSet rset = statement.executeQuery(sql);

		while (rset.next()) {
			System.out.println("Patient: " + rset.getInt("PATIENT_ID") + " "
					+ rset.getString("GIVEN_NAME") + " "
					+ rset.getString("SURNAME"));
		}

		rset.close();
		statement.close();
		conn.close();
		conn = null;
	}

	public static void main(String[] args) {
		// create and execute the client
		new DataSourceClient();
	}
}
