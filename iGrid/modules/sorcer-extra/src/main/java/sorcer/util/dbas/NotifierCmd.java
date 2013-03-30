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

import java.io.EOFException;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.event.EventMailbox;
import net.jini.event.MailboxRegistration;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerNotifierProtocol;
import sorcer.core.provider.notifier.NotificationRetrievalListenerImpl;
import sorcer.core.provider.notifier.NotificationRetrievalListenerProtocol;
import sorcer.util.DatabaseObject;
import sorcer.util.ProviderAccessor;
import sorcer.util.SorcerUtil;

public class NotifierCmd extends SorcerCmd implements SorcerConstants {
	final static public String context = "sorcerDBCmd";

	public NotifierCmd(String cmdName) {
		super(cmdName);
	}

	public void doIt() {
		// session = aps.getSession();
		// for(int i=0; i< args.length; i++)
		// Util.debug(this, "::doIt() arg i:" + i + " " + args[i]);
		try {
			switch (Integer.parseInt(cmdName)) {
			case REGISTER_FOR_NOTIFICATIONS:
				registerForNotifications();
				break;
			case GET_NOTIFICATIONS_FOR_SESSION:
				getNotificationsForSession();
				break;
			case ADD_JOB_TO_SESSION:
				addJobToSession();
				break;
			case GET_SESSIONS_FOR_USER:
				getSessionsForUser();
				break;
			case GET_JOB_NAME_BY_JOB_ID:
				// getJobNameByJobID();
				break;
			case GET_TASK_NAME_BY_TASK_ID:
				// getTaskNameByTaskID();
				break;
			case GET_NEW_SERVLET_MESSAGES:
				getNewServletMessages();
				break;
			case CLEANUP_SESSION:
				cleanupPreviousServletSession();
				break;
			case DELETE_NOTIFICATIONS:
				deleteNotifications();
				break;
			case DELETE_SESSION:
				deleteSession();
				break;
			default:
				sendError("Invalid cmd: " + cmdName);
			}
		} catch (Exception e) {
			System.out.println(getClass().getName()
					+ "::doIt() caught Exception.");
			e.printStackTrace();
			sendError(SorcerUtil.stackTraceToArray(e)[0]);
		}
	}

	/**
	 * called by doIt() getArgAsString(0) = Session name getArgAsString(1) = the
	 * user id of the user that is being registered getArgAsString(2) =
	 * NOTIFY_FAILURE, WARRNING... list seperated with | getArgAsString(3) = a |
	 * seperated list of job id's.
	 * 
	 **/
	void registerForNotifications() throws ClassNotFoundException,
			RemoteException, LeaseDeniedException, UnknownLeaseException,
			SQLException, EOFException, IOException {
		String sessionID = null;
		// System.out.println(getClass().getName() +
		// "::registerForNotifications() getArgAsString(1):" +
		// getArgAsString(1));
		EventMailbox mbox = ProviderAccessor.getEventMailbox();
		MailboxRegistration mbreg = mbox.register(Long.MAX_VALUE);

		Lease l = mbreg.getLease();
		l.renew(Lease.FOREVER);
		RemoteEventListener rl = mbreg.getListener();

		// now get the sorcer notifier service
		SorcerNotifierProtocol fnp = (SorcerNotifierProtocol) ProviderAccessor
				.getNotifierProvider();
		if (fnp == null) {
			aps.objAnswer = "ERROR";
			return;
		}
		Vector sessionJobs = new Vector();
		if (args.length > 3 + 1) {// + 1 for the GApp Principal that gets passed
			// as an argument for every command.
			String[] tmpSessionJobs = SorcerUtil.tokenize(getArgAsString(3),
					"|");
			for (int po = 0; po < tmpSessionJobs.length; po++)
				sessionJobs.add(new String(tmpSessionJobs[po]));
		}

		// parse out what to register for
		String[] regFor = SorcerUtil.tokenize(getArgAsString(2), "|");
		sessionID = storeSessionToDB(mbreg, getArgAsString(0),
				getArgAsString(1), sessionJobs);

		for (int i = 0; i < regFor.length; i++) {
			Integer regVal = new Integer(regFor[i]);// sanity check
			if ((regVal.intValue() != NOTIFY_EXCEPTION)
					&& (regVal.intValue() != NOTIFY_INFORMATION)
					&& (regVal.intValue() != NOTIFY_FAILURE)
					&& (regVal.intValue() != NOTIFY_WARNING)) {
				// throw new SorcerNotifierException(this +
				// " TRIED TO REGISTER FOR INVALID NOTIFICATION TYPE.");
			} else
				fnp.register(rl, regVal, getArgAsString(1), sessionJobs,
						sessionID);
		}

		setupServletSession(sessionID);

		/*
		 * PrintWriter out = aps.getWriter(); if(out == null)
		 * System.out.println("APS getWriter() = NULL"); out.println(sessionID);
		 */

		aps.objAnswer = sessionID;
	}

