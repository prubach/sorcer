/*
 * Copyright 2008 the original author or authors.
 * Copyright 2005 Sun Microsystems, Inc.
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
package sorcer.tools.webster;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for starting an Internal Webster
 * 
 * @author Dennis Reedy, adapted for SORCER by Mike Sobolewski
 */
public class InternalWebster {
	private static Logger logger = Logger.getLogger("sorcer.tools.webster");
	private static boolean debug = false;
	public static final String WEBSTER_ROOTS = "sorcer.webster.roots";

	/**
	 * Start an internal webster, setting the webster root to the location of
	 * SORCER lib-dl directories, and appending exportJars as the export jars
	 * for the JVM.
	 * 
	 * @param exportJars
	 *            The jars to set for the codebase
	 * 
	 * @return The port Webster has been started on
	 * 
	 * @throws IOException
	 *             If there are errors creating Webster
	 */
	public static int startWebster(String[] exportJars) throws IOException {
		String codebase = System.getProperty("java.rmi.server.codebase");
		if (codebase != null)
			throw new RuntimeException("Codebase is alredy specified: "
					+ codebase);

		String d = System.getProperty("sorcer.webster.debug");
		if (d.equals("true"))
			debug = true;	

		String roots;
		InetAddress ip = InetAddress.getLocalHost();
		String localIPAddress = ip.getHostAddress();
		String iGridHome = System.getProperty("iGrid.home");
		String userHome = System.getProperty("user.home");
		roots = System.getProperty(WEBSTER_ROOTS);
		if (roots == null) {
			// defaults iGrid roots
			String fs = File.separator;
			StringBuffer sb = new StringBuffer();
			sb.append(iGridHome).append(fs).append("lib").append(fs).append("river").append(fs).append("lib-dl")
			.append(';').append(userHome).append(fs).append(".m2").append(fs).append("repository");
			roots = sb.toString();
		}

		String sMinThreads = System.getProperty("sorcer.webster.minThreads",
				"1");
		int minThreads = 1;
		try {
			minThreads = Integer.parseInt(sMinThreads);
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "Bad Min Threads Number [" + sMinThreads
					+ "], " + "default to " + minThreads, e);
		}
		String sMaxThreads = System.getProperty("sorcer.webster.maxThreads",
				"10");
		int maxThreads = 10;
		try {
			maxThreads = Integer.parseInt(sMaxThreads);
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "Bad Max Threads Number [" + sMaxThreads
					+ "], " + "default to " + maxThreads, e);
		}
		String sPort = System.getProperty("sorcer.webster.port", "0");
		int port = 0;
		try {
			port = Integer.parseInt(sPort);
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, "Bad port Number [" + sPort + "], "
					+ "default to " + port, e);
		}

		String address = System.getProperty("sorcer.webster.interface");
		port = new Webster(port, roots, address, minThreads, maxThreads, true).getPort();
		if (logger.isLoggable(Level.FINEST))
			logger.finest("Webster MinThreads=" + minThreads + ", "
					+ "MaxThreads=" + maxThreads);

		if (logger.isLoggable(Level.FINE))
			logger.fine("Webster serving on port=" + port);

		String[] jars = null;
		String jarsList = null;
		if (exportJars != null)
			jars = exportJars;
		else {
			jarsList = System.getProperty("sorcer.codebase.jars");
			if (jarsList == null || jarsList.length() == 0)
				throw new RuntimeException(
						"No jar files available for the webster codebase");
			else
				jars = toArray(jarsList);
		}
		
		codebase = "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < jars.length - 1; i++) {
			sb.append("http://").append(localIPAddress).append(":")
					.append(port).append("/").append(jars[i]).append(" ");
		}
		sb.append("http://").append(localIPAddress).append(":").append(port)
				.append("/").append(jars[jars.length - 1]);
		codebase = sb.toString();
		System.setProperty("java.rmi.server.codebase", codebase);
		if (logger.isLoggable(Level.FINE))
			logger.fine("Setting 'java.rmi.server.codebase': " + codebase);

		return port;
	}

	private static String[] toArray(String arg) {
		StringTokenizer token = new StringTokenizer(arg, " ,;");
		String[] array = new String[token.countTokens()];
		int i = 0;
		while (token.hasMoreTokens()) {
			array[i] = token.nextToken();
			i++;
		}
		return (array);
	}

	public static void main(String[] args) {
		try {
			startWebster(new String[] { "sorcer-prv-dl.jar" });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
