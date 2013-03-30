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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import sorcer.core.SorcerConstants;
import sorcer.util.Command;
import sorcer.util.Invoker;
import sorcer.util.dbas.ApplicationDomain;
import sorcer.util.dbas.ServletProtocolConnection;

/**
 * SorcerCmd is a generic SORCER pseudo-factory tha implements multiple
 * commands. This class should be subclssed by domain specific pseudo-factories
 * (commands).
 */
public class SorcerCmd implements Command, SorcerConstants {
	protected static Logger logger = Logger
			.getLogger(SorcerCmd.class.getName());
	String cmdName;
	Object[] args;
	public SorcerProtocolStatement aps;
	protected ResultSet result;
	protected HttpSession session;
	protected PrintWriter pw;
	static public String htmlDir, url, urlbase, codebase;
	final static public String context = "sorcer";

	/**
	 * Constructs a new SorcerCmd to handle the defined coomands in a doIt()
	 * method. The doIt method can use the command arguments in the args array,
	 * and a provided SorcerProtocolStatement aps (subclass of
	 * ApplicationPtotocolStatement). Also a servelet session might be used for
	 * user persistent state, in your doIt method add the line session =
	 * aps.getSession();
	 * 
	 * @param cmdName
	 *            the command name
	 */
	public SorcerCmd(String cmdName) {
		this.cmdName = cmdName;
	}

	public SorcerCmd() {
		// do nothing
	}

	public void setArgs(Object target, Object[] args) {
		aps = (SorcerProtocolStatement) target;
		if (aps.dbConnection == null)
			pw = aps.getWriter();

		this.args = args;
		if (aps.pConnection instanceof ServletProtocolConnection)
			session = ((ServletProtocolConnection) aps.pConnection).session;
	}

	public void doIt() {
		// session = aps.getSession();
		logger.info("Should be implemented in subclasses");
	}