	protected void getNewServletMessages() throws RemoteException {
		NotificationRetrievalListenerProtocol nrl = (NotificationRetrievalListenerProtocol) session
				.getAttribute("notifierSession");

		// PrintWriter out = aps.getWriter();
		if (nrl.getMsgCount() > 0) {
			Vector v = nrl.getMsgData();
			aps.objAnswer = v;
			// for(int i = 0; i < v.size(); i++){
			// Vector curr = (Vector) v.elementAt(i);
			// out.println(Util.escapeReturns((String)curr.elementAt(MSG_ID)));
			// }
		} else
			aps.objAnswer = new Vector();// servlet needs a object back or it
		// may hang up
	}

	protected void cleanupPreviousServletSession() {
		MailboxRegistration mbr = (MailboxRegistration) session
				.getAttribute("notifierMailbox");
		try {
			if (mbr != null)
				mbr.disableDelivery();
			session.setAttribute("notifierSessionID", "");
		} catch (RemoteException re) {

		}
	}

	protected void setupServletSession(String sessionID) throws IOException,
			RemoteException, SQLException, ClassNotFoundException {

		MailboxRegistration mbr = getMailboxRegistration(sessionID);
		NotificationRetrievalListenerProtocol nrl = new NotificationRetrievalListenerImpl();

		try {
			mbr.enableDelivery(nrl);
		} catch (NoSuchObjectException nsoe) {
			System.out.println(getClass().getName()
					+ "::setupServletSession() no such mailbox object.");
		}
		session.setAttribute("notifierSession", nrl);
		session.setAttribute("notifierMailbox", mbr);
		session.setAttribute("notifierSessionID", sessionID);

	}

