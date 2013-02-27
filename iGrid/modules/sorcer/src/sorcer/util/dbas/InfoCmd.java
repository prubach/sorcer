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
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import sorcer.core.SorcerConstants;
import sorcer.util.Command;
import sorcer.util.Invoker;
import sorcer.util.Stopwatch;
import sorcer.util.dbas.ApplicationDomain;
import sorcer.util.html.Cell;
import sorcer.util.html.Document;
import sorcer.util.html.Heading;
import sorcer.util.html.Paragraph;
import sorcer.util.html.Table;
import sorcer.util.html.TableRow;

/**
 * InfoCmd is a psedofactory for testing SORCER commands. It implements the
 * jgapp.util.Protocol interface.
 */

public class InfoCmd implements Command, SorcerConstants {
	final static public String context = "info";
	String cmdName;
	String[] args;
	public SorcerProtocolStatement aps;
	private ResultSet result;
	private HttpSession session;

	public InfoCmd(String cmdName) {
		this.cmdName = cmdName;
	}

	public void setArgs(Object target, Object[] args) {
		aps = (SorcerProtocolStatement) target;
		this.args = (String[]) args;
	}

	public void doIt() {
		// session = aps.getSession();
		try {
			switch (Integer.parseInt(cmdName)) {
			case AS_PROPS:
				getServerProperties();
				break;
			case AS_SESSION:
				getSessionInfo();
				break;
			default:
				aps.answer = "ERROR:Invalid cmd: " + cmdName;
			}
		} catch (Exception e) {
			aps.answer = "ERROR:" + e.getMessage();
		}
	}

	public void setInvoker(Invoker invoker) {
		aps = (SorcerProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return aps;
	}

	public void undoIt() {
		// do nothing
	}

	private void getServerProperties() throws SQLException, IOException {
		// create an SQL query
		Stopwatch stopwatch = new Stopwatch();
		Document document = new Document("SORCER Portal Properties");
		document.add(new Heading(1, "SORCER Portal Properties"));

		Table table = new Table();
		table.setBorder(1);

		String name;
		String value;
		TableRow row;
		// Get the application server properties
		Properties props = ApplicationDomain.getProperties();
		Enumeration e = props.propertyNames();
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			value = props.getProperty(name);
			if (value.equals(""))
				value = "(n.a.)";
			row = new TableRow();
			row.add(new Cell(name));
			row.add(new Cell(value));
			table.add(row);
		}

		// Get the system properties
		props = System.getProperties();
		e = props.propertyNames();
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			value = props.getProperty(name);
			if (value.equals(""))
				value = "(n.a.)";
			row = new TableRow();
			row.add(new Cell(name));
			row.add(new Cell(value));
			table.add(row);
		}

		document.add(table);
		document.add(new Paragraph());
		document.add("Total processing time: " + stopwatch.get() + " ms");
		document.print(aps.getWriter());
	}

	private void getSessionInfo() {
		PrintWriter out = aps.getWriter();
		// Increment the hit count for this page. The value is saved
		// in this client's session under the name "snoop.count".
		Integer count = (Integer) session.getValue("snoop.count");
		if (count == null)
			count = new Integer(1);
		else {
			count = new Integer(count.intValue() + 1);
		}
		session.putValue("snoop.count", count);

		out.println("<HTML><HEAD><TITLE>SORCER Session Snoop</TITLE>");
		out
				.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"Sun, 17 Oct 1971 02:00:00 GMT\">");
		// out.println("<META HTTP-EQUIV=\"Pragma\" CONTENT="no-cache">");
		out.println("<META HTTP-EQUIV=\"Cache-Control\" CONTENT=\"no-cache\">");
		out.println("</HEAD><BODY><H1>SORCER Session Snoop</H1>");

		// Display the hit count for this page
		out.println("You've visited this page " + count
				+ ((count.intValue() == 1) ? " time." : " times."));

		out.println("<P>");

		out.println("<H3>Here is your saved session data:</H3>");
		String[] names = session.getValueNames();
		for (int i = 0; i < names.length; i++) {
			out.println(names[i] + ": " + session.getValue(names[i]) + "<BR>");
		}

		out.println("<H3>Here are some vital stats on your session:</H3>");
		out.println("Session id: " + session.getId() + "<BR>");
		out.println("New session: " + session.isNew() + "<BR>");
		out.println("Creation time: " + session.getCreationTime());
		out.println("<I>(" + new java.util.Date(session.getCreationTime())
				+ ")</I><BR>");
		out.println("Last access time: " + session.getLastAccessedTime());
		out.println("<I>(" + new java.util.Date(session.getLastAccessedTime())
				+ ")</I><BR>");

		out.println("<H3>Here are all the current session IDs");
		out.println("and the times they've hit this page:</H3>");
		HttpSessionContext context = session.getSessionContext();
		Enumeration ids = context.getIds();
		while (ids.hasMoreElements()) {
			String id = (String) ids.nextElement();
			out.println(id + ": ");
			HttpSession foreignSession = context.getSession(id);
			Integer foreignCount = (Integer) foreignSession
					.getValue("snoop.count");
			if (foreignCount == null)
				out.println(0);
			else
				out.println(foreignCount.toString());
			out.println("<BR>");
		}

		out.println("</BODY></HTML>");
	}
}