	public void setInvoker(Invoker invoker) {
		aps = (SorcerProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return aps;
	}

	public void undoIt() {
		logger.info("SorcerCmd>>Should be implemented in subclasses");
	}

	public String cmd(int cmdNum) {
		return url + "?cmd=" + cmdNum;
	}

	public String arg(int argNum) {
		logger.info("This is in arg method" + argNum);
		return "arg" + argNum;
	}

	public Object getValue(String name) {
		return session.getValue(name);
	}

	public void putValue(String name, Object value) {
		session.putValue(name, value);
	}

	public void printFile(String fileName) throws IOException {
		String line;
		FileInputStream fis = null;
		try {
			/*
			 * BufferedReader in = new BufferedReader(new FileReader(htmlDir +
			 * fileName)); while ((line = in.readLine()) != null)
			 * pw.println(line);
			 */
			fis = new FileInputStream(htmlDir + fileName);
			int n;
			while ((n = fis.available()) > 0) {
				byte[] b = new byte[n];
				int result = fis.read(b);
				if (result == -1)
					break;
				pw.print(new String(b));
			}
		} catch (FileNotFoundException e1) {
			sendError("Can not find file: " + fileName);
		} finally {
			// if (in!=null) in.close();
			if (fis != null)
				fis.close();
		}
	}

	public void beginJS() throws IOException {
		pw.println("<SCRIPT LANGUAGE=\"JavaScript\">");
		pw.println("<!--");
	}

	public void endJS() throws IOException {
		pw.print("//---></script>");
	}

	public void sendError(String message) {
		pw.println("<p>ERROR: " + message);
		pw.println("<br>You may wish to inform the system administrator.");
		pw.println("</body></html>");
	}

	public void sendError() {
		sendError("SORCER command execution error");
	}

	public void superScript(String unit, String expo) {
		pw.println(unit + "<sup>" + expo + "</sup>");
	}

	/**
	 * Gets the named parameter value as a String
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a String
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found or was the empty string
	 */
	public String getStringParameter(String name)
			throws ParameterNotFoundException {
		Object val = session.getValue(name);
		if (val == null)
			throw new ParameterNotFoundException(name + " not found");
		else
			return (String) val;
	}

	/**
	 * Gets the named parameter value as a String, with a default. Returns the
	 * default value if the parameter is not found or is the empty string.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a String, or the default
	 */
	public String getStringParameter(String name, String def) {
		try {
			return getStringParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a boolean
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a boolean
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 */
	public boolean getBooleanParameter(String name)
			throws ParameterNotFoundException {
		return new Boolean(getStringParameter(name)).booleanValue();
	}

	/**
	 * Gets the named parameter value as a boolean, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a boolean, or the default
	 */
	public boolean getBooleanParameter(String name, boolean def) {
		try {
			return getBooleanParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a byte
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a byte
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter value could not be converted to a byte
	 */
	public byte getByteParameter(String name)
			throws ParameterNotFoundException, NumberFormatException {
		return Byte.parseByte(getStringParameter(name));
	}

	/**
	 * Gets the named parameter value as a byte, with a default. Returns the
	 * default value if the parameter is not found or cannot be converted to a
	 * byte.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a byte, or the default
	 */
	public byte getByteParameter(String name, byte def) {
		try {
			return getByteParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a char
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a char
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found or was the empty string
	 */
	public char getCharParameter(String name) throws ParameterNotFoundException {
		String param = getStringParameter(name);
		if (param.length() == 0)
			throw new ParameterNotFoundException(name + " is empty string");
		else
			return (param.charAt(0));
	}

	/**
	 * Gets the named parameter value as a char, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a char, or the default
	 */
	public char getCharParameter(String name, char def) {
		try {
			return getCharParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a double
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a double
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter could not be converted to a double
	 */
	public double getDoubleParameter(String name)
			throws ParameterNotFoundException, NumberFormatException {
		return new Double(getStringParameter(name)).doubleValue();
	}

	/**
	 * Gets the named parameter value as a double, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a double, or the default
	 */
	public double getDoubleParameter(String name, double def) {
		try {
			return getDoubleParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a float
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a float
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter could not be converted to a float
	 */
	public float getFloatParameter(String name)
			throws ParameterNotFoundException, NumberFormatException {
		return new Float(getStringParameter(name)).floatValue();
	}

	/**
	 * Gets the named parameter value as a float, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a float, or the default
	 */
	public float getFloatParameter(String name, float def) {
		try {
			return getFloatParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a int
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a int
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter could not be converted to a int
	 */
	public int getIntParameter(String name) throws ParameterNotFoundException,
			NumberFormatException {
		return Integer.parseInt(getStringParameter(name));
	}

	/**
	 * Gets the named parameter value as a int, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a int, or the default
	 */
	public int getIntParameter(String name, int def) {
		try {
			return getIntParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a long
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a long
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter could not be converted to a long
	 */
	public long getLongParameter(String name)
			throws ParameterNotFoundException, NumberFormatException {
		return Long.parseLong(getStringParameter(name));
	}

	/**
	 * Gets the named parameter value as a long, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a long, or the default
	 */
	public long getLongParameter(String name, long def) {
		try {
			return getLongParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * Gets the named parameter value as a short
	 * 
	 * @param name
	 *            the parameter name
	 * @return the parameter value as a short
	 * @exception ParameterNotFoundException
	 *                if the parameter was not found
	 * @exception NumberFormatException
	 *                if the parameter could not be converted to a short
	 */
	public short getShortParameter(String name)
			throws ParameterNotFoundException, NumberFormatException {
		return Short.parseShort(getStringParameter(name));
	}

	/**
	 * Gets the named parameter value as a short, with a default. Returns the
	 * default value if the parameter is not found.
	 * 
	 * @param name
	 *            the parameter name
	 * @param def
	 *            the default parameter value
	 * @return the parameter value as a short, or the default
	 */
	public short getShortParameter(String name, short def) {
		try {
			return getShortParameter(name);
		} catch (Exception e) {
			return def;
		}
	}

	// intialize static resources
	static {
		htmlDir = ApplicationDomain
				.getProperty("applicationServlet.templateDir");
		if (!(htmlDir.charAt(htmlDir.length() - 1) == File.separatorChar))
			htmlDir = htmlDir + File.separatorChar;

		url = ApplicationDomain.getProperty("applicationServlet.url");
		urlbase = ApplicationDomain.getProperty("applicationServlet.urlbase");
		codebase = ApplicationDomain.getProperty("applicationServlet.codebase");
	}

	// markups for SORCER applets
	public static String SORCER_LAUNCHER = "<APPLET name=\"designInput\" codebase=\""
			+ codebase
			+ "\" code=\"sorcer.lnch.SorcerLauncher\" archive=\"sorcer.jar\" width=700 height=450></APPLET>";

	protected String getArgAsString(int i) {
		return (String) args[i];
	}

}
