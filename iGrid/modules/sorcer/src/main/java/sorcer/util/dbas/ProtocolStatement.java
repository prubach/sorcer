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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.util.ProtocolStream;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

public class ProtocolStatement implements SorcerConstants {
	private static Logger logger = Logger.getLogger(ProtocolStatement.class
			.getName());
	private JdbcConnectionImpl impl;
	public Connection dbConnection;
	public Statement stmt;

	public ProtocolStream stream;
	protected ResultSet result;
	public ProtocolConnection pConnection;
	int page = 1, pageSize = 100;

	public ProtocolStatement() {
		// do Nothing
	}

	public ProtocolStatement(ProtocolConnection con) {
		initialize(con, null);
	}

	public ProtocolStatement(ProtocolConnection con, ProtocolStream stream) {
		initialize(con, stream);
		this.stream = stream;
	}

	public ProtocolStatement(ProtocolConnection con, ProtocolStream stream,
			int page, int pageSize) {
		this(con, stream);
		this.page = page;
		this.pageSize = pageSize;
	}

	public void initialize(ProtocolConnection con, ProtocolStream stream) {
		System.out.println(getClass().getName() + "::initialize() START.");

		this.stream = stream;
		pConnection = con;
		try {
			System.out.println(getClass().getName()
					+ "::initialize pConnection:" + pConnection);
			impl = pConnection.acquireImpl();
			System.out.println(getClass().getName() + "::initialize impl:"
					+ impl);
			dbConnection = impl.getConnection();
			stmt = dbConnection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Puts single quotes around String objec because queries require single
	 * quotes around string values
	 */
	public String makeSQLString(String s) {
		String quotedString = new String("'" + s + "'");
		return quotedString;
	}

	/**
	 * Select takes what field to look for, what table to search, and any
	 * conditions that limit the search.
	 */
	protected ResultSet select(String what, String table, String conditions) {

		try {
			result = stmt.executeQuery("SELECT " + what + " FROM " + table
					+ " WHERE " + conditions);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Select takes what field to look for, what table to search, and returns a
	 * result containing all the information in that table
	 */
	private ResultSet select(String what, String table) {
		try {
			result = stmt.executeQuery("SELECT " + what + " FROM " + table);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Returns true if value found in a column and table
	 */
	public boolean valExists(String table, String column, String value) {
		return idExists(table, column, "'" + value + "'");
	}

	/**
	 * Returns true if id found in a table tableName
	 */
	public boolean idExists(String table, String column, String id) {
		try {
			StringBuffer request = new StringBuffer("SELECT ").append(column)
					.append(" FROM ").append(table).append(" WHERE ").append(
							column).append("=").append(id);
			logger.info("idExists:query: " + request.toString());

			result = stmt.executeQuery(request.toString());
			logger.info("idExists:result: " + result);
			return result.next();
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Returns true if val1 of att2 and val2 of att2 found in a table tableName,
	 * val1 and val1 to are not SQL strings.
	 */
	public boolean val2Exists(String tableName, String att1, String val1,
			String att2, String val2) {
		try {
			StringBuffer request = new StringBuffer("SELECT ").append(att1)
					.append(" FROM ").append(tableName).append(" WHERE ")
					.append(att1).append("=").append(val1).append(" AND ")
					.append(att2).append("=").append(val2);
			result = stmt.executeQuery(request.toString());
			return result.next();
		} catch (SQLException e) {
			return false;
		}
	}

	/*----------------Returns true if att1=val1 for some other seqId(other than the one to be updated)----------------*/
	public boolean updateValExists(String tableBaseName, String seqId,
			String att1, String val1) {
		try {
			StringBuffer request = new StringBuffer("SELECT ").append(
					Sorcer.seqIdName(tableBaseName)).append(" FROM ").append(
					Sorcer.tableName(tableBaseName)).append(" WHERE ").append(
					att1).append("=").append("'").append(val1).append("'");
			result = stmt.executeQuery(request.toString());
			for (int i = 0;; i++) {
				result.next();
				if (!String.valueOf(result.getInt(1)).equals(seqId))
					throw new DuplicateIDException();
			}
		} catch (SQLException e) {
			return false;
		} catch (DuplicateIDException e) {
			return true;
		}
	}

	public void executeQuery(String queryLine) throws IOException {
		StringBuffer sb;
		Object colVal;
		int columns;
		int pos;
		try {
			result = stmt.executeQuery(queryLine);
			columns = result.getMetaData().getColumnCount();

			// if(pConnection.selectedHeader!=null)
			// out.writeUTF(pConnection.selectedHeader);

			while (result.next()) {
				sb = new StringBuffer(1024);
				for (pos = 1; pos <= columns; pos++) {
					colVal = result.getObject(pos);
					if (columns > 1 && pos != columns)
						sb.append(result.getObject(pos)).append(SEP);
					else
						sb.append(result.getObject(pos));
				}
				logger.info("executeQuery: " + sb.toString());
				if (stream.isObjectStream())
					stream.writeObject(sb.toString());
				else
					stream.writeEscapedLine(sb.toString());
			}
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void executePageQuery(String queryLine) throws IOException {
		StringBuffer sb;
		Object colVal;
		int columns;
		int pos, i = 0, lb = page * pageSize, ub = (page + 1) * pageSize;
		try {
			result = stmt.executeQuery(queryLine);
			columns = result.getMetaData().getColumnCount();

			// if(pConnection.selectedHeader!=null)
			// out.writeUTF(pConnection.selectedHeader);

			while (result.next() && i < page * pageSize) {
				if (i >= lb && i < ub) {
					sb = new StringBuffer(1024);
					for (pos = 1; pos <= columns; pos++) {
						colVal = result.getObject(pos);
						if (columns > 1 && pos != columns)
							sb.append(result.getObject(pos)).append(SEP);
						else
							sb.append(result.getObject(pos));
					}
					logger.info("executeQuery: " + sb.toString());
					stream.writeEscapedLine(sb.toString());
				} else if (i >= ub)
					break;
				i = i + 1;
			}
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void executeQueryFor(String queryLine) throws IOException {
		StringBuffer sb;
		Object colVal;
		PreparedStatement pstmt;
		int columns;
		int pos;

		try {
			String[] tokens = SorcerUtil.tokenize(queryLine, DELIM);
			pstmt = dbConnection
					.prepareStatement((String) ApplicationDomain.queries
							.get(tokens[0]));
			for (int i = 1; i < tokens.length; i++)
				pstmt.setString(i, tokens[i]);
			result = pstmt.executeQuery();

			columns = result.getMetaData().getColumnCount();

			// if(pConnection.selectedHeader!=null)
			// out.writeUTF(pConnection.selectedHeader);

			while (result.next()) {
				sb = new StringBuffer(256);
				for (pos = 1; pos <= columns; pos++) {
					if (result.getObject(pos) instanceof String) {
						colVal = SorcerUtil.escapeReturns((String) result
								.getObject(pos));
					} else {
						colVal = result.getObject(pos);
					}
					if (columns > 1 && pos != columns)
						// sb.append(result.getObject(pos)).append(sep);
						sb.append(colVal).append(SEP);
					else
						// sb.append(result.getObject(pos));
						sb.append(colVal);
				}
				logger.info("executeQueryFor=" + sb.toString());
				stream.writeEscapedLine(sb.toString());
			}
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String executeUpdate(String queryLine) {
		logger.info("executeUpdate: " + queryLine);
		int i = -1;
		try {
			i = stmt.executeUpdate(queryLine);
			stmt.getConnection().commit();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return String.valueOf(i);
	}

	protected String executeUpdateFor(String queryLine) {
		int i = -1;
		PreparedStatement pstmt;

		try {
			String[] tokens = SorcerUtil.tokenize(queryLine, String
					.valueOf(DELIM));
			pstmt = dbConnection
					.prepareStatement((String) ApplicationDomain.queries
							.get(tokens[0]));
			for (i = 1; i < tokens.length; i++)
				pstmt.setString(i - 1, tokens[i]);
			i = pstmt.executeUpdate();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return String.valueOf(i);
	}

	protected Object executeDefaultCmd(int defaultCmd, Object[] data) {
		/*
		 * Implement in sublclasses for apps using the following template:
		 * 
		 * if (defaultCmd.equals(MYAPP.MYCOMMAND)) query =
		 * "create an SQL query for MYAPP.MYCOMMAND"; //then process the query
		 * 
		 * or in the way DefaultProtocolStatement and GAppProtocolProvider are
		 * implemented using Factory and Command design patterns
		 */
		return "ProtocolStatement:defaultQuery: should be reimplemented by subclasses";
	}

	public void close() {
		try {
			stmt.close();
			if (impl != null)
				pConnection.releaseImpl(impl);
			// make sure implementation is release once
			impl = null;
		} catch (SQLException e) {
			System.err.println("Failed to close JDBC Statement");
		}
	}

} // end class ProtocolStatement

