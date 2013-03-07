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
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import oracle.jdbc.pool.OracleDataSource;

public class DataSourceBinder {
	public DataSourceBinder() {

		Context context = null;
		try {
			Hashtable env = new Hashtable();
			String str = System.getProperty("binderProperties");
			FileInputStream fin;
			if (str != null)
				// load("/" + str); //search all paths in CLASSPATH
				fin = new FileInputStream(str);
			else
				fin = new FileInputStream("as.def");

			Properties props = new Properties();
			props.load(fin);
			String factory = props
					.getProperty("provider.initialContext.factory");
			env.put(Context.INITIAL_CONTEXT_FACTORY, factory.trim());
			String prvURL = props.getProperty("provider.JNDIProvider.url");
			env.put(Context.PROVIDER_URL, prvURL.trim());
			context = new InitialContext(env);

			String bindName = props.getProperty("provider.dataSource.name");
			createDataSource(context, bindName.trim());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// CreateDataSource and bind
	static void createDataSource(Context ctx, String bindName)
			throws SQLException, NamingException {
		OracleDataSource dataSource = new OracleDataSource();
		dataSource.setUser("sorcer");
		dataSource.setPassword("1sorcer1");
		dataSource.setDriverType("thin");
		dataSource.setNetworkProtocol("tcp");
		dataSource.setServerName("mondrian.cs.ttu.edu");
		dataSource.setPortNumber(1521);
		dataSource.setDatabaseName("QASORCER");
		ctx.rebind(bindName, dataSource);
	}

	public static void main(String[] args) throws SQLException, NamingException {
		new DataSourceBinder();
	}
}