	/**************************************************
	 * 
	 * getArgAsString(0] = session id that matched sequence id in the
	 * fip_session db table
	 * 
	 ************************************/
	protected void getNotificationsForSession() throws IOException,
			SQLException, ClassNotFoundException, InterruptedException {
		String sessionSeqID = getArgAsString(0);
		// System.out.println(getClass().getName() +
		// "::getNotificationsForSession() sessionSeqID:" + sessionSeqID);
		// if the session is already there dont bother.
		if (!sessionSeqID.equals(session.getAttribute("notifierSessionID"))) {
			cleanupPreviousServletSession();
			setupServletSession(sessionSeqID);
		}

		/*
		 * NotificationRetriever msgGetter = new NotificationRetriever(mbr);
		 * 
		 * 
		 * msgGetter.start();
		 * 
		 * //for(int i = 0; i < 9; i++) // Thread.sleep(250);//sleep 2 secs to
		 * get messages //this doesnt really cut it theres got to be a better
		 * way holddown timer or something
		 */

		Vector allMsgs = getAllMessagesFromDB(sessionSeqID);
		/*
		 * for(int i = 0; i < allMsgs.size(); i++){ Vector curr = (Vector)
		 * allMsgs.elementAt(i); System.out.println(getClass().getName() +
		 * "::getNotificationsForSession() msgs["+ i + "]: " + " MSG_ID : " +
		 * (String) curr.elementAt(MSG_ID) + " MSG_TYPE : " + (String)
		 * curr.elementAt(MSG_TYPE) + " JOB_ID : " + (String)
		 * curr.elementAt(JOB_ID) + " TASK_ID : " + (String)
		 * curr.elementAt(TASK_ID) + " MSG_CONTENT : " + (String)
		 * curr.elementAt(MSG_CONTENT) + " MSG_SOURCE : " + (String)
		 * curr.elementAt(MSG_SOURCE) + " JOB_NAME : " + (String)
		 * curr.elementAt(JOB_NAME) + " TASK_NAME : " + (String)
		 * curr.elementAt(TASK_NAME) + " CREATION_TIME : " + (String)
		 * curr.elementAt(CREATION_TIME) + " IS_NEW : " + (String)
		 * curr.elementAt(IS_NEW)); }
		 */
		/*
		 * disableMailbox(mbr);//here or thread stopping?? Vector newMsgs =
		 * msgGetter.getListener().getMsgData();
		 * 
		 * 
		 * for(int i = 0; i < allMsgs.size(); i++){ Vector curr = (Vector)
		 * allMsgs.elementAt(i); System.out.println(getClass().getName() +
		 * "::getNotificationsForSession() msgs["+ i + "]: " + " MSG_ID : " +
		 * (String) curr.elementAt(MSG_ID) + " MSG_TYPE : " + (String)
		 * curr.elementAt(MSG_TYPE) + " JOB_ID : " + (String)
		 * curr.elementAt(JOB_ID) + " TASK_ID : " + (String)
		 * curr.elementAt(TASK_ID) + " MSG_CONTENT : " + (String)
		 * curr.elementAt(MSG_CONTENT) + " MSG_SOURCE : " + (String)
		 * curr.elementAt(MSG_SOURCE) + " JOB_NAME : " + (String)
		 * curr.elementAt(JOB_NAME) + " TASK_NAME : " + (String)
		 * curr.elementAt(TASK_NAME) + " CREATION_TIME : " + (String)
		 * curr.elementAt(CREATION_TIME) + " IS_NEW : " + (String)
		 * curr.elementAt(IS_NEW)); }
		 * 
		 * allMsgs = setNewMessages(allMsgs, newMsgs);
		 * 
		 * 
		 * //instead of printwriter this should be written out as a vector
		 */

		// PrintWriter out = aps.getWriter();
		/*
		 * for(int i = 0; i < allMsgs.size(); i++){ Vector curr = (Vector)
		 * allMsgs.elementAt(i);
		 * out.println(Util.escapeReturns((String)curr.elementAt(MSG_ID)) + "|"
		 * + Util.escapeReturns((String) curr.elementAt(MSG_TYPE)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(JOB_ID)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(TASK_ID)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(MSG_CONTENT)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(MSG_SOURCE)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(JOB_NAME)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(TASK_NAME)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(CREATION_TIME)) + "|" +
		 * Util.escapeReturns((String) curr.elementAt(IS_NEW))); }
		 */

		Vector results = new Vector();
		for (int i = 0; i < allMsgs.size(); i++) {
			Vector curr = (Vector) allMsgs.elementAt(i);
			results.addElement((String) curr.elementAt(MSG_ID) + "|"
					+ (String) curr.elementAt(MSG_TYPE) + "|"
					+ (String) curr.elementAt(JOB_ID) + "|"
					+ (String) curr.elementAt(TASK_ID) + "|"
					+ (String) curr.elementAt(MSG_CONTENT) + "|"
					+ (String) curr.elementAt(MSG_SOURCE) + "|"
					+ (String) curr.elementAt(JOB_NAME) + "|"
					+ (String) curr.elementAt(TASK_NAME) + "|"
					+ (String) curr.elementAt(CREATION_TIME) + "|"
					+ (String) curr.elementAt(IS_NEW));
		}
		aps.objAnswer = results;
	}

	// flag the new messages form the event mailbox as new
	protected Vector setNewMessages(Vector all, Vector newMsgs) {
		Vector currMsg;
		Vector newMsg;

		for (int i = 0; i < newMsgs.size(); i++) {
			newMsg = (Vector) newMsgs.elementAt(i);
			currMsg = getMessageByID(all, (String) newMsg.elementAt(MSG_ID));
			currMsg.setElementAt(TRUE, IS_NEW);
		}
		return all;
	}

	protected void disableMailbox(MailboxRegistration mailbox)
			throws RemoteException {
		try {
			mailbox.disableDelivery();
		} catch (java.rmi.NoSuchObjectException nsoe) {
			logger
					.info("::disableMailbox(): Mailbox could not be disabled. No Such Object");
		}
	}

