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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import sorcer.core.SorcerConstants;
import sorcer.util.Command;
import sorcer.util.Invoker;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

public class SorcerTypeCmd implements Command, SorcerConstants {
	final static public String context = "sorcerType";
	String cmdName;
	Object[] arg;
	Hashtable sorcerTypes, ftScratch;
	public SorcerProtocolStatement fps;
	private ResultSet result;
	private HttpSession session;

	public SorcerTypeCmd(String cmdName) {
		this.cmdName = cmdName;
	}

	public void setArgs(Object target, Object[] arg) {
		fps = (SorcerProtocolStatement) target;
		this.arg = arg;
	}

	public void doIt() {
		try {
			if (cmdName.equals(Integer.toString(PERSIST_SORCER_TYPES))) {
				sorcerTypes = (Hashtable) arg[0];
				ftScratch = (Hashtable) arg[1];
				persistSorcerTypes();
			} else if (cmdName.equals(Integer.toString(PERSIST_SORCER_NAME))) {
				addSorcerName();
			} else if (cmdName.equals(Integer.toString(RENAME_SORCER_NAME))) {
				renameSorcerName();
			}
		} catch (SQLException se) {
			se.printStackTrace();
			fps.answer = "ERROR:" + se.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			fps.answer = "ERROR:" + e.getMessage();
		}
	}

	public void persistSorcerTypes() throws SQLException {
		try {
			fps.dbConnection.setAutoCommit(false);
			// renameSorcerNames();
			addSorcerTypes();
			addModifiers();
			fps.dbConnection.setAutoCommit(true);
			fps.dbConnection.commit();
			fps.dbConnection.setAutoCommit(false);
			updateSorcerTypes();
			updateModifiers();
			fps.dbConnection.setAutoCommit(true);
			fps.dbConnection.commit();
			fps.dbConnection.setAutoCommit(false);
			deleteModifiers();
			deleteSorcerTypes();
			fps.dbConnection.setAutoCommit(true);
			fps.dbConnection.commit();
		} catch (SQLException ex) {
			ex.printStackTrace();
			fps.dbConnection.rollback();
			fps.dbConnection.setAutoCommit(true);
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			fps.dbConnection.rollback();
			fps.dbConnection.setAutoCommit(true);
		}
	}

	private String getFt(String ftID) {
		String match = ftID + SEP;
		String ft = null;
		for (Enumeration e = sorcerTypes.keys(); e.hasMoreElements();) {
			ft = (String) e.nextElement();
			if (((String) sorcerTypes.get(ft)).indexOf(match) >= 0)
				return ft;
		}
		return null;
	}

	private void addSorcerTypes() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("ADD");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer addRequest = new StringBuffer("Insert into ")
				.append(Sorcer.tableName("SORCER_TYPE"))
				.append(
						" (Sorcer_Type_Seq_Id,Sorcer_Type_Name,Application_Cd,Format_Cd) ")
				.append("values (?,?,?,?)");

