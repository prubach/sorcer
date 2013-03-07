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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import sorcer.util.ProtocolStream;
import sorcer.util.ServletProtocolStream;
import sorcer.util.Sorcer;
import sorcer.util.dbas.ApplicationDomain;
import sorcer.util.dbas.ApplicationProtocolStatement;
import sorcer.util.dbas.ProtocolConnection;
import sorcer.util.dbas.ServletProtocolConnection;

public class SorcerProtocolStatement extends ApplicationProtocolStatement {

	static public String url;

	public SorcerProtocolStatement() {
		super();
		getHttpURL();
	}

	public SorcerProtocolStatement(ProtocolConnection con) {
		super(con);
		getHttpURL();
	}

	public SorcerProtocolStatement(ProtocolConnection con, ProtocolStream stream) {
		super(con, stream);
		getHttpURL();
	}

	private static void getHttpURL() {
		url = "http://" + ApplicationDomain.getProperty("http:host") + ":"
				+ ApplicationDomain.getProperty("http:port");
	}

	public String[] getAccess(String userLogin) throws SQLException {
		return getAccess(userLogin, true);
	}

	/**
	 * Returns an array of user Id, role, password and login for the underlying
	 * application
	 */
	public String[] getAccess(String userLogin, boolean isLogin)
			throws SQLException {
		StringBuffer request = new StringBuffer(
				"SELECT User_Seq_Id,Role,Password,Login").append(" FROM ")
				.append(Sorcer.tableName("USER")).append(" WHERE ");

		if (isLogin) {
			request.append("Login='").append(userLogin).append("'");
		} else {
			request.append("User_Seq_Id=").append(userLogin);
		}

		request.append(" AND Valid=1");

		if (ApplicationDomain.isMultiProject) {
			request.append(" AND Project='").append(ApplicationDomain.appName)
					.append("'");
		}
		result = stmt.executeQuery(request.toString());
		String[] items = new String[4];
		if (result.next()) {
			items[0] = result.getString(1);
			items[1] = result.getString(2);
			items[2] = result.getString(3);
			items[3] = result.getString(4);
			return items;
		} else
			return null;
	}

	public boolean setUserPassword(String userPasswd) throws SQLException {
		// should be implemented
		return false;
	}

	public PrintWriter getWriter() {
		return ((ServletProtocolStream) stream).out;
	}

	// implemeneted now in superclass
	// public HttpSession getSession() {
	// return ((ServletProtocolConnection)pConnection).session;
	// }

	public ServletProtocolConnection getProtocolConnection() {
		return (ServletProtocolConnection) pConnection;
	}

	public Connection getJDBCConnection() {
		return dbConnection;
	}

	public Statement getJDBCStatement() {
		return stmt;
	}
}