	protected Vector getMessageByID(Vector msgs, String msgID) {
		Vector msg;
		for (int i = 0; i < msgs.size(); i++) {
			msg = (Vector) msgs.elementAt(i);
			if (msgID.equals(msg.elementAt(MSG_ID)))
				return msg;
		}
		logger.info("::getMessageByID() RETURNING NULL msgID:" + msgID);
		return null;
	}

	/*
	 * protected void fillJobTaskNames(Vector msgs) throws SQLException { Vector
	 * currMsg; for(int i=0; i < msgs.size(); i++){ currMsg = (Vector)
	 * msgs.elementAt(i); Util.debug(this,
	 * "::fillJobTaskNames() curr msg size():" + currMsg.size());
	 * currMsg.setElementAt
	 * (getJobNameByJobID((String)currMsg.elementAt(JOB_ID)), JOB_NAME);
	 * currMsg.
	 * setElementAt(getTaskNameByTaskID((String)currMsg.elementAt(TASK_ID)),
	 * TASK_NAME); } }
	 */

	protected Vector getAllMessagesFromDB(String sessionID) throws SQLException {
		Vector msgs = new Vector();
		ResultSet rs = null;
		Statement stmt = aps.stmt;

		StringBuffer query = new StringBuffer("SELECT ")
				.append(
						"M.Message_Seq_Id, M.Msg_Type, M.Job_Seq_Id, M.Eng_Task_Seq_Id, ")
				.append("M.Content, M.Source, ")
				.append("J.Name AS JName, T.Name AS TName, ")
				.append(
						"TO_CHAR(M.Creation_Date, 'MM/DD HH:MI:SS') AS Creation_Time ")
				.append("FROM ").append(
						"FIP_MESSAGE M, FIP_JOB J, FIP_ENG_TASK T ").append(
						"WHERE ").append("M.Session_Seq_Id = ").append(
						sessionID).append(" AND ").append(
						"M.Job_Seq_Id = J.Job_Seq_Id ").append(" AND ").append(
						"M.Eng_Task_Seq_Id = T.Eng_Task_Seq_Id");
		/*
		 * StringBuffer query = new StringBuffer("SELECT ").append(
		 * "MESSAGE_SEQ_ID, MSG_TYPE, CONTENT, JOB_SEQ_ID, ENG_TASK_SEQ_ID, SOURCE "
		 * ). append("FROM FIP_MESSAGE WHERE SESSION_SEQ_ID =").
		 * append(sessionID);
		 */
		logger.info("::getAllMessagesFromDB() query:" + query.toString());
		rs = stmt.executeQuery(query.toString());

		while (rs.next()) {
			Vector v = new Vector();
			v.insertElementAt(rs.getString("MESSAGE_SEQ_ID"), MSG_ID);
			v.insertElementAt(rs.getString("MSG_TYPE"), MSG_TYPE);
			v.insertElementAt(rs.getString("JOB_SEQ_ID"), JOB_ID);
			v.insertElementAt(rs.getString("ENG_TASK_SEQ_ID"), TASK_ID);
			v.insertElementAt(rs.getString("CONTENT"), MSG_CONTENT);
			v.insertElementAt(rs.getString("SOURCE"), MSG_SOURCE);
			v.insertElementAt(rs.getString("JName"), JOB_NAME);
			v.insertElementAt(rs.getString("TName"), TASK_NAME);
			v.insertElementAt(rs.getString("Creation_Time"), CREATION_TIME);
			v.insertElementAt(FALSE, IS_NEW);
			msgs.addElement(v);
		}
		return msgs;// (Vector)stream.readObject();
	}

	/**
	 * 
	 * getArgAsString(0] = user id of the person to get the session list for
	 * 
	 ***/
	protected void getSessionsForUser() throws SQLException {
		ResultSet rs = null;
		Statement stmt = aps.stmt;
		String userID = getArgAsString(0);

		StringBuffer query = new StringBuffer(
				"SELECT Session_Name, Session_Seq_Id FROM FIP_SESSION WHERE ")
				.append("User_Seq_Id =").append(userID);

		logger.info("::getSessionsForUser() query:" + query.toString());
		rs = stmt.executeQuery(query.toString());

		// PrintWriter out = aps.getWriter();
		// while(rs.next())
		// out.println(rs.getString("Session_Name") +"|" +
		// rs.getString("Session_Seq_Id"));

		Vector results = new Vector();
		while (rs.next())
			results.addElement(rs.getString("Session_Name") + "|"
					+ rs.getString("Session_Seq_Id"));
		aps.objAnswer = results;
	}

