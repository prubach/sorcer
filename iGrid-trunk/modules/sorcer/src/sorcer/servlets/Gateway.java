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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.RMISecurityManager;
import java.util.Hashtable;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sorcer.core.DocumentFileStorer;
import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.core.xml.StaxParser;
import sorcer.core.xml.XMLElement;
import sorcer.core.xml.XMLFile;
import sorcer.security.jaas.UsernamePasswordCallbackHandler;
import sorcer.security.util.GAppACL;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.ServiceExertion;
import sorcer.service.Servicer;
import sorcer.util.DocumentDescriptor;
import sorcer.util.ProviderAccessor;
import sorcer.util.rmi.InputStreamAdapter;
import sorcer.util.rmi.OutputStreamProxy;

public class Gateway extends HttpServlet {

	private String intermediateFile = null;

	private String propertyFilePath;

	private void loadProperties(HttpServletResponse res) {
		// load the properties form properties file
		Properties props = new Properties();
		File file;

		FileInputStream fin;
		String str;

		try {
			fin = new FileInputStream(propertyFilePath + File.separator
					+ "gateway.properties");
			props.load(fin);
			fin.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			try {
				res.sendError(res.SC_BAD_REQUEST,
						"Unable to read Property file");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		str = props.getProperty("gateway.task.filename", "task.xml");
		intermediateFile = str;
	}

	private void writeToFile(String filename, String data) {
		PrintWriter pw = null;

		try {
			pw = new PrintWriter(new FileOutputStream(filename));

			pw.write(data);
			pw.flush();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} finally {
			pw.close();
		}
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		propertyFilePath = config.getInitParameter("propertyFilePath");
		if (propertyFilePath == null)
			propertyFilePath = ".";

		System.out.println(getClass().getName() + ">>>>>>>> initParam = "
				+ propertyFilePath);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		// Read the data sent from MIDlet
		ServletInputStream in = req.getInputStream();
		DataInputStream din = new DataInputStream(in);

		int length = 0;
		length = req.getContentLength();

		if (length <= 0) {
			System.out.println("some problem with length");
			length = 1024;
		}

		byte[] data = new byte[length];
		if ((din.read(data) == -1)) {
			System.out.println("some problem with actual data");
			res.sendError(res.SC_BAD_REQUEST, "Unable to read parameters");
			return;
		}

		String request = new String(data);
		int idx = request.indexOf("<?xml");
		if (idx != -1)
			request = request.substring(idx);

		idx = request.indexOf("\n0");
		if (idx != -1)
			request = request.substring(0, idx);

		System.out.println(">>>>>>>>>>>>>>request sent = " + request);
		request = request.trim();

		System.out.println(getClass().getName() + ">>>>>>>> initParam = "
				+ req.getParameter("asDir"));
		loadProperties(res);
		System.out.println("after load properties");
		writeToFile(intermediateFile, request);
		System.out.println("after writeToFile");

		// Parser Stuff
		StaxParser parser = new StaxParser(intermediateFile);

		Hashtable elements = parser.parse();

		String result = null;
		Subject subject;

		String username = (String) ((XMLElement) (elements.get("username")))
				.getData();
		char[] password = ((String) ((XMLElement) (elements.get("password")))
				.getData()).toCharArray();

		System.out.println(">>>>>>Username" + username);
		System.out.println(">>>>>>password" + password);

		try {
			System.setProperty("portal.server",
					"http://neem.cs.ttu.edu:2030/sorcer/controller");

			Configuration cfg = new Configuration() {

				public AppConfigurationEntry[] getAppConfigurationEntry(
						String applicationName) {
					Hashtable options = new Hashtable();
					String key = "jgapp.jaas.PasswordLoginModule";
					String value = "required";
					options.put(key, value);

					// jgapp.jaas.PasswordLoginModule
					// com.sun.security.auth.module.UnixLoginModule

					return new AppConfigurationEntry[] { new AppConfigurationEntry(
							"jgapp.jaas.PasswordLoginModule",
							AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
							options) };
				}

				public void refresh() {
				}
			};

			Configuration.setConfiguration(cfg);

			LoginContext loginContext = new LoginContext("ravi",
					new UsernamePasswordCallbackHandler(username, password));

			loginContext.login();
			subject = loginContext.getSubject();

			String serviceType = null;
			XMLElement element;

			element = (XMLElement) (elements.get("serviceType"));
			if (element != null)
				serviceType = (String) element.getData();

			Class[] classTypes = { Class.forName(serviceType) };
			Servicer provider = ProviderAccessor.getProvider(classTypes);

			if (provider != null) {
				if (provider instanceof DocumentFileStorer) {
					SorcerPrincipal principal = new SorcerPrincipal();

					principal.setId("363");
					principal.setName("sobol");
					principal.setRole("root");
					principal.setAccessClass(4);
					principal.setExportControl(false);

					if (System.getSecurityManager() == null) {
						System.setSecurityManager(new RMISecurityManager());
					}

					try {
						DocumentFileStorer rfs = (DocumentFileStorer) provider;
						DocumentDescriptor docDesc = new DocumentDescriptor();
						docDesc.setPrincipal(principal);

						GAppACL acl = new GAppACL(principal, "ACL Protected");
						try {
							acl.addGroupPermissions(principal, "group1",
									new String[] { GAppACL.VIEW, GAppACL.ADD },
									true);
						} catch (Exception e) {
						}

						docDesc.setACL(acl);

						element = (XMLElement) (elements.get("method"));
						String methodName = null;

						if (element != null)
							methodName = (String) element.getData();

						System.out
								.println("before getting filename, folder and file content");
						if ((element = (XMLElement) elements
								.get("in-value" + 0)) != null) {
							System.out.println("entered if loop");
							String uploadData = (String) element.getData();
							String path = null;

							System.out.println("before extracting ::");
							int index = uploadData.indexOf("::");
							if (index != -1) {
								path = uploadData.substring(0, index);
								uploadData = uploadData.substring(index + 2,
										uploadData.length());
							}

							System.out.println("before path.lastIndexOf()");
							if (path != null) {
								index = path.lastIndexOf("/");
								if (index != -1) {
									docDesc.setFolderPath(path.substring(0,
											index));
									docDesc.setDocumentName(path.substring(
											index + 1, path.length()));
								}

								System.out
										.println("before writing the actual file contents");
								writeToFile("tempFile", uploadData);
							}
						} else { // something wrong, break;
							result = new String("Data not proper");
						}

						if ("getDirectories".equals(methodName)) {
							System.out.println(getClass().getName()
									+ " before calling ");
							result = rfs.getDirectories(docDesc);
							System.out.println(getClass().getName()
									+ " after calling ");
						} else {
							if ("uploadFile".equalsIgnoreCase(methodName)) {
								System.out
										.println("Now writting this file to the server");
								System.out
										.println("Upload method, method name = "
												+ methodName);
								docDesc = rfs.getOutputDescriptor(docDesc);
								((OutputStreamProxy) docDesc.out)
										.write(new File("tempFile"));
								result = "Success";
							} else if ("listDirectories"
									.equalsIgnoreCase(methodName)) {
								docDesc.setFolderPath("/");
								System.out.println(getClass().getName()
										+ " before calling ");
								result = rfs.listDirectories(docDesc);
								System.out.println(getClass().getName()
										+ " after calling ");
							} else { // case of download
								System.out
										.println("Download method starts , method name = "
												+ methodName);
								docDesc = rfs.getInputDescriptor(docDesc);
								((InputStreamAdapter) docDesc.in)
										.read(new File("ravi"));
							}
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				} else {
					ServiceContext context = new ServiceContext(
							(String) ((XMLElement) (elements.get("method")))
									.getData(),
							(String) ((XMLElement) (elements.get("method")))
									.getData());

					int i = 0;

					for (; (element = (XMLElement) elements.get("in-value" + i)) != null; i++) {
						Contexts.putInValue(context, SorcerConstants.IN_VALUE
								+ SorcerConstants.CPS + i, element.getData());
					}

					String providerName = null;
					String methodName = null;

					element = (XMLElement) (elements.get("providerName"));
					if (element != null)
						providerName = (String) element.getData();

					element = (XMLElement) (elements.get("method"));
					if (element != null)
						methodName = (String) element.getData();

					System.out
							.println(">>>>>>>>>>>>>>The Method to be invoked = "
									+ methodName);
					System.out
							.println(">>>>>>>>>>>>>>The provider to be invoked = "
									+ providerName);
					System.out
							.println(">>>>>>>>>>>>>>The serviceType to be invoked = "
									+ serviceType);

					NetSignature method = new NetSignature(methodName,
							NetSignature.getClass(serviceType), providerName);

					ServiceExertion task = null;

					task = new NetTask(providerName, method);

					task.setContext(context);
					task.setDescription(providerName);
					task.setSubject(subject);

					ServiceExertion outTask;
					outTask = (ServiceExertion) (provider.service(task, null));
					result = Contexts.getFormattedOut(outTask.getContext(),
							false);
				}
			} else {
				result = new String("Provider not found");
				System.out.println("Provider not found");
			}

			// create the response and stream it to the MIDlet

			XMLFile virtualFile = new XMLFile();
			virtualFile.setExertion("task");
			virtualFile.startContext();
			virtualFile.setInValue(result);
			virtualFile.endContext();
			virtualFile.endExertion();

			result = virtualFile.getFile().toString();
			System.out.println("file = \n" + result);

		} catch (javax.security.auth.login.FailedLoginException fle) {
			fle.printStackTrace();
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		res.setContentType("application/octet-stream");
		res.setContentLength(result.length());
		res.setStatus(res.SC_OK);

		OutputStream out = res.getOutputStream();
		out.write(result.getBytes());
		out.flush();
		out.close();

	}

	/*--------------------------------------------------
	 * Information about servlet
	 *-------------------------------------------------*/
	public String getServletInfo() {
		return "Gateway for MIDP Devices";
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);
	}
}
