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

import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * The RMIServlet provides RMI export support for subclasses
 */
public class RMIServlet extends HttpServlet implements Remote {
	protected Registry registry;

	public void init(ServletConfig sc) throws ServletException {
		super.init(sc);
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());

		String isRemote = getInitParameter("isRemote");
		if (isRemote != null && isRemote.equalsIgnoreCase("true")) {
			try {
				int port = 0;
				String sport = getInitParameter("servlet.port");
				if (sport != null)
					port = Integer.parseInt(sport);

				UnicastRemoteObject.exportObject(this, port);
				bind();
			} catch (RemoteException e) {
				log("Problem binding to RMI registry: " + e.getMessage());
			}
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
		// Fallback choice is the name of this class
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
			return 1099;
		}
	}

	/**
	 * Binds the servlet to the registry. Creates the registry if necessary.
	 * Logs any errors.
	 */
	protected void bind() {
		// Try to find the appropriate registry already running
		int port = getRegistryPort();
		try {
			registry = LocateRegistry.getRegistry(port);
			registry.list(); // Verify it's alive and well
		} catch (Exception e) {
			// Couldn't get a valid registry
			e.printStackTrace();
			registry = null;
		}

		// If we couldn't find it, we need to create it.
		// (Equivalent to running "rmiregistry")
		if (registry == null) {
			try {
				registry = LocateRegistry.createRegistry(port);
			} catch (Exception e) {
				e.printStackTrace();
				log("Could not get or create RMI registry on port " + port
						+ ": " + e.getMessage());
				return;
			}
		}

		// If we get here, we must have a valid registry.
		// Now register this servlet instance with that registry.
		try {
			String rn = getRegistryName();
			registry.rebind(rn, this);
		} catch (Exception e) {
			e.printStackTrace();
			log("Could not bind to RMI registry: " + e.getMessage());
			return;
		}
	}

	/**
	 * Unbinds the servlet from the registry. Logs any errors.
	 */
	protected void unbind() {
		try {
			if (registry != null)
				registry.unbind(getRegistryName());
		} catch (Exception e) {
			log("Problem unbinding from RMI registry: " + e.getMessage());
		}
	}

	/**
	 * Halts the servlet's RMI operations. Causes the servlet to unbind itself
	 * from the registry. Logs any errors. Subclasses that override this method
	 * must be sure to first call <tt>super.destroy()</tt>.
	 */
	public void destroy() {
		unbind();
	}
}