		PreparedStatement addFtstmt = fps.dbConnection
				.prepareStatement(addRequest.toString());
		String ftName = null, ft = null, newFtID = null;
		String aft[];
		for (int i = 0; i < scratch.size(); i++) {
			ft = getFt((String) scratch.elementAt(i));
			if (ft == null)
				continue;
			ftName = SorcerUtil.secondToken((String) sorcerTypes.get(ft), SEP);
			aft = SorcerUtil.tokenize(ft, SEP);
			newFtID = fps.nextVal("Sorcer_Type");
			addFtstmt.setInt(1, Integer.parseInt(newFtID));
			addFtstmt.setString(2, ftName);
			addFtstmt.setInt(3, Integer.parseInt(aft[0]));
			addFtstmt.setInt(4, Integer.parseInt(aft[1]));
			addFtstmt.executeUpdate();
			// update sorcerType hashtable & ftScratch with new ftID
			sorcerTypes.put(ft, newFtID + SEP + ftName);
			scratch.setElementAt(newFtID, i);
		}
	}

	private void updateSorcerTypes() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("UPDATE");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer updateRequest = new StringBuffer("Update ")
				.append(Sorcer.tableName("SORCER_TYPE"))
				.append(
						" set Sorcer_Type_Seq_Id=?,Sorcer_Type_Name=?,Application_cd=?,Format_Cd=? ");

		PreparedStatement updateFtstmt = fps.dbConnection
				.prepareStatement(updateRequest.toString());
		String ftName = null, ft = null;
		String aft[];
		for (int i = 0; i < scratch.size(); i++) {
			ft = getFt((String) scratch.elementAt(i));
			if (ft == null)
				continue;
			ftName = SorcerUtil.secondToken((String) sorcerTypes.get(ft), SEP);
			aft = SorcerUtil.tokenize(ft, SEP);
			updateFtstmt.setInt(1, Integer.parseInt(SorcerUtil.firstToken(
					(String) sorcerTypes.get(ft), SEP)));
			updateFtstmt.setString(2, ftName);
			updateFtstmt.setInt(3, Integer.parseInt(aft[0]));
			updateFtstmt.setInt(4, Integer.parseInt(aft[1]));
			updateFtstmt.executeUpdate();
		}
	}

	private void addModifiers() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("ADD");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer addRequest = new StringBuffer("Insert into ").append(
				Sorcer.tableName("MODIFIER")).append(
				" (Sorcer_Type_Seq_Id,Modifier_Cd) ").append("values (?,?)");

		PreparedStatement addModifierstmt = fps.dbConnection
				.prepareStatement(addRequest.toString());
		String ft = null, modID = null;
		String amod[];
		for (int i = 0; i < scratch.size(); i++) {
			ft = getFt((String) scratch.elementAt(i));
			if (ft == null)
				continue;
			modID = SorcerUtil.getItem(ft, 2, SEP);
			amod = SorcerUtil.tokenize(modID, ".");
			for (int j = 0; j < amod.length; j++) {
				addModifierstmt.setInt(1, Integer.parseInt((String) scratch
						.elementAt(i)));
				addModifierstmt.setString(2, amod[j]);
				addModifierstmt.executeUpdate();
			}
		}
	}

	private void updateModifiers() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("UPDATE");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer delRequest = new StringBuffer(
				"Delete from FIP_MODIFIER where ")
				.append("Sorcer_Type_Seq_Id= ?");

		StringBuffer addRequest = new StringBuffer("Insert into ").append(
				Sorcer.tableName("MODIFIER")).append(
				" (Sorcer_Type_Seq_Id,Modifier_Cd) ").append("values (?,?)");

		PreparedStatement insertModifierstmt = fps.dbConnection
				.prepareStatement(addRequest.toString());
		PreparedStatement deleteModifierstmt = fps.dbConnection
				.prepareStatement(delRequest.toString());
		String ft = null, modID = null;
		String amod[];
		for (int i = 0; i < scratch.size(); i++) {
			ft = getFt((String) scratch.elementAt(i));
			if (ft == null)
				continue;
			modID = SorcerUtil.getItem(ft, 2, SEP);
			amod = SorcerUtil.tokenize(modID, ".");
			deleteModifierstmt.setInt(1, Integer.parseInt((String) scratch
					.elementAt(i)));
			deleteModifierstmt.executeUpdate();
			for (int j = 0; j < amod.length; j++) {
				insertModifierstmt.setInt(1, Integer.parseInt((String) scratch
						.elementAt(i)));
				insertModifierstmt.setInt(2, Integer.parseInt(amod[j]));
				insertModifierstmt.executeUpdate();
			}
		}
	}

	private void deleteSorcerTypes() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("DELETE");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer delRequest = new StringBuffer(
				"Delete from FIP_SORCER_TYPE where ")
				.append("Sorcer_Type_Seq_Id=?");
		PreparedStatement delFtstmt = fps.dbConnection
				.prepareStatement(delRequest.toString());
		for (int i = 0; i < scratch.size(); i++) {
			delFtstmt
					.setInt(1, Integer.parseInt((String) scratch.elementAt(i)));
			delFtstmt.executeUpdate();
		}
	}

	private void deleteModifiers() throws SQLException {
		Vector scratch = (Vector) ftScratch.get("DELETE");
		if (scratch == null || scratch.isEmpty())
			return;

		StringBuffer delRequest = new StringBuffer(
				"Delete from FIP_MODIFIER where ")
				.append("Sorcer_Type_Seq_Id=?");
		PreparedStatement delModstmt = fps.dbConnection
				.prepareStatement(delRequest.toString());
		for (int i = 0; i < scratch.size(); i++) {
			delModstmt.setInt(1, Integer
					.parseInt((String) scratch.elementAt(i)));
			delModstmt.executeUpdate();
		}
	}

	private void addSorcerName() throws SQLException {
		String name_SeqID = null;
		if (!fps.valExists(Sorcer.tableName("NAME"), "Name", SorcerUtil
				.firstToken((String) arg[0], SEP))) {
			name_SeqID = fps.nextVal("NAME");

			StringBuffer request = new StringBuffer("Insert  into ").append(
					Sorcer.tableName("NAME"))
					.append("(Name_Cd,Name,Comments) ").append(" values (")
					.append(name_SeqID).append(",'").append(
							SorcerUtil.firstToken((String) arg[0], SEP))
					.append("','").append(
							SorcerUtil.secondToken((String) arg[0], SEP))
					.append("')");
			fps.stmt.executeUpdate(request.toString());
			fps.answer = name_SeqID + SEP + arg[0];
		} else
			fps.answer = "ERROR:Sorcer_Type_Name "
					+ SorcerUtil.firstToken((String) arg[0], SEP) + " Exists.";
	}

	private void renameSorcerName() throws SQLException {
		String[] str = SorcerUtil.tokenize((String) arg[1], SEP);
		StringBuffer request = new StringBuffer("Update ").append(
				Sorcer.tableName("NAME")).append(" set Name ='").append(str[0])
				.append("', Comments='").append(str[1]).append(
						"' where Name_cd=").append((String) arg[0]);
		fps.stmt.executeUpdate(request.toString());
		fps.answer = arg[0] + SEP + arg[1];
	}

	public void setInvoker(Invoker invoker) {
		fps = (SorcerProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return fps;
	}

	public void undoIt() {
		// do nothing
	}
}
/*
 * private void renameSorcerNames() throws SQLException { Hashtable nameIds =
 * (Hashtable)arg[4]; Vector renamedIds = (Vector)arg[3];
 * 
 * StringBuffer updateRequest = new
 * StringBuffer("Update ").append(Const.tableName("NAME"))
 * .append(" set Name=?,Comments=? where Name_cd=?");
 * 
 * Util.debug(this,"renameSorcerNames ::"+updateRequest+"::"+nameIds);
 * PreparedStatement updateNameStmt =
 * fps.dbConnection.prepareStatement(updateRequest.toString()); try { for
 * (Enumeration e = renamedIds.elements();e.hasMoreElements();) { String id =
 * (String)e.nextElement(); String[] str =
 * Util.tokenize((String)nameIds.get(id),GApp.sep);
 * updateNameStmt.setString(1,str[0]); updateNameStmt.setString(2,str[1]);
 * updateNameStmt.setInt(3,Integer.parseInt(id));
 * updateNameStmt.executeUpdate(); }
 * 
 * fps.dbConnection.setAutoCommit(true); fps.dbConnection.commit(); }catch
 * (SQLException ex) { ex.printStackTrace(); fps.dbConnection.rollback();
 * fps.dbConnection.setAutoCommit(true); throw ex; } }
 */

