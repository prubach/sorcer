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
import java.util.logging.Logger;

import sorcer.security.util.SorcerPrincipal;
import sorcer.util.CmdManager;
import sorcer.util.Invoker;
import sorcer.util.Mandate;
import sorcer.util.ProtocolStream;
import sorcer.util.SQLHelper;
import sorcer.util.Sorcer;

public abstract class ApplicationProtocolStatement extends ProtocolStatement
		implements Invoker {
	private static Logger logger = Logger
			.getLogger(ApplicationProtocolStatement.class.getName());
	public String answer = null;
	public CmdManager controller;
	public Object objAnswer;

	public ApplicationProtocolStatement() {
		super();
		createController();
	}

	/*
	 * public ApplicationProtocolStatement(String poolName) { super(poolName);
	 * createController(); }
	 */

	public ApplicationProtocolStatement(ProtocolConnection con) {
		super(con);
		createController();
	}

	public ApplicationProtocolStatement(ProtocolConnection con,
			ProtocolStream stream) {
		super(con, stream);
		createController();
	}

	public Object executeDefaultCmd(int defaultCmd, Object[] args) {
		objAnswer = null;
		answer = null;
		controller.doIt(defaultCmd, args);
		if (objAnswer != null)
			return objAnswer;
		else
			return answer;
	}

	public String executeCmd(int cmd, Object[] args) {
		answer = null;
		controller.doIt(cmd, args);
		return answer;
	}

	protected void createController() {
		if (ProtocolConnection.isExtended()) {
			String prefix = ApplicationDomain.appPrefix;
			System.out.println("prefix " + prefix);
			int i = prefix.indexOf('.');
			System.out.println("prefix i" + prefix + i);
			String providerName = (i > 0) ? prefix.substring(0, i)
					.toLowerCase() : "";
			System.out.println("provider name "
					+ providerName);
			String classPrefix = (i > 0) ? prefix.substring(i + 1) : prefix;
			System.out.println("class prefix " + classPrefix);
			controller = new CmdManager("dbas", providerName, classPrefix, this);
			System.out.println("controller " + controller);
		} else
			controller = new CmdManager("dbas", "GApp", this);
	}

	// used with CmdManager
	public boolean executeSelect(String action) {
		// should be implemnted is subclasses for the action
		// not supported by the CmdFactory
		return true;
	}

	// used with CmdManager
	public boolean executeAction(String cmd) {
		// should be implemnted is subclasses for the action
		// not supported by the CmdFactory
		return true;
	}

	public boolean executeMandate(Mandate mandate) {
		// should be implemnted is subclasses for the action
		// not supported by the CmdFactory
		return true;
	}

	/**
	 * Returns true if id found in a table tableName
	 */
	public boolean idExists(String tableBaseName, String id) {
		return idExists(Sorcer.tableName(tableBaseName), Sorcer
				.seqIdName(tableBaseName), id);
	}

	/**
	 * Returns next id in a table tableBaseName
	 */
	public String nextVal(String tableBaseName) throws SQLException {
		if (ApplicationDomain.isOracle()) {
			result = stmt
					.executeQuery("SELECT " + Sorcer.tableName(tableBaseName)
							+ "_seq.NextVal from DUAL");
			result.next();
			return Integer.toString(result.getInt(1));
		} else
			try {
				Class c = Class.forName("sorcer.persist.util.SQLUtil");
				SQLHelper sh = (SQLHelper) c.newInstance();
				return sh.nextValue(Sorcer.tableName(tableBaseName) + "_SEQ",
						stmt.getConnection());
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			} catch (InstantiationException ie) {
				ie.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}

		return null;
	}

	/**
	 * Returns next id in a sequence seq
	 */
	public String nextValue(String sequence) throws SQLException {
		result = stmt.executeQuery("SELECT " + sequence + "NextVal from DUAL");
		result.next();
		return Integer.toString(result.getInt(1));
	}

	/**
	 * Returns number of rows for select statement for a provided condition
	 */
	public int count(String tableBaseName, String condition)
			throws SQLException {
		result = stmt.executeQuery("SELECT count(*) FROM "
				+ Sorcer.tableName(tableBaseName) + " WHERE " + condition);
		result.next();
		return result.getInt(1);
	}

	/**
	 * Lock a basic table in the exlusive mode
	 */
	public void lockBasicTable(String tableBaseName) throws SQLException {
		lockTable(Sorcer.tableName(tableBaseName));
	}

	/**
	 * Lock a table in the exlusive mode
	 */
	public void lockTable(String table) throws SQLException {
		stmt.executeQuery("LOCK TABLE " + table + " IN EXCLUSIVE MODE");
	}

	/**
	 * Lock a rows in a table with a condition
	 */
	public void lockRow(String rows, String table, String condition)
			throws SQLException {
		StringBuffer request = new StringBuffer("SELECT ").append(rows).append(
				" FROM ").append(table).append(" WHERE ").append(condition)
				.append(" FOR UPDATE");

		stmt.executeQuery(request.toString());
	}

	/**
	 * Delete a record with id in a table tableBaseName
	 */
	public int deleteId(String tableBaseName, String id)
			throws RecordNotFoundException, SQLException {
		if (!idExists(tableBaseName, id))
			throw new RecordNotFoundException(
					"The record requested from the database was not found.");
		else {
			StringBuffer request = new StringBuffer("DELETE FROM ").append(
					Sorcer.tableName(tableBaseName)).append(" WHERE ").append(
					Sorcer.seqIdName(tableBaseName)).append("=").append(id);
			return stmt.executeUpdate(request.toString());
		}
	}

	/**
	 * Delete a record in a table tableBaseName with condition columnn=value
	 */
	public int delete(String tableBaseName, String column, String value)
			throws RecordNotFoundException, SQLException {
		if (!valExists(Sorcer.tableName(tableBaseName), column, value))
			throw new RecordNotFoundException();
		else {
			StringBuffer request = new StringBuffer("DELETE FROM ").append(
					Sorcer.tableName(tableBaseName)).append(" WHERE ").append(
					column).append("='").append(value).append("'");
			return stmt.executeUpdate(request.toString());
		}
	}

	/**
	 * Returns id in a table tableName under condition column=value
	 */
	public String getId(String table, String column, String value)
			throws SQLException {
		StringBuffer request = new StringBuffer("SELECT ").append(
				Sorcer.seqIdName(table)).append(" FROM ").append(
				Sorcer.tableName(table)).append(" WHERE ").append(column)
				.append("=").append(value);
		result = stmt.executeQuery(request.toString());
		if (result.next()) {
			return Integer.toString(result.getInt(1));
		} else
			return null;
	}

	/**
	 * returns previous id in a table tableName
	 */
	public String getPreviousId(String table, String id, String currentId)
			throws SQLException {
		StringBuffer request = new StringBuffer("SELECT MAX(").append(id)
				.append(") FROM ").append(Sorcer.tableName(table)).append(
						" WHERE ").append(id).append("<").append(currentId);

		result = stmt.executeQuery(request.toString());
		if (result.next()) {
			return Integer.toString(result.getInt(1));
		} else
			return null;
	}

	public SorcerPrincipal getUserAccess(String userLogin) throws SQLException {
		return getUserAccess(userLogin, true);
	}

	public SorcerPrincipal getUserAccess(String userLogin, boolean isLogin)
			throws SQLException {
		StringBuffer request = new StringBuffer("SELECT Password,").append(
				Sorcer.tableName("User")).append(".").append(
				(isLogin) ? "User_Seq_Id," : "Login,").append("SSOID,").append(
				Sorcer.tableName("Role")).append(".").append("Name,").append(
				"Project, ").append("email, ").append("Export_Control, ")
				.append("Access_Class, ").append("first_Name, ").append(
						"last_Name, ").append("Phone ").append(" FROM ")
				.append(Sorcer.tableName("USER")).append(",").append(
						Sorcer.tableName("Roles ")).append(",").append(
						Sorcer.tableName("Role ")).append(" WHERE ");

		if (isLogin) {
			request.append("Login='").append(userLogin).append("'");
		} else {
			request.append(Sorcer.tableName("User")).append(".").append(
					"User_Seq_Id=").append(userLogin);
		}

		request.append(" and ").append(Sorcer.tableName("User")).append(".")
				.append("User_Seq_Id=").append(Sorcer.tableName("Roles"))
				.append(".").append("User_Seq_Id and ").append(
						Sorcer.tableName("Roles")).append(".").append(
						"Role_Seq_Id=").append(Sorcer.tableName("Role")).append(
						".").append("Role_Seq_Id");
		request.append(" AND Valid=1");

		if (ApplicationDomain.isMultiProject) {
			request.append(" AND Project='").append(ApplicationDomain.appName)
					.append("'");
		}
		logger.info("getUserAccess:query: " + request.toString());

		result = stmt.executeQuery(request.toString());
		String[] items = new String[4];

		if (result.next()) {
			SorcerPrincipal principal = new SorcerPrincipal();
			principal.setPassword(result.getString(1));
			if (isLogin) {
				principal.setId(result.getString(2));
				principal.setName(userLogin);
			} else {
				principal.setName(result.getString(2));
				principal.setId(userLogin);
			}
			principal.setSSO(result.getString(3));
			principal.setRole(result.getString(4));
			principal.setProject(result.getString(5));
			principal.setEmailId(result.getString(6));
			principal.setExportControl(!"0".equals(result.getString(7)));
			try {
				principal.setAccessClass(Integer.parseInt(result.getString(8)));
			} catch (Exception e) {
			}

			principal.firstName = result.getString(9);
			principal.lastName = result.getString(10);
			principal.phone = result.getString(11);
			if (pConnection != null && pConnection.principal != null)
				principal.setSessionID(pConnection.principal.getSessionID());
			pConnection.principal = principal;
			return principal;
		} else
			return null;
	}

	// Validate and populate the GAppPrincipal in pConnection
	public boolean validateUserLogin() throws SQLException {
		SorcerPrincipal principal = getUserAccess(pConnection.getPrincipal()
				.getName(), true);
		return (principal != null);
	}

	public boolean setUserPassword(String userPasswd) throws SQLException {
		StringBuffer request = new StringBuffer("Update ").append(
				Sorcer.tableName("USER")).append(" set Password='").append(
				userPasswd).append("' where user_Seq_ID=").append(
				pConnection.getPrincipal().getId());
		result = stmt.executeQuery(request.toString());
		return true;
	}

}
