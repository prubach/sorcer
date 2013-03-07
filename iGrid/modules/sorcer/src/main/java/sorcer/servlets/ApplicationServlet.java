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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpUtils;

import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.Commander;
import sorcer.util.Crypt;
import sorcer.util.Result;
import sorcer.util.SorcerUtil;
import sorcer.util.dbas.ApplicationDomain;
import sorcer.util.dbas.Pool;
import sorcer.util.dbas.ServletProtocolConnection;

/**
 * Provides command execution with database connectivity.
 */
public class ApplicationServlet extends RMIServlet implements Commander,
		HttpConstants, SorcerConstants {
	protected static Logger logger = Logger.getLogger(ApplicationServlet.class
			.getName());
	public static ApplicationDomain domain;
	// Handler threads
	static protected Pool pool;
	static private boolean useSSO;
	final static int BUF_SIZE = 2048, ARG_TALLY = 12;
	static protected boolean useSession = true;
	public static HttpSession buffer;
	public static Hashtable sessionMap;
	private static ApplicationServletSessionCleaner cleaner; // = new

	// ApplicationServletSessionCleaner();

	public void init(ServletConfig sc) throws ServletException {
		super.init(sc);
		cleaner = new ApplicationServletSessionCleaner();
		sessionMap = new Hashtable();
		domain = new ApplicationDomain();
		String asDir = sc.getInitParameter("asDir");
		String ap = sc.getInitParameter("appProperties");
		logger.info("-asDir is " + asDir);
		logger.info("ap is " + ap);
		if (asDir == null) {
			System.err.println("Can not find application server properties");
			return;
		}

		String[] args = (ap == null) ? new String[] { "-dir", asDir }
				: new String[] { "-dir", asDir, "-props", ap };
		logger.info("as dir is : " + asDir);
		domain.init(args);

		useSSO = domain.useSSO();

		String poolSize = domain.props.getProperty(
				"applicationServer.poolSize."
						+ ApplicationDomain.appName.toLowerCase(), "5");

		try {
			pool = new Pool(Integer.parseInt(poolSize),
					ServletProtocolConnection.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError(e.getMessage());
		}

		initApplication();
	}

	private void initApplication() {
		try {
			execDefaultCmd(LOAD_GROUPS, new Serializable[] { "" });
			execDefaultCmd(LOAD_ROLES, new Serializable[] { "" });
			execDefaultCmd(LOAD_PERMISSION, new Serializable[] { "" });
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

		logger.info("service:method=" + method + " queryString=" + qs);
		Hashtable params = null;
		int cmd;

		if (useSSO) {
			if (!isSSOAuthenticated(req, res))
				return;
		}

		if (qs != null)
			params = HttpUtils.parseQueryString(qs);

		if (qs != null && qs.startsWith("cmd")) {
			aspect = getFirstParameter("cmd", params);

			// URL connection from client
			if (method.equals("POST")) {
				if (aspect.startsWith("gapp")) {
					processCmd(req, res);
				} else if ((cmd = Integer.parseInt(aspect)) > OBJECT_CMD_START)
					processObjectCmd(cmd, req, res);
				else
					processCmd(aspect, req, res);
				return;
			}
		}
		// IF request is for a file
		else if (qs != null && qs.startsWith("file")) {
			sendSecureFile(req, res, params);
			return;
		}

		// still cmd might be in the body from POST request
		// Try to read the cmd value.
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
		} else if ((aspect = req.getParameter("filename")) != null) {
			getFile(aspect, req, res);
			return;
		} else {
			processCmd(String.valueOf(AS_FIRST), req, res);
			// sendFailure(out, "No command specified for this servlet");
		}
	}

	private void updateResponseHeader(HttpServletRequest req,
			HttpServletResponse res) {
		res.setContentType("text/html");

		// res.setHeader("Pragma","no-cache");
		res.setHeader("Cache-Control", "no-cache");

		if (req.getHeader("User-Agent").indexOf("MSIE") > -1)
			res.setHeader("Expires", "Tue, 08 Oct 1996 08:00:00 GMT");
	}

	protected void readHeaderBody(BufferedReader br, HttpServletResponse res)
			throws ServletException, IOException {
		StringBuffer sb = new StringBuffer();
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				// Util.debug(this, "line=" + line);
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception ex) {
			System.err.println("Error reading HTTP body");
		}

		ServletOutputStream out = res.getOutputStream();
		sendData(out, sb.toString());
	}

	protected void sendSecureFile(HttpServletRequest req,
			HttpServletResponse res, Hashtable params) throws IOException {
		boolean authorized = false;
		// Get Authorization header
		String auth = req.getHeader("Authorization");

		// Do we allow that user?
		SorcerPrincipal principal = getUser(auth);
		if (principal != null && !principal.isExportControl())
			authorized = true;
		else if (getFirstParameter("userID", params) != null
				&& getFirstParameter("nounce", params) != null
				&& getFirstParameter("hash", params) != null) {

			principal = getPrincipalFromUserID(getFirstParameter("userID",
					params));

			if (principal != null && !principal.isExportControl()) {
				String userCredentials = principal.getId()
						+ new String(principal.getPassword())
						+ getFirstParameter("nounce", params);

				String hash = new Crypt().crypt(userCredentials, SEED)
						.toString();

				String clientHash = java.net.URLDecoder
						.decode(getFirstParameter("hash", params));
				authorized = (hash != null && hash.equals(clientHash));

			}
		}

		if (!authorized) {
			PrintWriter out = res.getWriter();
			// Not allowed, so report he's unauthorized
			res.setContentType("text/html");
			res.sendError(res.SC_UNAUTHORIZED);
			res.setHeader("WWW-Authenticate",
					"BASIC realm=\"Secure-File Store\"");
		} else {
			String dir = ApplicationDomain.secureUploadDir.replace('/',
					File.separatorChar);
			String queryPath = getFirstParameter("file", params);
			if (queryPath != null) {
				queryPath = queryPath.substring(queryPath.indexOf('/') + 1)
						.replace('/', File.separatorChar);
			}
			File targ = new File(dir, queryPath);
			boolean OK = printHeaders(targ, req.getRemoteAddr(), res);
			ServletOutputStream outStream = res.getOutputStream();
			if (OK) {
				sendFile(targ, outStream);
			} else {
				send404(targ, outStream);
			}
		}
	}

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

	/**
	 * The basic method for the application servlet that processes the client
	 * requests or commands via URL connection. Requests or commands are issued
	 * by the gapp.dbac.ServletProtocol proxy.
	 */
	protected void processCmd(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		PrintWriter out = res.getWriter();
		PipedWriter pw = new PipedWriter();
		PipedReader pr = new PipedReader(pw);
		BufferedReader pin = new BufferedReader(pr);
		PrintWriter pout = new PrintWriter(pw);
		Hashtable data = new Hashtable();

		// get principal and session and put it in data.
		prepareProcessCmdData(req, data);

		data.put("pout", pout);
		data.put("uri", req.getRequestURI());

		try {
			pool.performWork(data);
			String s;
			// while ((s = pin.readLine())!=null) {
			while (!(s = pin.readLine()).equals("_DONE_")) {
				out.println(s);
			}
			out.println("_DONE_");
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError(e.getMessage());
		} finally {
			pin.close();
			pout.close();
		}
		out.close();
	}

	private void prepareProcessCmdData(HttpServletRequest req, Hashtable data)
			throws IOException {
		BufferedReader in = req.getReader();
		StringBuffer out = new StringBuffer();
		if (out == null)
			out = new StringBuffer();
		SorcerPrincipal principal = null;
		int dcmd = Integer.parseInt(in.readLine());
		out.append(dcmd).append("\n");
		// Util.debug(this,"________________dcmd="+dcmd);

		if (dcmd == LOGIN && useSession) {
			// Util.debug(this, "::prepareProcessCmdData() LOGIN");
			// Get the current session object or create one if necessary
			out.append(in.readLine()).append("\n");
			HttpSession session = req.getSession(true);
			setSession(session);
			cleanup();
			data.put("session", session);
		} else if (dcmd == EXECCMD || dcmd == EXECDEFAULT) {
			// Util.debug(this,
			// "::prepareProcessCmdData() EXECCMD or EXECDEFAULT");
			out.append(in.readLine()).append("\n");

			int no = Integer.parseInt(in.readLine());
			String[] args = new String[no];

			out.append(String.valueOf(no)).append("\n");

			for (int j = 0; j < no; j++) {
				args[j] = in.readLine();
				out.append(args[j]).append("\n");
			}
			principal = (no > 0) ? SorcerPrincipal.fromString(args[no - 1])
					: null;

			// Util.debug(this, "::prepareProcessCmdData() principal:" +
			// principal);

			if (useSSO) {
				// If we use SSO login is never called and the session
				// is never setup and things are broken =)
				HttpSession session = req.getSession(false);
				setSession(session);
				cleanup();
				data.put("session", session);
			}
		}
		// Command other than login,execCmd and execDefaultCmd
		else {
			// Util.debug(this, "::prepareProcessCmdData() OTHER COMMAND.");
			out.append(in.readLine()).append("\n");
			principal = SorcerPrincipal.fromString(in.readLine());

			// Util.debug(this, "::prepareProcessCmdData() principal:" +
			// principal);
		}
		if (principal != null) {
			data.put("principal", principal);
			if (useSession && principal.getSessionID() != null)
				data.put("session", getSession(principal.getSessionID()));
		}

		// Util.debug(this, "___________________data in="+out.toString());
		data.put("in", new BufferedReader(new StringReader(out.toString())));
	}

	/**
	 * The basic method for the application servlet that processes the client
	 * POST or GET requests or commands.
	 */
	protected void processCmd(String cmd, HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		updateResponseHeader(req, res);
		PrintWriter out = res.getWriter();

		StringBuffer sb = new StringBuffer();
		// Util.debug(this,"__________________cmd="+cmd+" sb="+sb.toString()+" req="+req.toString());
		writeProtocolData(cmd, sb, req);

		PipedWriter pw = new PipedWriter();
		PipedReader pr = new PipedReader(pw);
		BufferedReader pin = new BufferedReader(pr);
		PrintWriter pout = new PrintWriter(pw);

		Hashtable data = new Hashtable();
		data.put("cmd", cmd);
		data.put("in", sb.toString());
		data.put("pout", pout);

		// Get the current session object or create one if necessary
		// Some IE VM do not pass on a session id, so we provide a workaround
		if (useSession) {
			HttpSession session = null;
			data.put("session", req.getSession(true));
			if (buffer == null)
				buffer = session;
		}

		try {
			pool.performWork(data);
			String s;
			while (!(s = pin.readLine()).equals("_DONE_")) {
				// Util.debug(this, "processCmd:s=" + s);
				out.println(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError(e.getMessage());
		} finally {
			pin.close();
			pout.close();
		}
		out.close();
	}

	protected void processObjectCmd(int cmd, HttpServletRequest req,
			HttpServletResponse res) throws IOException {

		try {
			ObjectInputStream in = new ObjectInputStream(req.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(res
					.getOutputStream());

			PipedOutputStream pos = new PipedOutputStream();
			PipedInputStream pis = new PipedInputStream(pos);
			ObjectOutputStream oout = new ObjectOutputStream(pos);
			BufferedInputStream bis = new BufferedInputStream(pis);

			Hashtable data = new Hashtable();
			data.put("cmd", new Integer(cmd));
			data.put("oout", oout);
			Object[] args = null;
			;

			try {
				args = (Object[]) in.readObject();
				// if (args[args.length-1] instanceof GAppPrincipal) {
				// GAppPrincipal principal = (GAppPrincipal)objs[objs.length-1];
				// if (sessionMap.get(principal.getSessionId()) == null)

				data.put("objects", args);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}

			if (useSession) {
				HttpSession session = null;
				SorcerPrincipal principal = null;
				if (args[args.length - 1] instanceof SorcerPrincipal)
					principal = (SorcerPrincipal) args[args.length - 1];
				else
					System.err.println(getClass().getName()
							+ "::processObjectCmd() NO PRINCIPAL IN ARGS.");

				if (cmd == LOGIN) {
					session = req.getSession(true);
					setSession(session);
					cleanup();
				} else if (principal != null) {
					data.put("principal", principal);
					session = getSession(principal.getSessionID());
					if (session == null) {
						session = req.getSession(true);
						setSession(session);
						principal.setSessionID(session.getId());
					}
				}
				data.put("session", session);
				if (buffer == null)
					buffer = session;
			}

			try {
				pool.performWork(data);

				Result result = new Result();
				Object o;

				ObjectInputStream ois = new ObjectInputStream(bis);
				while (!(((o = ois.readObject()) instanceof String) && (o
						.toString()).equals("_DONE_")))
					result.addElement(o);

				if (result.size() >= 1 && result.elementAt(0) instanceof Result)
					out.writeObject(result.elementAt(0));
				else
					out.writeObject(result);

				// write items from command
				// if (result.size() > 0)
				//
			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalError(e.getMessage());
			} finally {
				pos.close();
				in.close();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public Result execDefaultCmd(int command, Serializable[] args)
			throws RemoteException {
		// Prepare the data...
		StringBuffer out = new StringBuffer();
		out.append(Integer.toString(EXECDEFAULT)).append("\n");

		out.append(Integer.toString(command)).append("\n");
		if (args != null && args.length >= 1)
			out.append(Integer.toString(args.length)).append("\n");

		for (int j = 0; j < args.length; j++)
			out.append(args[j]).append("\n");

		try {
			PipedWriter pw = new PipedWriter();
			PipedReader pr = new PipedReader(pw);
			BufferedReader pin = new BufferedReader(pr);
			PrintWriter pout = new PrintWriter(pw);

			Hashtable data = new Hashtable();
			data
					.put("in", new BufferedReader(new StringReader(out
							.toString())));
			data.put("pout", pout);
			data.put("objects", args);

			try {
				pool.performWork(data);

				String s;
				Result result = new Result();
				while (!(s = pin.readLine()).equals("_DONE_")) {
					result.addElement(s);
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalError(e.getMessage());
			} finally {
				pin.close();
				pout.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("execCmd failed", e);
		}
	}

	public Result execCmd(int command, Serializable[] args)
			throws RemoteException {
		// Prepare the data...
		StringBuffer out = new StringBuffer();
		out.append(Integer.toString(command)).append("\n");
		if (args != null && args.length > 1)
			out.append(Integer.toString(args.length)).append("\n");

		for (int j = 0; j < args.length; j++)
			out.append(args[j]).append("\n");

		try {
			PipedWriter pw = new PipedWriter();
			PipedReader pr = new PipedReader(pw);
			BufferedReader pin = new BufferedReader(pr);
			PrintWriter pout = new PrintWriter(pw);

			Hashtable data = new Hashtable();
			data
					.put("in", new BufferedReader(new StringReader(out
							.toString())));
			data.put("pout", pout);
			data.put("objects", args);

			try {
				pool.performWork(data);

				String s;
				Result result = new Result();
				while (!(s = pin.readLine()).equals("_DONE_")) {
					result.addElement(s);
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalError(e.getMessage());
			} finally {
				pin.close();
				pout.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("execCmd failed", e);
		}
	}

	/**
	 * Writes a command protocoyl data as required by the
	 * jgapp.dbas.ProtocolConnection class: the first item is a default command
	 * EXECDEFAULT, then the current commnd and the number of its arguments. The
	 * first argument ("method" or "arg0") is the command method followd by the
	 * method arguments.
	 */
	private void writeProtocolData(String cmd, StringBuffer buffer,
			HttpServletRequest req) {
		String arg;
		String[] args = new String[ARG_TALLY];
		// get a method name
		String provider = req.getParameter("provider");
		if (provider == null || provider.length() == 0)
			provider = String.valueOf(EXECDEFAULT);
		// Util.debug(this,"________________________-cmd="+cmd+" buffer="+buffer+" provider="+provider);
		// get a method arguments
		for (int i = 0; i < ARG_TALLY; i++) {
			arg = req.getParameter("arg" + i);
			logger.info("Args are " + arg);
			if (arg != null && arg.length() != 0)
				args[i] = arg;
			else
				args[i] = "null";
		}

		int bottom = ARG_TALLY;
		for (int i = ARG_TALLY - 1; i >= 0; i--) {
			if (!args[i].equals("null"))
				break;
			bottom = i;
		}

		buffer.append(String.valueOf(provider)).append("\n").append(cmd)
				.append("\n").append(bottom).append("\n");
		logger.info("Command cmd is " + cmd);
		for (int i = 0; i <= bottom; i++) {
			buffer.append(args[i]).append("\n");
		}

		/*
		 * Util.debug(this, "writeProtocolData>>provider=" + provider +
		 * "\ncmd= " + cmd + "\n" + "size= " + bottom + "\nargs= " +
		 * Util.arrayToString(args));
		 */
	}

	protected boolean isCustomAuthorized(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();

		// Get Authorization header
		String auth = req.getHeader("Authorization");

		// Do we allow that user?
		SorcerPrincipal principal = getUser(auth);
		if (principal == null) {
			// Not allowed, so report he's unauthorized
			res.setContentType("text/html");
			res.setHeader("WWW-Authenticate",
					"BASIC realm=\"Secure-File Store\"");
			res.sendError(res.SC_UNAUTHORIZED);
			return false;
		} else
			return true;
	}

	protected boolean isSSOAuthenticated(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		// Util.debug(this, "::isSSOAuthenticated() sm-gessouid:" +
		// req.getHeader("sm-gessouid") +
		// " sm-georaclehrid:" + req.getHeader("sm-georaclehrid"));

		String SSOUID = req.getHeader("sm-gessouid");

		// redirect if no id
		if (SSOUID == null) {
			String scheme = req.getScheme();
			String serverName = req.getServerName();
			int p = req.getServerPort();
			String port = (p == 80) ? "" : ":" + p;

			// Util.debug(this, "redirect>>" +scheme + "://" + serverName + port
			// + "/" +
			// domain.appName.toLowerCase() + "/"+
			// domain.getProperty("applicationServer.SSOlogin.url"));

			res.sendRedirect(scheme + "://" + serverName + port + "/"
					+ domain.appName.toLowerCase() + "/"
					+ domain.getProperty("applicationServer.SSOlogin.url"));
			return false;
		}

		// Get the session
		HttpSession session = req.getSession(true);

		// Does the session indicate this user already logged in?
		Object done = session.getValue("logon.isDone"); // marker object
		if (done == null) {
			// No logon.isDone means he hasn't logged in.
			// Save the request URL as the true target and redirect to the login
			// page.
			session.putValue("login.isDone", SSOUID);
		}

		return true;
	}

	protected boolean isAuthenticated(HttpServletRequest req,
			HttpServletResponse res) throws IOException {

		// Get the session
		HttpSession session = req.getSession(true);

		// Does the session indicate this user already logged in?
		Object done = session.getValue("logon.isDone"); // marker object
		if (done == null) {
			// No logon.isDone means he hasn't logged in.
			// Save the request URL as the true target and redirect to the login
			// page.
			session.putValue("login.target", HttpUtils.getRequestURL(req)
					.toString());
			String scheme = req.getScheme();
			String serverName = req.getServerName();
			int p = req.getServerPort();
			String port = (p == 80) ? "" : ":" + p;

			logger.info("redirect>>" + scheme + "://" + serverName + port
					+ domain.appName.toLowerCase() + "/login.html");

			res.sendRedirect(scheme + "://" + serverName + port
					+ domain.appName.toLowerCase() + "/login.html");
			return false;
		}
		// If we get here, the user has logged in and is authorized
		return true;
	}

	protected boolean isValid(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();

		// Get the user's name and password
		String name = req.getParameter("name");
		String passwd = req.getParameter("passwd");

		// Check the name and password for validity
		if (getUser(name + ":" + passwd) != null) {
			out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
			out.println("<BODY>Your login and password are invalid.<BR>");
			out
					.println("You may want to <A HREF=\"/login.html\">try again</A>");
			out.println("</BODY></HTML>");
		} else {
			// Valid login. Make a note in the session object.
			HttpSession session = req.getSession(true);
			session.putValue("logon.isDone", name); // just a marker object

			// Try redirecting the client to the page he first tried to access
			try {
				String target = (String) session.getValue("login.target");
				if (target != null)
					res.sendRedirect(target);
				return true;
			} catch (Exception ignored) {
			}

			// Couldn't redirect to the target. Redirect to the project page.
			String scheme = req.getScheme();
			String serverName = req.getServerName();
			int p = req.getServerPort();
			String port = (p == 80) ? "" : ":" + p;
			res.sendRedirect(scheme + "://" + serverName + port);
		}
		return true;
	}

	/**
	 * Check the user information sent in the Authorization header against the
	 * database of users maintained by the application
	 */
	protected SorcerPrincipal getUser(String auth) throws IOException {
		if (auth == null)
			return null; // no auth

		if (!auth.toUpperCase().startsWith("BASIC "))
			return null; // we only do BASIC

		// Get encoded user and password, comes after "BASIC "
		String userpassEncoded = auth.substring(6);

		// Decode it, using any base 64 decoder
		sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
		String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));

		// Check our user list to see if that user and password are "allowed"
		return verifyUser(userpassDecoded);
	}

	protected SorcerPrincipal verifyUser(String loginPasswd) {
		Result result = null;
		try {
			result = execCmd(LOGIN, new Serializable[] { SorcerUtil.firstToken(
					loginPasswd, ":")
					+ SEP
					+ new Crypt().crypt(SorcerUtil
							.secondToken(loginPasswd, ":"), SEED) });

		} catch (Exception e) {
			e.printStackTrace();
		}
		String user = (String) result.elementAt(0);
		// Util.debug(this,"_________________________"+user.substring(7));
		if (user.startsWith("logged"))
			return SorcerPrincipal.fromString(user.substring(7));
		else
			return null;
	}

	protected SorcerPrincipal getPrincipalFromUserID(String userID) {
		Result result = null;
		try {
			result = execDefaultCmd(GET_GAPP_PRINCIPAL,
					new Serializable[] { userID });
		} catch (Exception e) {
			e.printStackTrace();
		}
		String user = (String) result.elementAt(0);
		// Util.debug(this,"_________________________"+user);
		return SorcerPrincipal.fromString(user);
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

	private String getFirstParameter(String parameter, Hashtable table) {
		Object obj = table.get(parameter);
		if (obj != null && obj instanceof String[])
			return ((String[]) obj)[0];
		else
			return null;
	}

	private void sendData(ServletOutputStream out, String data)
			throws IOException {
		out.println("<HTML><HEAD>Application Servlet Data</HEAD><BODY>");
		out.println("<h2>Application Servlet Data</h2>");
		out.println("<b>" + data + "</b>");
		out.println("</BODY></HTML>");
	}

	private void sendFailure(ServletOutputStream out, String reason)
			throws IOException {
		out.println("<HTML><HEAD>Command Execution Failure</HEAD><BODY>");
		out.println("<h2>The execution failed, due to:</h2>");
		out.println("<b>" + reason + "</b>");
		out.println("<P>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}

	private void sendFailure(PrintWriter out, String reason) throws IOException {
		out.println("<HTML><HEAD>Command Execution Failure</HEAD><BODY>");
		out.println("<h2>The execution failed, due to:</h2>");
		out.println("<b>" + reason + "</b>");
		out.println("<P>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}

	private void sendFailureStandard(ServletOutputStream out, String addtl)
			throws IOException {
		sendFailure(out, "The administrator has either not set up the <BR>"
				+ "servlet, or has configured it incorrectly.<BR>"
				+ "The specific reason for failure is:<BR> " + addtl);
		return;
	}

	/**
	 * Returns true if an error is returned while receiving data from
	 * ApplicationServer
	 */
	protected boolean isError(Vector results, ServletOutputStream out)
			throws IOException {
		return isError(results, null, out);
	}

	/**
	 * Returns true if an error is returned while receiving data from
	 * ApplicationServer and with a provided error message when results are
	 * empty
	 */
	protected boolean isError(Vector results, String message,
			ServletOutputStream out) throws IOException {
		if (results.size() == 1) {
			String str = (String) results.elementAt(0);
			if (str.startsWith("ERROR")) {
				sendFailure(out, str.substring(6));
				return true;
			}
		} else if (message != null && results.size() == 0) {
			sendFailure(out, message);
			return true;
		}
		return false;
	}

	/**
	 * Obtain information on this servlet.
	 * 
	 * @return String describing this servlet.
	 */
	public String getServletInfo() {
		return "Application servlet -- used to execute the Protocol interface";
	}

	public long getLastModified(HttpServletRequest req) {
		return new Date().getTime() / 1000 * 1000;
	}

	void send404(File targ, ServletOutputStream out) throws IOException {
		sendFailure(out, "Not Found<BR><BR>" + "The requested resource:  "
				+ targ.toString() + "  was not found.<BR>");
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
		map.put(".pdf", "application/pdf");
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

	// default first page command
	final static int AS_FIRST = 500;

	public HttpSession getSession(String sessionID) {
		logger.info(getClass().getName() + "::getSession() sessionID:"
				+ sessionID);
		return (sessionID == null) ? null : (HttpSession) sessionMap
				.get(sessionID);
	}

	public void setSession(HttpSession session) {
		sessionMap.put(session.getId(), session);
		session.setAttribute("_cleaner", cleaner);
	}

	public static void cleanup() {
		// cleans up the Invalidated Session
	}

	public class ApplicationServletSessionCleaner implements
			HttpSessionBindingListener {
		ApplicationServlet servlet;

		public void valueBound(HttpSessionBindingEvent hsbe) {
			logger.info(getClass().getName() + "::valueBound() BOINK.");
		}

		public void valueUnbound(HttpSessionBindingEvent hsbe) {
			logger.info(getClass().getName() + "::valueUnbound() BONG.");
			ApplicationServlet.sessionMap.remove(hsbe.getSession().getId());
		}
	}

}
