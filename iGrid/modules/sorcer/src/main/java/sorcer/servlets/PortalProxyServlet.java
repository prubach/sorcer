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

package sorcer.servlets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.rmi.Naming;
import java.util.Hashtable;
import java.util.Vector;

import javax.security.cert.X509Certificate;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.context.ControlContext;
import sorcer.service.Context;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.util.Commander;
import sorcer.util.Mandate;
import sorcer.util.Result;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

/**
 * The PortalProxyServlet provides access to the SORCER ApplicationServlet and
 * ServiceServlet
 */
public class PortalProxyServlet extends HttpServlet implements SorcerConstants {
	protected ServiceProxyDispatcher dispatcher;
	protected Commander controller;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String isDebug = getInitParameter("isDebugged");

		// Get the location of the RMI registry.
		String location = Sorcer.getRmiUrl();
		String name;
		try {
			// Now get the remote ServiceServer object
			name = location + Sorcer.getProperty("sorcer.jobberServer");
			dispatcher = (ServiceProxyDispatcher) Naming.lookup(name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Now get the remote ApplicationServer object
			name = location + Sorcer.getProperty("sorcer.commanderServer");
			controller = (Commander) Naming.lookup(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String method = req.getMethod(), redirectURL = null;
		String qs = req.getQueryString();
		// aspect is oriented on a commanad "cmd" parameter
		// in the query string or in header body
		String aspect = null;

		// Util.debug(this, "service:method=" + method);
		// Util.debug(this, "service:queryString=" + qs);

		// Get the cert's serial number
		X509Certificate cert = getCertificate(req);
		if (cert != null) {
			// to be implemented
			String serialNumber = cert.getSerialNumber().toString();
		}

		Hashtable params = null;
		if (qs != null)
			params = HttpUtils.parseQueryString(qs);
		if (qs != null && qs.startsWith("cmd")) {
			aspect = getFirstParameter("cmd", params);
			// Util.debug(this, "aspect=" + aspect);
			if (method.equals("POST")) {
				if (aspect.equals(Integer.toString(DO_TASK)))
					processTask(req, res);
				else if (aspect.equals(Integer.toString(DO_JOB)))
					processJob(req, res);
				else if (aspect.equals(Integer.toString(EXEC_MANDATE)))
					processMandate(req, res);
				else
					processCmd(aspect, req, res);

				return;
			}
		}

		// still cmd might be in the body from POST request
		if (aspect == null) {
			aspect = req.getParameter("cmd");
		}
		if (aspect == null) {
			String str = req.getPathInfo();
			if (str != null && str.length() != 0) {
				String[] tokens = SorcerUtil.tokenize(str, "/");
				if (tokens[0].equals("cmd"))
					aspect = tokens[1];
			}
		}

		if (aspect != null) {
			processCmd(aspect, req, res);
			return;
		} else
			sendFailure(res.getOutputStream(),
					"No command specified for this servlet");
	}

	protected void processTask(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		ObjectInputStream in = new ObjectInputStream(req.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(res.getOutputStream());

		try {
			// Read in task from the applet
			Serializable[] args = (Serializable[]) in.readObject();
			// Write the result
			out
					.writeObject(dispatcher.service((ServiceExertion) args[0],
							null));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Close the I/O streams
		in.close();
		out.close();
	}

	protected void processJob(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		ObjectInputStream in = new ObjectInputStream(req.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(res.getOutputStream());

		try {
			// Read in job from the applet client
			Serializable[] args = (Serializable[]) in.readObject();
			Job job = (Job) args[0];
			ControlContext cc = (ControlContext) job.getContext();
			if (cc.isMonitored()) {
				// notify o MASTER task completion
				Vector recipents = null;
				String notifyees = cc.getNotifyList(job.getMasterExertion());
				// Util.debug(this, "notifyTaskExecution:notifyees=" +
				// notifyees);
				if (notifyees != null) {
					String[] list = SorcerUtil.tokenize(notifyees, MAIL_SEP);
					recipents = new Vector(list.length);
					for (int i = 0; i < list.length; i++)
						recipents.addElement(list[i]);
				}
				String to = "", admin = Sorcer.getProperty("sorcer.admin");

				if (recipents == null) {
					if (admin != null) {
						recipents = new Vector();
						recipents.addElement(admin);
					}
				} else if (!recipents.contains(admin) && admin != null)
					recipents.addElement(admin);

				if (recipents == null)
					to = to
							+ "No e-mail notifications will be sent for this job.";

				else {
					to = to + "e-mail notification will be sent to<BR>";
					for (int i = 0; i < recipents.size(); i++)
						to = to + "  " + recipents.elementAt(i) + "<BR>";
				}
				if (job.getMasterExertion() != null)
					Contexts.putOutValue(((ServiceExertion) job
							.getMasterExertion()).getContext(),
							Context.JOB_COMMENTS, "Your job <H2>"
									+ job.getName()
									+ "</H2>has been submitted.<BR>" + to);

				out.writeObject(job);
				dispatcher.service(job, null);
			} else {
				out.writeObject(dispatcher.service(job, null));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// Close the I/O streams
		in.close();
		out.close();
	}

	protected void processMandate(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		ObjectInputStream in = new ObjectInputStream(req.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(resp.getOutputStream());
		try {
			// Read in parameters from the applet
			Serializable[] args = (Serializable[]) in.readObject();
			Mandate mandate = (Mandate) args[0];

			// Execute the client mandate
			mandate = dispatcher.execMandate(mandate);

			// Write the result
			out.writeObject(mandate);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Close the I/O streams
		in.close();
		out.close();
	}

	protected void processCmd(String cmd, HttpServletRequest req,
			HttpServletResponse res) {
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			try {
				in = new ObjectInputStream(req.getInputStream());
				out = new ObjectOutputStream(res.getOutputStream());
				Serializable[] args = (Serializable[]) in.readObject();
				Result result = controller.execCmd(Integer.parseInt(cmd), args);
				// Write the result
				out.writeObject(result);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException ce) {
				ce.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			// Close the I/O streams
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private String getFirstParameter(String parameter, Hashtable table) {
		Object obj = table.get(parameter);
		if (obj != null && obj instanceof String[])
			return ((String[]) obj)[0];
		else
			return null;
	}

	private void sendFailure(ServletOutputStream out, String reason)
			throws IOException {
		out.println("<HTML><HEAD>Command Execution Failure</HEAD><BODY>");
		out.println("<h2>The execution failed, due to:</h2>");
		out.println("<b>" + reason + "</b>");
		out.println("<P>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}

	/**
	 * Return the browser's certificate as an attribute and cast it correctly
	 */
	protected X509Certificate getCertificate(HttpServletRequest request) {
		return (X509Certificate) request
				.getAttribute("javax.servlet.request.X509Certificate");
	}

	/**
	 * Jakarta doesn't like doing redirects as a non-standard HTTPS, so build
	 * the full URL correctly
	 */
	protected String getRedirectURL(HttpServletRequest request, String url) {
		StringBuffer result = new StringBuffer("https://");
		result.append(request.getServerName());
		result.append(":");
		result.append(request.getServerPort());
		result.append(url);

		return result.toString();
	}
}
