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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import sorcer.core.SorcerConstants;
import sorcer.util.SorcerUtil;
import sorcer.util.dbac.SocketProtocol;

/**
 * The class FileUploadHandler is a generic file upload and storage servlet. The
 * functionality of the servlet can be easily extendedd is subclasses. The
 * servlet allows files to be uploaded to a directory defined by
 * bapp.dbas.UploadCmd. This servlet is intended to be called via a special form
 * page created by its doGet method.
 */
public class FileUploadHandler extends FileUploadServlet implements
		SorcerConstants {
	// isDebugged and isClassAccess flags are set from a servlet properties
	// or application launcher propeties file
	static private boolean isDebugged = true, isClassAccess = false;
	// can be used by other servlets
	public static SocketProtocol protocol;
	// uploadTarget = "fileUploadWindow" for a separate window
	protected String uploadTarget = "dirDocs", bgColor = "C0C0C0", mimeTypes;

	/**
	 * Specifies login and password to the application server that makes
	 * application specific database updates.
	 */
	protected String asLogin, asPassword;

	/**
	 * Specifies a URL for notification applet that allow to notify interested
	 * parties about the uploaded file
	 */
	protected String notifyURL, docWorksheetURL, versionWorksheetURL,
			draftWorksheetURL, reviewWorksheetURL, attachmentWorksheetURL;

	/**
	 * Specifies file storage directory, where the file will be stored by GApp.
	 * If null, the servlet is considered unavailable, and an error will be
	 * returned to the client.
	 */
	protected File uploadDirectory = null;

	/**
	 * Specifies a size and maximum size of uploadable file. By default, this
	 * value is 20MB.
	 */
	protected long fileSize;

	/**
	 * Initialize this servlet by reading an applicaton default properties file
	 * and/or parameters from the servlets.properties file. Parameters under
	 * considerations are: appProperties, mimeTypes, baseDir, archiveDir,
	 * asPort, asHost, maxSize. If the applicaton default properties file is not
	 * available all parameters should be specified in the servlets.properties
	 * file.
	 */
	public void init(ServletConfig sc) {
		super.init(sc);

		// First get the application default properties file name. The file
		// defines upload directory (baseDir and archiveDir) and database
		// conectivity
		// parameters (host and port) used by an underlying application.
		String appProperties = sc.getInitParameter("appProperties");
		if (appProperties == null)
			System.err.println("Can not find application properties");

		mimeTypes = sc.getInitParameter("mimeTypes");
		if (mimeTypes == null)
			System.err.println("Can not find MIME types file");

		String baseDir = sc.getInitParameter("baseDir");
		String archiveDir = sc.getInitParameter("archiveDir");
		String max = sc.getInitParameter("maxSize");
		String asPort = sc.getInitParameter("applicationServerPort");
		String asHost = sc.getInitParameter("applicationServerPortHost");
		String debugged = sc.getInitParameter("isDebugged");
		if (debugged == null)
			debugged = "false";
		String classAccess = sc.getInitParameter("isClassAccess");
		if (classAccess == null)
			classAccess = "false";
		notifyURL = sc.getInitParameter("launcher.notify.url");
		docWorksheetURL = sc.getInitParameter("launcher.docWorksheet.url");
		versionWorksheetURL = sc
				.getInitParameter("launcher.versionWorksheet.url");
		draftWorksheetURL = sc.getInitParameter("launcher.draftWorksheet.url");
		reviewWorksheetURL = sc
				.getInitParameter("launcher.reviewWorksheet.url");
		attachmentWorksheetURL = sc
				.getInitParameter("launcher.attachmentWorksheet.url");

		asLogin = sc.getInitParameter("asLogin");
		if (asLogin == null)
			asLogin = "servlet";
		asPassword = sc.getInitParameter("asPassword");

		// Read the application properties if available
		Properties props = new Properties();
		if (appProperties != null) {
			try {
				props.load(new FileInputStream(appProperties));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		// Ovewrite the servlet properties if application properties
		// are available
		max = props.getProperty("uploadedFile.maxSize", max);
		try {
			maxSize = 1024 * 1024 * Integer.parseInt(max);
		} catch (Exception e) {
			// Do nothing, leave at default value
		}

		notifyURL = props.getProperty("launcher.notify.url", notifyURL);
		docWorksheetURL = props.getProperty("launcher.docWorksheet.url",
				docWorksheetURL);
		versionWorksheetURL = props.getProperty(
				"launcher.versionWorksheet.url", versionWorksheetURL);
		draftWorksheetURL = props.getProperty("launcher.draftWorksheet.url",
				draftWorksheetURL);
		reviewWorksheetURL = props.getProperty("launcher.reviewWorksheet.url",
				reviewWorksheetURL);
		attachmentWorksheetURL = props.getProperty(
				"launcher.attachmentWorksheet.url", attachmentWorksheetURL);

		debugged = props.getProperty("fileUpload.isDebugged", debugged);
		isDebugged = debugged.equals("true");

		classAccess = props.getProperty("launcher.classAccess", classAccess);
		isClassAccess = classAccess.equals("true");

		baseDir = props.getProperty("launcher.baseDir", baseDir);
		archiveDir = props.getProperty("launcher.archiveDir", archiveDir);
		try {
			baseDir.replace('/', File.separatorChar);
			archiveDir.replace('/', File.separatorChar);
			if (!baseDir.endsWith(File.separator)) {
				baseDir += File.separator;
			}
		} catch (Exception e) {
			// Leave set at null, will disable servlet.
			return;
		}
		uploadDirectory = new File(baseDir + archiveDir);
		debug(this, "uploadDirectory: " + uploadDirectory);

		FileUploadServlet.writeInFile = true;

		if (uploadDirectory != null && !uploadDirectory.exists()
				&& !uploadDirectory.mkdirs()) {
			// Since we couldn't create the temporary directory,
			// even though it doesn't exist, disable the servlet.
			uploadDirectory = null;
		}

		asPort = props.getProperty("applicationServer.port", asPort);
		asHost = props.getProperty("applicationServer.host", asHost);
		createProtocol(asPort, asHost);
	}

	/**
	 * Create the database connection for this servlet
	 */
	protected void createProtocol(String asPort, String asHost) {
		int portNum = 0;
		try {
			portNum = new Integer(asPort).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		protocol = new SocketProtocol(asHost, portNum, true);
		protocol.connect();
		if (protocol.connected()) {
			protocol.login(asLogin, asPassword);
		} else {
			System.err
					.println("GApp file upload could not connect to database");
			return;
		}
	}

	// Executed when servlet is called with no form (no multipart form data)
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		// Get the variables passed in through the query string
		ServletOutputStream out = res.getOutputStream();
		String[] reqParams = getQueryStringParams(req, out);

		// Print out the upload form depending on formType or send failure
		// report
		// if reqParams is null failure was already sent
		if (reqParams != null) {
			if (reqParams[FORM_TYPE].equals("uploadDoc")
					|| reqParams[FORM_TYPE].equals("uploadReview")
					|| reqParams[FORM_TYPE].equals("uploadAttachment")
					|| reqParams[FORM_TYPE].equals("uploadDraft")) {
				// Print out common elements
				logCommonSection(req, res, reqParams, out);
				// print out form-specific elements
				if (reqParams[FORM_TYPE].equals("uploadDraft"))
					logUploadDraftSection(reqParams, out);
				else if (reqParams[FORM_TYPE].equals("uploadDoc")
						|| reqParams[FORM_TYPE].equals("uploadAttachment")
						|| reqParams[FORM_TYPE].equals("uploadReview")) {
					logUploadDocSection(reqParams, out);
				}
			} else
				sendFailure(out, "Not supported upload form type "
						+ reqParams[FORM_TYPE]);
		}

		out.close();
	}

	/**
	 * Get parameters from a query string requesting a form for diferrent types
	 * of file upload.
	 */
	protected String[] getQueryStringParams(HttpServletRequest req,
			ServletOutputStream out) throws IOException {
		// Get the variables passed in through the query string
		Hashtable table;
		String[] reqParams = new String[6];

		if (req.getQueryString() != null) {
			table = HttpUtils.parseQueryString(req.getQueryString());

			// Get the form type being requested
			// valid types are uploadDoc, uploadReview, and uploadDraft
			reqParams[FORM_TYPE] = getParamValue("formType", table);
			reqParams[USER_NAME] = getParamValue("userName", table);
			reqParams[CONTEXT_ID] = getParamValue("contextOID", table);
			reqParams[MODIFIER] = getParamValue("modifier", table);
			reqParams[SUBDIR] = getParamValue("subdir", table);
			reqParams[IS_CHAINING] = getParamValue("chaining", table);
		}
		// verify authorization
		String[] pack = new String[2];
		pack[0] = reqParams[CONTEXT_ID];
		pack[1] = reqParams[USER_NAME];
		if (!isProtocolAlive(out))
			return null;
		Vector reply = protocol.executeDefault(AUTHORIZE_UPLOAD, pack);
		if (isError(reply, out))
			return null;
		String msg = (String) reply.elementAt(0);
		if (!msg.equals(OK))
			sendFailure(out, msg);

		return reqParams;
	}

	/**
	 * Write a common section of upload form for all form types
	 */
	protected void logCommonSection(HttpServletRequest req,
			HttpServletResponse res, String[] reqParams, ServletOutputStream out)
			throws IOException {
		// Print out the form
		res.setHeader("Expires", "Tue, 08 Oct 1996 08:00:00 GMT");
		res.setHeader("Content-type", "text/html");

		// Print out common elements
		out.println("<html>");
		logInputVerification(reqParams, out);
		out.println("<body BGCOLOR='" + bgColor + "'>");
		out.println("<form action=" + HttpUtils.getRequestURL(req).toString());
		out.println("      enctype='multipart/form-data'");
		out.println("      target='" + uploadTarget + "'");
		out.println("      method='POST'>");
		logHiddenItem("formType", reqParams[FORM_TYPE], out);
		logHiddenItem("userName", reqParams[USER_NAME], out);
		logHiddenItem("chaining", reqParams[IS_CHAINING], out);
	}

	/**
	 * Write section of upload form for files uploaded that are not associated
	 * with any document.
	 */
	protected void logUploadDraftSection(String[] reqParams,
			ServletOutputStream out) throws IOException {
		out.println("<h1>File Upload</h1>");
		out.println("The File you are uploading will not yet be "
				+ "associated with any Document.<br>");
		out
				.println("This function is only recommended if you need to share information"
						+ "qickly,<br>without a version and review control.<br>");
		out
				.println("You can associate this File with a Document later.<br><br>");
		logHiddenItem("contextOID", reqParams[CONTEXT_ID], out);
		logHiddenItem("modifier", reqParams[MODIFIER], out);
		logHiddenItem("subdir", reqParams[SUBDIR], out);
		logFileInput(out);
		logFilenameInput(out);
		logDocDescription(out);
		if (mimeTypes != null)
			logMimeTypes(out);

		if (isClassAccess)
			logClassAccessSelector(out);

		out.println("<INPUT TYPE=button VALUE='Send' "
				+ "ONCLICK='verifyData(document.forms[0])'>");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	/**
	 * Write a section of upload form for files uploaded that are reviews or
	 * associated with an existing database document.
	 */
	protected void logUploadDocSection(String[] reqParams,
			ServletOutputStream out) throws IOException {
		out.println("<h1>File Upload</h1>");
		out.println("The File you are uploading will be associated "
				+ "with the selected Document.<br><br>");
		logHiddenItem("contextOID", reqParams[CONTEXT_ID], out);
		logHiddenItem("modifier", reqParams[MODIFIER], out);
		logHiddenItem("subdir", reqParams[SUBDIR], out);
		logFileInput(out);
		logDocDescription(out);

		if (mimeTypes != null)
			logMimeTypes(out);

		if (isClassAccess && reqParams[FORM_TYPE].equals("uploadReview"))
			logClassAccessSelector(out);

		out.println("<INPUT TYPE=button VALUE='Send' "
				+ "ONCLICK='verifyData(document.forms[0])'>");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	/**
	 * Basic method for file upload that processes an upload form parameters and
	 * based on its formType requests relevant processing including database
	 * updates.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		Hashtable table;
		ServletOutputStream out = res.getOutputStream();
		// check if the application server connection is alive
		if (!isProtocolAlive(out))
			return;

		String boundary = validateUpload(req, out);
		if (boundary == null || boundary.length() == 0)
			return;
		try {
			table = parseMulti(boundary, req.getInputStream(), uploadDirectory
					.getPath());
		} catch (Throwable t) {
			t.printStackTrace(new PrintWriter(out));
			return;
		}
		// Get info on uploaded file
		Object obj = table.get("fileinput");
		if (obj == null || !(obj instanceof Hashtable)) {
			sendFailure(out,
					"Did not upload a file.  There may an error in the form.");
			return;
		}

		// get uploaded file and form parameters, some of them are hidden,
		// in the upload form provided by doGet
		String[] reqParams = getUploadFormParms(req, table);
		// for testing the upload form input only
		// logFormInfo(table);
		boolean isRightForm = true;
		String accessName = null;
		if (reqParams[FORM_TYPE].equals("uploadDoc"))
			accessName = uploadFile(UPLOAD_DOC, out, reqParams);
		else if (reqParams[FORM_TYPE].equals("uploadReview"))
			accessName = uploadFile(UPLOAD_REVIEW, out, reqParams);
		else if (reqParams[FORM_TYPE].equals("uploadAttachment"))
			accessName = uploadFile(UPLOAD_ATTACH, out, reqParams);
		else if (reqParams[FORM_TYPE].equals("uploadDraft"))
			accessName = uploadFile(UPLOAD_DRAFT, out, reqParams);
		else
			isRightForm = false;

		if (isRightForm && accessName != null) {
			try {
				saveFile((Hashtable) obj, accessName);
			} catch (IOException e) {
				sendFailure(out, "Uploaded file write error");
			}
			logResponse(res, out, reqParams);
			return;
		}
		// if not done, an error is sent already
		else if (accessName == null)
			return;
		else
			logFormInfo(table, reqParams, out);

		out.close();
	}

	private boolean isProtocolAlive(ServletOutputStream out) throws IOException {
		int sleepTime = 1000;
		if (!protocol.isAlive()) {
			protocol.disconnect();
			protocol.connect();
			try {
				while (!protocol.connected()) {
					Thread.sleep(sleepTime);
					protocol.checkConnection();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			protocol.login(asLogin, asPassword);
		}
		return true;
	}

	/**
	 * Performs basic tests for a correct file ulpoad and notifies user about
	 * existing problems.
	 */
	protected String validateUpload(HttpServletRequest req,
			ServletOutputStream out) throws ServletException, IOException {
		String boundary = "";
		if (uploadDirectory == null) {
			sendFailureStandard(out, "uploadDirectory is unset");
			return boundary;
		}
		if (!req.getContentType().toLowerCase().startsWith(
				"multipart/form-data")) {
			// Since this servlet only handles File Upload, bail out
			sendFailure(out, "Wrong content type set for file upload in the "
					+ "main page.  Something is seriously amiss.");
			return boundary;
		}
		if (req.getContentLength() > maxSize) {
			sendFailure(out,
					"Content too long - sent files are limited in size");
			return boundary;
		}
		int ind = req.getContentType().indexOf("boundary=");
		if (ind == -1) {
			sendFailure(out, "boundary is not set");
			return boundary;
		}
		boundary = req.getContentType().substring(ind + 9);
		if (boundary == null) {
			sendFailure(out, "boundary is not set");
		}
		return boundary;

	}

	// get upload form parameters, some of them are hidden, sent by doGet
	protected String[] getUploadFormParms(HttpServletRequest req,
			Hashtable table) {
		Hashtable filehash = (Hashtable) table.get("fileinput");
		String fname = (String) filehash.get("filename");
		String contentType = (String) filehash.get("content-type");

		String[] reqParams;
		if (isClassAccess)
			reqParams = new String[12];
		else
			reqParams = new String[10];

		reqParams[FORM_TYPE] = getParamValue("formType", table);
		reqParams[FILENAME] = getFilename(fname, req.getHeader("User-Agent"));
		reqParams[MODIFIER] = getParamValue("modifier", table);
		if (reqParams[MODIFIER] == null)
			reqParams[MODIFIER] = "";
		reqParams[SUBDIR] = getParamValue("subdir", table);
		if (reqParams[SUBDIR] == null)
			reqParams[SUBDIR] = "";
		else if (reqParams[SUBDIR].indexOf(File.separator) != reqParams[SUBDIR]
				.length() - 1)
			reqParams[SUBDIR] = reqParams[SUBDIR] + File.separator;
		reqParams[MIME_TYPE] = getParamValue("mimeType", table);
		if (reqParams[MIME_TYPE].equals(""))
			reqParams[MIME_TYPE] = contentType;
		if (reqParams[FORM_TYPE].equals("uploadDraft"))
			reqParams[USER_FILENAME] = getParamValue("userFilename", table);
		reqParams[USER_NAME] = getParamValue("userName", table);
		reqParams[CONTEXT_ID] = getParamValue("contextOID", table);
		reqParams[DESCRIPTION] = getParamValue("description", table);
		reqParams[IS_CHAINING] = getParamValue("chaining", table);

		if (isClassAccess) {
			reqParams[CLASS_ACCESS] = getParamValue("classAccess", table);
			reqParams[EXPORT_CONTROL] = getParamValue("exportControl", table);
		}

		return reqParams;
	}

	private String getParamValue(String parameter, Hashtable table) {
		Object obj = table.get(parameter);
		if (obj != null && obj instanceof String[])
			return ((String[]) obj)[0];
		else
			return "";
	}

	private void saveFile(Hashtable filehash, String accessName)
			throws IOException {
		if (accessName.charAt(0) != File.separatorChar)
			accessName = File.separator + accessName;
		debug(this, "saveFile=" + uploadDirectory + accessName);

		if (writeInFile) {
			String ufn = (String) filehash.get("content");
			// debug(this, "saveFile:ufn=" + ufn);
			new File(ufn).renameTo(new File(uploadDirectory + accessName));
		} else {
			BufferedOutputStream fileout = new BufferedOutputStream(
					new FileOutputStream(uploadDirectory + accessName));
			byte[] bytes = (byte[]) filehash.get("content");
			fileout.write(bytes, 0, bytes.length);
			fileout.flush();
			fileout.close();
		}
	}

	protected String uploadFile(int type, ServletOutputStream out,
			String[] reqParams) throws IOException {
		String[] lines = packArguments(reqParams);
		// debug(this, "uploadDoc:lines:" + Util.arrayToString(lines));
		Vector reply = protocol.executeDefault(type, lines);
		if (isError(reply, out))
			return null;
		// String accessName = (String)reply.elementAt(0);
		// debug(this, "uploadFile:accessName: " + accessName);
		return (String) reply.elementAt(0);
	}

	protected void logFormInfo(Hashtable table, String[] reqParams,
			ServletOutputStream out) throws IOException {
		// for testing the upload form input only
		out.println("<HTML><HEAD><TITLE>FileUpload Output");
		out.println("</TITLE></HEAD><BODY BGCOLOR='" + bgColor + "'>");
		logFormData(table, out);
		out.println("You just downloaded a file named: " + reqParams[FILENAME]
				+ "<BR>");
		// out.println("<FORM>");
		// out.println("<INPUT type=button value='OK' onClick='window.close()'>");
		// out.println("</FORM>");
		out.println("</BODY></HTML>");
	}

	protected void logResponse(HttpServletResponse res,
			ServletOutputStream out, String[] reqParams) throws IOException {
		// debug(this, "logResponse:chaining=" + reqParams[IS_CHAINING]);

		if (reqParams[IS_CHAINING].length() != 0) {
			if (reqParams[IS_CHAINING].endsWith("notify"))
				res.sendRedirect(notifyURL);
			else if (reqParams[IS_CHAINING].startsWith("docWorksheet"))
				res.sendRedirect(docWorksheetURL);
			else if (reqParams[IS_CHAINING].startsWith("versionWorksheet"))
				res.sendRedirect(docWorksheetURL);
			else if (reqParams[IS_CHAINING].startsWith("draftWorksheet"))
				res.sendRedirect(draftWorksheetURL);
			else if (reqParams[IS_CHAINING].startsWith("reviewWorksheet"))
				res.sendRedirect(reviewWorksheetURL);
			else if (reqParams[IS_CHAINING].startsWith("attachmentWorksheet"))
				res.sendRedirect(attachmentWorksheetURL);
			return;
		}
		out.println("<html>");
		out.println("<body BGCOLOR='" + bgColor + "'>");
		out.println("Done.<br>");
		out
				.println("The next time you display the worksheet, you will see the changes.<br>");
		// out.println("<form>");
		// out.println("<input type=button value='OK' onClick='window.close()'>");
		// out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

	/**
	 * Obtain information on this servlet.
	 * 
	 * @return String describing this servlet.
	 */
	public String getServletInfo() {
		return "GApp file upload servlet";
	}

	private void sendFailureStandard(ServletOutputStream out, String addtl)
			throws IOException {
		sendFailure(out, "The administrator has either not set up the <BR>"
				+ "servlet, or has configured it incorrectly.<BR>"
				+ "The specific reason for failure is:<BR> " + addtl);
		return;
	}

	private void sendFailure(ServletOutputStream out, String reason)
			throws IOException {
		// out.println("<HTML><HEAD>Upload Failure</HEAD><BODY>");
		out.println("<HTML><BODY BGCOLOR='" + bgColor + "'>");
		out.println("<h2>The upload failed, due to:</h2>");
		out.println("<b>" + reason + "</b>");
		out.println("<P>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}

	public void logInputVerification(String[] reqParams, ServletOutputStream out)
			throws IOException {
		out.println("<head>");
		out.println("<script language='JavaScript'>");
		if (!reqParams[FORM_TYPE].equals("AssignWithDoc")) {
			out.println("function FileNameOK(filename) {");
			out.println("  if (filename.length < 1) {");
			out.println("    alert(\"Filename is empty\")");
			out.println("    return 0");
			out.println("  }");
			out.println("  if (filename.length > 128) {");
			out.println("    alert(\"Filename is too long (> 128 chars)\")");
			out.println("    return 0");
			out.println("  }");
			out.println("  if ((filename.indexOf(\"^\") >= 0) || "
					+ "(filename.indexOf(\"|\") >= 0)) {");
			out.println("    alert(\"Filename cannot contain ^ or |\")");
			out.println("    return 0");
			out.println("  }");
			out.println("  return 1");
			out.println("}");
			out.println("function DescriptionOK(description) {");
			// actual limit is 1024 characters, but we may need to add
			// apostrophes
			out.println("  if (description.length > 1000) {");
			out
					.println("    alert(\"Description is too long (> 1000 chars)\")");
			out.println("    return 0");
			out.println("  }");
			out
					.println("  if ((description.indexOf(\"\\\\\") >= 0) || "
							+ "(description.indexOf(\"^\") >= 0) || (description.indexOf(\"|\") >= 0)) {");
			out
					.println("    alert(\"Description cannot contain a backslash, ^, or |\")");
			out.println("    return 0");
			out.println("  }");
			out.println("  return 1");
			out.println("}");
		}
		out.println("function verifyData(form) {");
		if (!reqParams[FORM_TYPE].equals("AssignWithDoc")) {
			out.println("  if (!FileNameOK(form.fileinput.value)) return 0");
			out
					.println("  if (!DescriptionOK(form.description.value)) return 0");
		}
		out.println("  form.submit()");
		out.println("  return 1");
		out.println("}");
		out.println("</script>");
		out.println("</head>");
	}

	// Determine the filename
	public String getFilename(String inputName, String userAgent) {
		String name;
		if (userAgent.indexOf("Win") > -1) {
			name = inputName.substring(inputName.lastIndexOf("\\") + 1);
		} else {
			name = inputName.substring(inputName.lastIndexOf("/") + 1);
		}
		return name;
	}

	public void logHiddenItem(String itemName, String itemVal,
			ServletOutputStream out) throws IOException {
		out.println("<input type='hidden' name='" + itemName + "' value='"
				+ itemVal + "'>");
	}

	// Display the filename input comment in the form
	protected void logFilenameInput(ServletOutputStream out) throws IOException {
		out
				.println("What is the name you wish to store the file under on the server?<br>");
		out.println("<input type='text' name='userFilename' size=50");
		out.println("<br><br>");
	}

	// Display the file input element in the form
	protected void logFileInput(ServletOutputStream out) throws IOException {
		out.println("File Name:<br>");
		out.println("<input type='file' name='fileinput' size=50>");
		out.println("<br><br>");
	}

	// Display the description element in the form
	protected void logDocDescription(ServletOutputStream out)
			throws IOException {
		out.println("Description:<br>");
		out.println("<textarea name='description' wrap='virtual' "
				+ "rows=3 cols=53></textarea>");
		out.println("<br><br>");
	}

	// Display the mime type selector element in the form
	public void logClassAccessSelector(ServletOutputStream out)
			throws IOException {
		StringBuffer html = new StringBuffer(
				"Access Class: <select name=classAccess>\n").append(
				"<option value='1' selected>1 - public\n").append(
				"<option value='2'>2 - sensitive\n").append(
				"<option value='3'>3 - confidential\n").append(
				"<option value='4'>4 - secret\n").append("</select>\n").append(
				"<input type='checkbox' name='exportControl'>Export Control");

		out.println(html.toString());
		out.println("<br><br>");
	}

	// Display a class access selector including an export control checkbox
	public void logMimeTypes(ServletOutputStream out) throws IOException {
		String line, type;
		// Open a data input stream on the mime.types file for reading
		try {
			FileInputStream fis = new FileInputStream(mimeTypes);

			BufferedReader dis = new BufferedReader(new InputStreamReader(fis));

			// Read the mime.types file, and create the select element as you go
			StringBuffer mimeTypeList = new StringBuffer(
					"<select name=mimeType>\n");
			mimeTypeList
					.append("<option value='' selected>Use default mime type\n");

			while ((line = dis.readLine()) != null) {
				if (!line.startsWith("#") && line.length() != 0) {
					type = SorcerUtil.firstToken(line, " \t");
					mimeTypeList.append("<option value='" + type + "'>" + type
							+ "\n");
				}
				mimeTypeList.append("</select>\n");
			}

			out
					.println("Select a mime type if this file has no extension,<br>");
			out.println("or if you want to override the mime type associated "
					+ "with the extension<br>");
			out.println(mimeTypeList.toString());
			out.println("<br><br>");
		} catch (FileNotFoundException e) {
			System.err.println("file was not found");
			return;
		} catch (IOException e) {
			System.err.println("could not read line");
			System.exit(1);
		}
	}

	private void logFormData(Hashtable table, ServletOutputStream out)
			throws IOException {
		Object obj;
		for (Enumeration fields = table.keys(); fields.hasMoreElements();) {
			String name = (String) fields.nextElement();
			out.println("<b>" + name + ":</b><br>");
			obj = table.get(name);
			if (obj instanceof Hashtable) {
				// its a file!
				Hashtable filehash = (Hashtable) obj;
				out.println("<hr>Filename: " + filehash.get("filename")
						+ "<br>");
				out.println("Content-Type: " + filehash.get("content-type")
						+ "<br>");
				obj = filehash.get("content");
				byte[] bytes = (byte[]) obj;
				out.println("Content size: " + bytes.length);
				out.println();
				out.println("<BR>");
			} else if (obj instanceof String[]) {
				String[] values = (String[]) obj;
				for (int i = 0; i < values.length; i++)
					out.println(values[i] + "<br>");
			}
		}
	}

	/**
	 * Returns arguments for application server gapp.dbas.UploadCmd command
	 */
	protected String[] packArguments(String[] reqParams) {
		String[] lines;
		if (isClassAccess)
			lines = new String[9];
		else
			lines = new String[7];

		lines[FU_FILE] = (reqParams[USER_FILENAME] == null || reqParams[USER_FILENAME]
				.length() == 0) ? reqParams[FILENAME]
				: reqParams[USER_FILENAME];
		lines[FU_DESC] = reqParams[DESCRIPTION];
		lines[FU_CONTEXT_ID] = reqParams[CONTEXT_ID];
		lines[FU_USER] = reqParams[USER_NAME];
		lines[FU_MIME_TYPE] = reqParams[MIME_TYPE];
		lines[FU_MODIFIER] = reqParams[MODIFIER];
		lines[FU_FILE_SIZE] = Long.toString(fileSize);

		if (isClassAccess) {
			lines[FU_CLASS_ACCESS] = reqParams[CLASS_ACCESS];
			lines[FU_EXPORT_CONTROL] = reqParams[EXPORT_CONTROL].equals("") ? "0"
					: "1";
		}

		return lines;
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

	public static void debug(Object caller, String message) {
		if (isDebugged)
			System.out.println(caller.getClass().getName() + ">>" + message);
	}

	/**
	 * Specifies indexes for user query string parameters; FORM_TYPE defines a
	 * style of form for diferrent types of opload (uploadDoc, uploadReview,
	 * uploadDraft); USER_NAME - the current application user; CONTEXT_ID - a
	 * database unique id of a document for a version or folder for a draft to
	 * be associatedd with. The remaining indexes are related to the upload form
	 * parametres. An array reqParams holds this parameters and shares them
	 * across related methodes to provide the thread safe processing.
	 */
	// protected String formType, userName, contextOID;

	static final int FORM_TYPE = 0, USER_NAME = 1, CONTEXT_ID = 2,
			MODIFIER = 3, SUBDIR = 4, IS_CHAINING = 5;
	static final int FILENAME = 6, MIME_TYPE = 7, USER_FILENAME = 8,
			DESCRIPTION = 9, CLASS_ACCESS = 10, EXPORT_CONTROL = 11;
}