	/****
	 * getArgAsString(0] = user id of session owner getArgAsString(1] = |
	 * seperated list of jobs getArgAsString(2] = | seperated list of
	 * notification types to add job to getArgAsString(3] = session id stored in
	 * the sorcer launcher getArgAsString(4] = | seperated list of job names
	 **/
	protected void addJobToSession() throws ClassNotFoundException,
			RemoteException, SQLException {
		SorcerNotifierProtocol fnp = (SorcerNotifierProtocol) ProviderAccessor
				.getNotifierProvider();
		String[] jobs = SorcerUtil.tokenize(getArgAsString(1), "|");
		String[] regFor = SorcerUtil.tokenize(getArgAsString(2), "|");
		String[] jobNames = SorcerUtil.tokenize(getArgAsString(4), SEP);

		for (int i = 0; i < jobs.length; i++) {
			for (int x = 0; x < regFor.length; x++) {
				Integer regVal = new Integer(regFor[x]);
				// System.out.println(getClass().getName() +
				// "::addJobToSession() length:" +
				// regFor.length + " regFor[]:" + regFor[x]);
				fnp.appendJobToSession(getArgAsString(0), jobs[i], regVal
						.intValue(), getArgAsString(3));
			}
			storeJobForSession(getArgAsString(3), jobs[i]);
			updateSessionName(jobNames[i], getArgAsString(3));
		}
	}

	protected void updateSessionName(String jobName, String sessionID)
			throws SQLException {
		ResultSet rs = null;
		Statement stmt = aps.stmt;

		/*
		 * StringBuffer query = new
		 * StringBuffer("UPDATE FIP_SESSION SET Session_Name = ").
		 * append("(SELECT Session_Name, '"). append(jobName).
		 * append(" ' FROM FIP_SESSION WHERE Session_Seq_Id = ").
		 * append(sessionID). append(" )"). append(" WHERE Session_Seq_Id = ").
		 * append(sessionID);
		 */
		StringBuffer query = new StringBuffer("SELECT Session_Name ").append(
				"FROM FIP_SESSION WHERE Session_Seq_Id = ").append(sessionID);

		logger.info("::updateSessionName() query:" + query.toString());
		rs = stmt.executeQuery(query.toString());
		rs.next();
		String name = rs.getString("Session_Name");

		try {
			query = new StringBuffer("UPDATE FIP_SESSION SET Session_Name = '")
					.append(name).append(" ").append(jobName).append(
							"' WHERE Session_Seq_Id = ").append(sessionID);
			logger.info("::updateSessionName() query:" + query.toString());
			rs = stmt.executeQuery(query.toString());
		} catch (SQLException sqle) {
			// problem when name is > 64 chars
		}
	}

	/***
	 * 
	 * args[0] == vector of ids of notificatiosn to delete
	 * 
	 ***/
	protected void deleteNotifications() throws ClassNotFoundException,
			RemoteException, SQLException {
		Vector ids = (Vector) args[0];
		for (int i = 0; i < ids.size(); i++) {
			logger.info("::deleteNotifications() deleting id:"
					+ ids.elementAt(i));
			deleteNotification((String) ids.elementAt(i));
		}
	}

	protected void deleteNotification(String id) throws SQLException {
		ResultSet rs = null;
		String query = new String(
				"DELETE FROM FIP_MESSAGE WHERE MESSAGE_SEQ_ID=" + id);

		try {
			logger.info("::deleteNotification() query:" + query);
			rs = aps.stmt.executeQuery(query.toString());
		} catch (SQLException sqle) {
			System.out.println(getClass().getName()
					+ "::deleteNotifications() caught SQLException.");
			sqle.printStackTrace();
			throw sqle;
		}
	}

