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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.core.Cataloger;
import sorcer.core.SorcerConstants;
import sorcer.core.dispatch.ExertionDispatcher;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ExecState;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.util.AccessorException;
import sorcer.util.Commander;
import sorcer.util.Mandate;
import sorcer.util.Mandator;
import sorcer.util.ProviderAccessor;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;
import sorcer.util.dbas.ApplicationDomain;
import sorcer.util.dbas.RuntimeJobCmd;
import sorcer.util.dbas.ServiceCmd;

/**
 * The ServiceServlet provides access to the SORCER service catalog
 */
public class ServiceServlet extends RMIServlet implements
		ServiceProxyDispatcher, SorcerConstants, HttpConstants {
	/**
	 * The server configuration
	 */
	public static ApplicationDomain domain;

	final static int BUF_SIZE = 2048, ARG_TALLY = 12;

	private static Cataloger cataloger;

	private Mandator persister;

	protected Commander controller;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String asDir = config.getInitParameter("asDir");
		String ap = config.getInitParameter("appProperties");

		// Util.debugy(this, "asDir=" + asDir);
		// Util.debug(this, "appProperties=" + ap);
		// asDir = "/projects/sorcer/wcted/src/dbas";
		if (asDir == null) {
			System.err.println("Can not find application server properties");
			return;
		}

		if (ApplicationServlet.domain == null) {
			domain = new ApplicationDomain();
			String[] args = (ap == null) ? new String[] { "-dir", asDir }
					: new String[] { "-dir", asDir, "-props", ap };
			domain.init(args);
			// domain.processArgs(args);
			// domain.loadProperties();
			// domain.initQueryTable();
			// domain.initialize();
		} else
			domain = ApplicationServlet.domain;

		/*
		 * String[] args = (ap == null) ? new String[] {"-dir", asDir} : new
		 * String[] {"-dir", asDir, "-props", ap}; domain.init(args);
		 * domain.processArgs(args); domain.loadProperties();
		 * domain.initQueryTable(); domain.initialize();
		 */

		// Get the location of the RMI registry.
		String location = Sorcer.getRmiUrl();
		String name;
		try {

			/*
			 * This code is deprecated. mtl // Now get the remote catalog object
			 * name = location + Env.getProperty("sorcer.catalogServer");
			 * Util.debug(this, "catalog=" + name); cataloger =
			 * (Cataloger)Naming.lookup(name); Catalog.setCatalog(cataloger);
			 */

			System.out.println(getClass().getName()
					+ "::init() before cataloger:" + cataloger);
			cataloger = ProviderAccessor.getCataloger();
			System.out.println(getClass().getName()
					+ "::init() after cataloger:" + cataloger);
			// Catalog.setCatalog(cataloger);

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

	/**
	 * Returns the name under which the servlet should be bound in the registry.
	 * By default the name is the servlet's class name. This can be overridden
	 * with the <tt>registryName</tt> init parameter.
	 * 
	 * @return the name under which the servlet should be bound in the registry
	 */
	protected String getRegistryName() {
		// First name choice is the "registryName" init parameter
		String name = getInitParameter("registryName");
		if (name != null)
			return name;
		else
			// return a default SORCER environment value
			name = Sorcer.getProperty("sorcer.jobberServer");

		// Fallback choice is the name of this class
		if (name != null)
			return name;
		else
			return this.getClass().getName();
	}

	/**
	 * Returns the port where the registry should be running. By default the
	 * port is the default registry port (1099). This can be overridden with the
	 * <tt>registryPort</tt> init parameter.
	 * 
	 * @return the port for the registry
	 */
	protected int getRegistryPort() {
		// First port choice is the "registryPort" init parameter
		try {
			return Integer.parseInt(getInitParameter("registryPort"));
		}

		// Fallback choice is the default registry port
		catch (NumberFormatException e) {
			return Integer.parseInt(Sorcer.getRmiPort());
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

		Hashtable params = null;
		String sessionId = null;

		if (qs != null) {
			params = HttpUtils.parseQueryString(qs);
			sessionId = getFirstParameter("sessionId", params);
		}

		if (qs != null && qs.startsWith("cmd")) {
			aspect = getFirstParameter("cmd", params);
			// Util.debug(this, "aspect=" + aspect);

			if (method.equals("POST")) {
				if (aspect.equals(Integer.toString(DO_TASK))
						|| aspect.equals(Integer.toString(DO_JOB)))
					processExertion(req, res, aspect, sessionId);
				else if (aspect.equals(Integer.toString(EXEC_MANDATE))) {
					try {
						processMandate(req, res, sessionId);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
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

		if ((aspect = req.getParameter("filename")) != null) {
			getFile(aspect, req, res);
		} else
			sendFailure(res.getOutputStream(),
					"No command specified for this servlet");
	}

	protected void processExertion(HttpServletRequest req,
			HttpServletResponse res, String aspect, String sessionId)
			throws IOException {
		ObjectInputStream in = new ObjectInputStream(req.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(res.getOutputStream());
		Mandate mandate = new Mandate(Integer.parseInt(aspect));
		Mandate result = null;
		try {
			// Read in exertion from the applet
			mandate.setArgs((Serializable[]) in.readObject());
			result = execMandate(mandate);
			// convert to thin
			convertRemoteToThin(result);
			// Write the result
			out.writeObject(result);
			// Close the I/O streams
		} catch (Throwable t) {
			t.printStackTrace();
		}
		in.close();
		out.close();
	}

	protected void processMandate(HttpServletRequest req,
			HttpServletResponse resp, String sessionId)
			throws ServletException, IOException {
		ObjectInputStream in = new ObjectInputStream(req.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(resp.getOutputStream());
		Mandate mandate = null;
		Mandate result = null;
		try {
			mandate = (Mandate) ((Object[]) in.readObject())[0];
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			result = new Mandate(mandate.getCommandID());
			result.getResult().setStatus(ExecState.FAILED);
			result.getResult().addElement(
					"ERROR:ClassNotFoundException " + e.getMessage());
		}
		// Read in parameters from the applet
		try {
			result = execMandate(mandate);
			// convert to thin
			convertRemoteToThin(result);
		} catch (RemoteException re) {
			re.printStackTrace();
			result = new Mandate(mandate.getCommandID());
			result.getResult().setStatus(ExecState.FAILED);
			result.getResult().addElement(
					"ERROR:RemoteException while processing Mandate!");
		} catch (ContextException ce) {
			ce.printStackTrace();
			result = new Mandate(mandate.getCommandID());
			result.getResult().setStatus(ExecState.FAILED);
			result.getResult().addElement(
					"ERROR:ContextException " + ce.getMessage());
		}

		// Write the result
		out.writeObject(result);
		// Close the I/O streams
		in.close();
		out.close();
		// _________________REVISIT
		// persistACL(mandate);
		// _________________REVISIT
	}

	public Mandate execMandate(Mandate mandate) throws RemoteException {

		Mandate result = null;

		try {
			if (mandate == null)
				return null;
			int cmd = mandate.getCommandID();
			if (cmd == STOP_JOB || cmd == SUSPEND_JOB || cmd == RESUME_JOB
					|| cmd == STEP_JOB || cmd == DO_JOB || cmd == DO_TASK) {
				ServiceCmd scmd = new ServiceCmd(Integer.toString(cmd),
						new Serializable[] { mandate });
				scmd.doIt();
				result = scmd.getResult();
			} else if (cmd == GET_RUNTIME_JOBNAMES || cmd == GET_RUNTIME_JOB) {

				RuntimeJobCmd rjcmd = new RuntimeJobCmd(Integer.toString(cmd),
						new Serializable[] { mandate });
				rjcmd.doIt();
				result = rjcmd.getResult();
			} else
				result = persister.execMandate(mandate);
			return result;
			// }
		} catch (Exception ce) {
			System.out.println(getClass().getName()
					+ "::execMandate() caught Exception. retrying.");
			ce.printStackTrace();
			return result;
		}
	}

	private Exertion service(int serviceCmd, Serializable[] args)
			throws RemoteException, ExertionException {
		Mandate mandate = new Mandate(serviceCmd);
		mandate.setArgs(args);
		Mandate result = execMandate(mandate);
		if (result == null || result.getResult() == null
				|| result.getResult().getStatus() < 0)
			// || !(result.getResult().elementAt(0) instanceof RemoteExertion))
			return null;
		else
			return (Exertion) result.getResult().elementAt(0);
	}

	public Exertion service(Exertion exertion, Transaction txn)
			throws RemoteException, TransactionException, ExertionException {
		return service(exertion);
	}

	public Exertion service(Exertion exertion) throws RemoteException,
			ExertionException {
		return service(SERVICE_EXERTION, new Serializable[] { exertion });
	}

	public Exertion stopJob(String jobID, Subject subject)
			throws RemoteException, ExertionException {
		return service(STOP_JOB, new Serializable[] { jobID, subject });
	}

	public Exertion stopTask(String taskID, Subject subject)
			throws RemoteException, ExertionException {
		return service(STOP_TASK, new Serializable[] { taskID, subject });
	}

	public Exertion suspendJob(String jobID, Subject subject)
			throws RemoteException, ExertionException {
		return service(SUSPEND_JOB, new Serializable[] { jobID, subject });
	}

	public Exertion resumeJob(String jobID, Subject subject)
			throws RemoteException, ExertionException, SignatureException {
		return service(RESUME_JOB, new Serializable[] { jobID, subject });
	}

	public Exertion stepJob(String jobID, Subject subject)
			throws RemoteException, ExertionException, SignatureException {
		return service(STEP_JOB, new Serializable[] { jobID, subject });
	}

	public Exertion dropExertion(Exertion exertion) throws RemoteException,
			ExertionException {
		return service(DROP_EXERTION, new Serializable[] { exertion });
	}

	private void convertRemoteToThin(Mandate mandate) throws ContextException {
		if (mandate != null) {
			for (int i = 0; i < mandate.getResult().size(); i++)
				if (mandate.getResult().elementAt(i) instanceof ServiceExertion)
					mandate.getResult()
							.setElementAt(
									((ServiceExertion) mandate.getResult()
											.elementAt(i)),
									i);
		}
	}

	private String getDataURL(String filename) {
		return ApplicationDomain.getProperty("applicationServlet.dataURL")
				+ filename;
	}

	private String getDataFilename(String filename) {
		return ApplicationDomain.getProperty("applicationServlet.baseDir")
				+ "/"
				+ ApplicationDomain.getProperty("applicationServlet.dataDir")
				+ "/" + filename;
	}

	protected void persistACL(Mandate mandate) throws RemoteException {
		if (mandate.getArgs().length != 4)
			return;
		if (mandate.getArgs()[3] instanceof Hashtable) {
			Serializable[] args = mandate.getArgs();
			// acl has HAshtable dataStructure.
			if (args[3] instanceof Hashtable) {
				if ((mandate.getCommandID() == PERSIST_JOB || mandate
						.getCommandID() == UPDATE_JOB)
						&& mandate.getResult().elementAt(0) instanceof Job) {
					Job job = (Job) mandate.getResult().elementAt(0);
					((Hashtable) args[3]).put("object", job.getName() + SEP
							+ "ServiceJob" + SEP + job.getId());
					Serializable[] aclArgs = { args[3] };
					controller.execCmd(ACL_CMD, aclArgs);
				}
			}
		}
	}

	/*
	 * public ID createID(String type, HttpServletRequest req) { // create
	 * unique identifier from current time String identifier =
	 * Long.toHexString(new Date().getTime()) ; return new
	 * ID(req.getRemoteAddr(),req.getServerPort(),type,identifier); }
	 */

	protected void getFile(String filename, HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		String remoteAddr = remoteAddr = req.getRemoteAddr();
		ServletOutputStream out = res.getOutputStream();
		String dir, inDir = req.getParameter("dir");

		String fname = filename.replace('/', File.separatorChar);
		if (fname.startsWith(File.separator))
			fname = fname.substring(1);

		dir = domain.asDir;
		if (inDir != null)
			dir = inDir;

		File targ = new File(dir, fname);
		if (targ.isDirectory()) {
			File ind = new File(targ, "index.html");
			if (ind.exists()) {
				targ = ind;
			}
		}
		boolean OK = printHeaders(targ, remoteAddr, res);
		if (OK) {
			sendFile(targ, out);
		} else {
			send404(targ, out);
		}
	}

	private String getFirstParameter(String parameter, Hashtable table) {
		Object obj = table.get(parameter);
		if (obj != null && obj instanceof String[])
			return ((String[]) obj)[0];
		else
			return null;
	}

	void sendFile(File targ, ServletOutputStream out) throws IOException {
		InputStream is = null;
		byte[] buf = new byte[BUF_SIZE];
		if (targ.isDirectory()) {
			listDirectory(targ, out);
			return;
		} else {
			is = new FileInputStream(targ.getAbsolutePath());
		}

		try {
			int n;
			while ((n = is.read(buf)) > 0) {
				out.write(buf, 0, n);
			}
		} finally {
			is.close();
		}
	}

	boolean printHeaders(File targ, String remoteAddr, HttpServletResponse res)
			throws IOException {
		boolean ret = false;
		int rCode = 0;
		if (!targ.exists()) {
			rCode = HTTP_NOT_FOUND;
			ret = false;
		} else {
			rCode = HTTP_OK;
			ret = true;
		}
		domain.log("From " + remoteAddr + ": GET " + targ.getAbsolutePath()
				+ "-->" + rCode);
		res.setHeader("Server", "ApplicationServlet: " + domain.appName);
		res.setHeader("Date", new Date().toString());
		if (ret) {
			if (!targ.isDirectory()) {
				res.setHeader("Content-length", Long.toString(targ.length()));
				res.setHeader("Last Modified", new Date(targ.lastModified())
						.toString());
				String name = targ.getName();
				int ind = name.lastIndexOf('.');
				String ct = null;
				if (ind > 0) {
					ct = (String) map.get(name.substring(ind));
				}
				if (ct == null) {
					ct = "unknown/unknown";
				}
				res.setHeader("Content-type", ct);
			} else
				res.setHeader("Content-type", "text/html");
		}
		return ret;
	}

	void send404(File targ, ServletOutputStream out) throws IOException {
		sendFailure(out, "Not Found<BR><BR>" + "The requested resource:  "
				+ targ.toString() + "  was not found.<BR>");
	}

	void listDirectory(File directory, ServletOutputStream out)
			throws IOException {
		String dir = ".";
		if (directory.getName().equals(".."))
			dir = new File(dir).getParent();
		else
			dir = directory.toString();

		out.println("<TITLE>Directory listing</TITLE><P>\n");
		out.println("<A HREF=\"appServlet?dir=" + dir
				+ "&filename=..\">Parent Directory</A><BR>\n");
		String[] list = directory.list();
		for (int i = 0; list != null && i < list.length; i++) {
			File f = new File(directory, list[i]);
			if (f.isDirectory()) {
				out.println("<A HREF=\"appServlet?dir=" + directory
						+ "&filename=" + list[i] + "/\">" + list[i]
						+ "/</A><BR>");
				// out.println(list[i]+"<BR>");
			} else {
				out.println("<A HREF=\"appServlet?filename=" + list[i] + "\">"
						+ list[i] + "</A><BR>");
			}
		}
		out.println("<P><HR><BR><I>" + (new Date()) + "</I>");
	}

	private void sendFailure(ServletOutputStream out, String reason)
			throws IOException {
		out.println("<HTML><HEAD>Command Execution Failure</HEAD><BODY>");
		out.println("<h2>The execution failed, due to:</h2>");
		out.println("<b>" + reason + "</b>");
		out.println("<P>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}

	/* mapping of file extensions to content-types */
	static java.util.Hashtable map = new java.util.Hashtable();

	static {
		fillMap();
	}

	static void fillMap() {
		map.put("", "content/unknown");
		map.put(".uu", "application/octet-stream");
		map.put(".exe", "application/octet-stream");
		map.put(".ps", "application/postscript");
		map.put(".zip", "application/zip");
		map.put(".sh", "application/x-shar");
		map.put(".tar", "application/x-tar");
		map.put(".snd", "audio/basic");
		map.put(".au", "audio/basic");
		map.put(".wav", "audio/x-wav");
		map.put(".gif", "image/gif");
		map.put(".jpg", "image/jpeg");
		map.put(".jpeg", "image/jpeg");
		map.put(".htm", "text/html");
		map.put(".html", "text/html");
		map.put(".text", "text/html");
		map.put(".c", "text/plain");
		map.put(".cc", "text/plain");
		map.put(".c++", "text/plain");
		map.put(".h", "text/plain");
		map.put(".pl", "text/plain");
		map.put(".txt", "text/plain");
		map.put(".java", "text/html");
		map.put(".mdb", "application/vnd.ms-access");
		map.put(".doc", "application/msword");
		map.put(".xls", "application/vnd.ms-excel");
		map.put(".ppt", "application/vnd.ms-powerpoint");
	}

	// default first page command
	final static int AS_FIRST = 500;

	public boolean isAuthorized(Subject sobject, String serviceType,
			String providerName) {
		return true;
	}

	public String getName() throws RemoteException {
		return "ServiceServlet";
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Servicer#getProviderName()
	 */
	@Override
	public String getProviderName() throws RemoteException {
		return getClass().getName();
	}

}