	protected String getSeqIDForSession(String sessionName, String ownerSeqID)
			throws SQLException {
		ResultSet rs = null;
		Statement stmt = aps.stmt;
		String sessionSeqID = null;

		StringBuffer query = new StringBuffer(
				"SELECT Session_Seq_Id FROM FIP_SESSION WHERE ").append(
				"Session_Name ='").append(sessionName).append(
				"' AND User_Seq_Id ='").append(ownerSeqID).append("'");

		rs = stmt.executeQuery(query.toString());
		sessionSeqID = rs.getString("Session_Seq_Id");

		return sessionSeqID;
	}

	protected String storeSessionToDB(MailboxRegistration mbreg,
			String sessionName, String ownerSeqID, Vector jobIDs)
			throws SQLException, EOFException, IOException {

		String sessionSeqID = null; //fix it
		// System.out.println(getClass().getName() +
		// "::storeSessionToDB() got sessionSeqID:" + sessionSeqID);

		Statement stmt = aps.stmt;
		int res = -1;

		try {
			aps.dbConnection.setAutoCommit(true);
			// now add the jobs
			for (int i = 0; i < jobIDs.size(); i++)
				storeJobForSession(sessionSeqID, (String) jobIDs.elementAt(i));

			// use sorcer.util.DatabaseObject to store the mailbox reg *object*
			// into the database
			DatabaseObject dbo = new DatabaseObject();
			dbo.writeObject(mbreg, "FIP_SESSION", "REGISTRATION_OBJECT",
					sessionSeqID, "SESSION_SEQ_ID", aps.dbConnection);

			aps.dbConnection.commit();
			aps.dbConnection.setAutoCommit(true);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			try {
				rollback(sqle);
			} catch (SQLException sqle2) {
				sqle2.printStackTrace();
				throw sqle2;
			}
		}
		return sessionSeqID;
	}

	/****
	 * 
	 * getArgAsString(0] == job id of whose name we want no longer used by
	 * client side
	 ****/
	protected String getJobNameByJobID(String jobID) throws SQLException {
		// System.out.println(getClass().getName() + "::getJobNameByJobID()");
		ResultSet rs = null;
		Statement stmt = aps.stmt;
		try {
			StringBuffer query = new StringBuffer(
					"SELECT Name FROM FIP_JOB WHERE Job_Seq_Id =");
			query.append(jobID);

			// System.out.println(getClass().getName() +
			// "::getJobNameByJobID() QUERY: " + query.toString());
			rs = stmt.executeQuery(query.toString());

			// parse resultset into a string
			/*
			 * PrintWriter out = aps.getWriter(); while(rs.next())
			 * out.println(rs.getString("Name"));
			 */
			rs.next();
			return rs.getString("Name");
		} catch (SQLException sqle) {
			System.out.println(getClass().getName()
					+ "::getJobNameByJobID() caught SQLException.");
			sqle.printStackTrace();
			throw sqle;
		}
	}

	/****
	 * 
	 * getArgAsString(0] == task id of whose name we want
	 * 
	 ****/
	protected String getTaskNameByTaskID(String taskID) throws SQLException {
		try {
			ResultSet rs = null;
			Statement stmt = aps.stmt;
			StringBuffer query = new StringBuffer(
					"SELECT Name FROM FIP_ENG_TASK WHERE Eng_Task_Seq_Id =")
					.append(taskID);
			// Util.debug(this, "::getTaskNameByTaskID() QUERY: " +
			// query.toString());
			rs = stmt.executeQuery(query.toString());

			// parse resultset into a string
			/*
			 * PrintWriter out = aps.getWriter(); while(rs.next()){ String s =
			 * rs.getString("Name"); //Util.debug(this,
			 * "::getTaskNameByTaskID() taskID:" + getArgAsString(0] +
			 * " task name:" + s); out.println(s);
			 * //out.println(rs.getString("Name")); }
			 */

			rs.next();
			return rs.getString("Name");
		} catch (SQLException sqle) {
			System.out.println(getClass().getName()
					+ "::getTaskNameByTaskID() caught SQLException.");
			sqle.printStackTrace();
			throw sqle;
		}
	}

	protected void storeJobForSession(String sessionSeqID, String jobID) {
		// System.out.println(getClass().getName() +
		// "::storeJobForSession() sessionSeqID:" + sessionSeqID + " jobID:" +
		// jobID);
		StringBuffer query = new StringBuffer();
		Statement stmt = aps.stmt;
		int res = -1;

		try {
			query = new StringBuffer(
					"INSERT INTO FIP_SESSION_JOB (Session_Seq_Id, Job_Seq_Id) ");
			query.append("VALUES (");
			query.append(sessionSeqID);
			query.append(", ");
			query.append(jobID);
			query.append(")");
			// System.out.println(getClass().getName() +
			// "::storeJobForSession() query:" + query.toString());
			res = stmt.executeUpdate(query.toString());
			// System.out.println(getClass().getName() +
			// "::storeJobForSession() inserted " + res + " records.");
		} catch (SQLException sqle) {
			// System.out.println(getClass().getName() +
			// "::storeJobForSession() caught SQLException.");
			// sqle.printStackTrace();
			// sqle.getSQLState();
		}
	}

	String getNextSeqID(String sequenceName) throws SQLException {
		// get teh next value of a sequence
		String seqNextVal = null;
		;
		ResultSet rs = null;
		String query = new String("SELECT " + sequenceName
				+ ".NEXTVAL FROM DUAL");

		try {
			// System.out.println(getClass().getName() +
			// "::getNextSeqID() query:" + query);
			rs = aps.stmt.executeQuery(query.toString());

			rs.next();
			seqNextVal = rs.getString(1);
			// System.out.println(getClass().getName() +
			// "::getNextSeqID() seqNextVal: " + seqNextVal);

		} catch (SQLException sqle) {
			System.out.println(getClass().getName()
					+ "::getNextSeqID() caught SQLException.");
			sqle.printStackTrace();
			throw sqle;
		}
		return seqNextVal;
	}

	protected MailboxRegistration getMailboxRegistration(String sessionSeqID)
			throws IOException, SQLException, ClassNotFoundException {
		logger.info("::getMailboxRegistration() sessionSeqID:" + sessionSeqID);
		DatabaseObject dbo = new DatabaseObject();

		return (MailboxRegistration) dbo.readObject("FIP_SESSION",
				"REGISTRATION_OBJECT", sessionSeqID, "SESSION_SEQ_ID",
				aps.dbConnection);

	}

	protected void rollback(SQLException ex) throws SQLException {
		if (aps.dbConnection != null) {
			System.err.println("Transaction is being rolled back");
			aps.dbConnection.setAutoCommit(true);
			aps.dbConnection.rollback();
		}
		// allow the client to know about it
		throw ex;
	}

	protected void deleteSession() throws IOException, SQLException,
			ClassNotFoundException, InterruptedException {
		String id = getArgAsString(0);

		try {
			String query = new String(
					"DELETE FROM FIP_MESSAGE WHERE SESSION_SEQ_ID=" + id);
			aps.stmt.executeQuery(query.toString());

			query = new String(
					"DELETE FROM FIP_SESSION_JOB WHERE SESSION_SEQ_ID=" + id);
			aps.stmt.executeQuery(query.toString());

			query = new String("DELETE FROM FIP_SESSION WHERE SESSION_SEQ_ID="
					+ id);
			logger.info("::deleteSession() query:" + query);
			aps.stmt.executeQuery(query.toString());

		} catch (SQLException sqle) {
			System.out.println(getClass().getName()
					+ "::deleteSession() caught SQLException.");
			sqle.printStackTrace();
			throw sqle;
		}

	}

	private class NotificationRetriever extends Thread {
		NotificationRetrievalListenerProtocol nrl = null;
		MailboxRegistration mailbox;

		public NotificationRetriever(MailboxRegistration mbr)
				throws RemoteException {
			setPriority(Thread.MAX_PRIORITY);
			nrl = new NotificationRetrievalListenerImpl();
			mailbox = mbr;
		}

		public NotificationRetrievalListenerProtocol getListener() {
			return nrl;
		}

		public void run() {
			try {
				mailbox.enableDelivery(nrl);
			} catch (java.rmi.NoSuchObjectException nsoe) {
				logger.info("::run(): No mailbox available. No Such Object");
			} catch (RemoteException re) {
				logger.info("::run(): Unhandled Rmote Exception.");
			}
		}
		// when the thread is stopped does it disable delivery or the creator?
	}
}
