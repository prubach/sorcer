/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.tools.shell.cmds;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jini.core.lookup.ServiceItem;
import net.jini.id.Uuid;
import sorcer.core.provider.Provider;
import sorcer.core.provider.StorageManagement;
import sorcer.core.monitor.MonitorUIManagement;
import sorcer.eo.operator;
import sorcer.jini.lookup.AttributesUtil;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.core.provider.DatabaseStorer;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.MonitorException;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.tools.shell.NetworkShell;
import sorcer.tools.shell.ReggieHelper;
import sorcer.tools.shell.ShellCmd;
import sorcer.util.WhitespaceTokenizer;
import sorcer.util.bdb.objects.ObjectInfo;
import sorcer.util.url.sos.SdbUtil;

public class DataStorageCmd extends ShellCmd {

	{
		COMMAND_NAME = "ds";

		NOT_LOADED_MSG = "***command not loaded due to conflict";

		COMMAND_USAGE = "ds [-s | -s <storage index> | -v | -x]"
			+ "\n\t\t\t  | -l [<store index>] | -r | -s"
			+ "\n\t\t\t  | (-e | -c | -cc | -ccc) [<exertion index>] [-s <filename>]";

		COMMAND_HELP = "Support for inspecting SORCER data storage;"
				+ "\n  -s   show data storage services"
				+ "\n  -s   <storage index>   select the data storage given <storage index>"
				+ "\n  -v   print the selected storage service"
				+ "\n  -x   clear the selection of data storage service"
				+ "\n  -l   [<store type>] list all storage records or of a given type"
				+ "\n  -r   print the selected record"
				+ "\n  -s   save the selected record in a given file ";

	}

	static private PrintStream out;
	static private ServiceItem[] dataStorers;
	static private ObjectInfo[] recordInfos;
	static private int selectedDataStorer = -1;
	private DatabaseStorer.Store selectedStore;
	private int selectedRecord = -1;
	static private Map<Uuid, ServiceItem> dataStorerMap = new HashMap<Uuid, ServiceItem>();

	public DataStorageCmd() {
	}

	public void execute() throws RemoteException, MonitorException {
		out = NetworkShell.getShellOutputStream();
		WhitespaceTokenizer myTk = NetworkShell.getShellTokenizer();
		int numTokens = myTk.countTokens();
		int myIdx = 0;
		String next = null;
		DatabaseStorer.Store storeType = null;

		if (numTokens == 0) {
			printStorageServices();
			return;
		} else if (numTokens == 1) {
			next = myTk.nextToken();
			if (next.equals("-v")) {
				if (selectedDataStorer >= 0) {
					describeStorer(selectedDataStorer);
				} else
					out.println("No selected data storage");
				return;
			} else if (next.equals("-l")) {
				printRecords(DatabaseStorer.Store.all);
			} else if (next.equals("-s")) {
                printStorageServices();
				selectedDataStorer = -1;
				// remove storage selection
			} else if (next.equals("-x")) {
				selectedDataStorer = -1;
			} else if (next.equals("-r")) {
				if (selectedRecord >= 0) {
					ObjectInfo recordInfo = recordInfos[selectedRecord];
//					printRecord(recordInfo.uuid, recordInfo.type);
				}
			} else {
				try {
					myIdx = Integer.parseInt(next);
					selectedRecord = myIdx;
				} catch (NumberFormatException e) {
					selectedRecord = selectRecordByName(next);
				}
				if (selectedRecord < 0
						|| selectedRecord >= recordInfos.length)
					out.println("No such REcord for: " + next);
				else
					out.println(recordInfos[selectedRecord]);
			}
		} else if (numTokens == 2) {
			next = myTk.nextToken();
			if (next.equals("-s")) {
				try {
					next = myTk.nextToken();
					myIdx = Integer.parseInt(next);
					selectedDataStorer = myIdx;
				} catch (NumberFormatException e) {
					selectedDataStorer = selectMonitorByName(next);
					if (selectedDataStorer < 0)
						out.println("No such data storage for: " + next);
				}
				if (selectedDataStorer >= 0) {
					describeStorer(selectedDataStorer, "SELECTED");
				} else {
					out.println("No such data storage for: " + selectedDataStorer);
				}
				return;
			} else if (next.equals("-l")) {
				try {
					next = myTk.nextToken();
					myIdx = Integer.parseInt(next);
					selectedRecord = myIdx;
				} catch (NumberFormatException e) {
					selectedRecord = selectRecordByName(next);
					if (selectedRecord < 0)
						out.println("No such Record for: " + next);
				}
				if (selectedRecord >= 0
						&& selectedRecord < recordInfos.length) {
					ObjectInfo recordInfo = recordInfos[selectedRecord];
//					printRecord(recordInfo.uuid, recordInfo.type);
				} else
					out.println("No such Record for: " + selectedRecord);
			}
		} else {
			out.println(COMMAND_USAGE);
			return;
		}
	}

	private void printRecord(Uuid id, DatabaseStorer.Store type) throws RemoteException, MonitorException {
		Exertion xrt = null;
		if (selectedDataStorer >= 0) {
			xrt = ((MonitorUIManagement) dataStorers[selectedDataStorer].service)
					.getMonitorableExertion(id, NetworkShell.getPrincipal());
		} else {
			xrt = ((MonitorUIManagement) dataStorerMap.get(id).service)
					.getMonitorableExertion(id, NetworkShell.getPrincipal());
		}

		out.println("--------- STORAGE RECORD # " + selectedRecord + " ---------");
		out.println(((ServiceExertion) xrt).describe());
	}

	private void printRecords(DatabaseStorer.Store type)
			throws RemoteException, MonitorException {
		if (dataStorers == null || dataStorers.length == 0) {
			findStorers();
		}
		Map<Uuid, ObjectInfo> all;
		if (selectedDataStorer >= 0) {
			out.println("From Data Storage: "
					+ AttributesUtil
							.getProviderName(dataStorers[selectedDataStorer].attributeSets)
					+ " at: "
					+ AttributesUtil
							.getHostName(dataStorers[selectedDataStorer].attributeSets));
//			all = ((StorageManagement) dataStorers[selectedDataStorer].service)
//					.getMonitorableExertionInfo(type,
//							NetworkShell.getPrincipal());
			
			Context cxt = null;
			 try {
				 try {
                     operator.store("dupa");
                 } catch (ExertionException e1) {
					e1.printStackTrace();
				} catch (SignatureException e1) {
					e1.printStackTrace();
				}
				 out.println("XXXXXXXXXXXXX service item: " + dataStorers[selectedDataStorer]);
				 out.println("XXXXXXXXXXXXX service: " + dataStorers[selectedDataStorer].service);
				 out.println("XXXXXXXXXXXXX interfaces: " + Arrays.toString(dataStorers[selectedDataStorer].service.getClass().getInterfaces()));
				 out.println("XXXXXXXXXXXXX name: " + ((Provider) dataStorers[selectedDataStorer].service).getProviderName());
				 try {
				 cxt = ((DatabaseStorer) dataStorers[selectedDataStorer].service).contextList(SdbUtil.getListContext(DatabaseStorer.Store.object));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				out.println("XXXXXXXXXXXXX dataContext: " + cxt);
				try {
                    operator.store("dupa");
                    List<String>  records = operator.list(DatabaseStorer.Store.object);
					out.println("XXXXXXXXXXXXX records; " + records);
				} catch (ExertionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SignatureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out.println(cxt.getValue(StorageManagement.store_content_list));
//			} catch (ExertionException e) {
//				e.printStackTrace();
//			} catch (SignatureException e) {
//				e.printStackTrace();
			} catch (ContextException e) {
				e.printStackTrace();
			}
			
			
			
		} else {
			Map<Uuid, ObjectInfo> ri = null;
			all = new HashMap<Uuid, ObjectInfo>();
			for (int i = 0; i < dataStorers.length; i++) {
				out.println("From Data Storage "
						+ AttributesUtil
								.getProviderName(dataStorers[i].attributeSets)
						+ " at: "
						+ AttributesUtil
								.getHostName(dataStorers[i].attributeSets));

				StorageManagement emx = (StorageManagement) dataStorers[i].service;
//				ri = emx.getMonitorableExertionInfo(type,
//						NetworkShell.getPrincipal());
				if (ri != null && ri.size() > 0) {
					all.putAll(ri);
				}
				// populate exertion/EMX map
				dataStorerMap.clear();
				for (ObjectInfo ei : ri.values()) {
					dataStorerMap.put(ei.uuid, dataStorers[i]);
				}
			}
		}
//		if (all.size() == 0) {
//			out.println("No monitored exertions at this time.");
//			return;
//		}
//		recordInfos = new RecordInfo[all.size()];
//		all.values().toArray(recordInfos);
//		printRecordInfos(recordInfos);
	}

	private void printRecordInfos(ObjectInfo[] recordInfos) {
		for (int i = 0; i < recordInfos.length; i++) {
			out.println("--------- RECORD # " + i + " ---------");
			out.println(recordInfos[i].describe());
		}
	}

	private void printStorageServices() {
        if (dataStorers==null || dataStorers.length==0) {
            try {
                findStorers();
            } catch (RemoteException re) {
                out.println("Problem retrieving data storers: " + re.getMessage());
            }
        }
		if ((dataStorers != null) && (dataStorers.length > 0)) {
			for (int i = 0; i < dataStorers.length; i++) {
				describeStorer(i);
			}
		} else
			System.out.println("Sorry, no fetched Data Storage services.");
	}

	static private void describeStorer(int index) {
		describeStorer(index, null);
	}
	
	public static void printCurrentStorer() {
		if (selectedDataStorer >= 0) {
			NetworkShell.shellOutput.println("Current data storage service: ");
			describeStorer(selectedDataStorer);
		}
		else {
			NetworkShell.shellOutput.println("No selected data storage, use 'ds -s' to list and select with 'ds #'");
		}
	}

	static private void describeStorer(int index, String msg) {
		out.println("---------" + (msg != null ? " " + msg : "")
				+ " DATA STORAGE SERVICE # " + index + " ---------");
		out.println("EMX: " + dataStorers[index].serviceID + " at: "
				+ AttributesUtil.getHostName(dataStorers[index].attributeSets));
		out.println("Home: "
				+ AttributesUtil.getUserDir(dataStorers[index].attributeSets));
		String groups = AttributesUtil
				.getGroups(dataStorers[index].attributeSets);
		out.println("Provider name: "
				+ AttributesUtil
						.getProviderName(dataStorers[index].attributeSets));
		out.println("Groups supported: " + groups);
	}

	private int selectMonitorByName(String name) {
		for (int i = 0; i < dataStorers.length; i++) {
			if (AttributesUtil.getProviderName(dataStorers[i].attributeSets)
					.equals(name))
				return i;
		}
		return -1;
	}

	private int selectRecordByName(String name) {
		for (int i = 0; i < dataStorers.length; i++) {
			if (AttributesUtil.getProviderName(dataStorers[i].attributeSets)
					.equals(name))
				return i;
		}
		return -1;
	}


	public static void setDataStorers(ServiceItem[] storers) {
		dataStorers = storers;
	}

	public static Map<Uuid, ServiceItem> getDataStorerMap() {
		return dataStorerMap;
	}

	static ServiceItem[] findStorers() throws RemoteException {
		dataStorers = ReggieHelper.lookup(new Class[]{StorageManagement.class});
		return dataStorers;
	}
	
//	static ServiceItem[] findStorers() {
//		return findStorers(false);
//	}
//
//	static ServiceItem[] findStorers(boolean newDiscovery) {
//		ServiceTemplate st = new ServiceTemplate(null,
//				new Class[] { StorageManagement.class }, null);
//		dataStorers = ServiceAccessor.getServiceItems(st, null,
//		// DiscoveryGroupManagement.ALL_GROUPS);
//				NetworkShell.getGroups());
//		return dataStorers;
//	}

	public static ArrayList<StorageManagement> getDataStorers() {
		ArrayList<StorageManagement> dataStorerList = new ArrayList<StorageManagement>();
		for (int i = 0; i < dataStorers.length; i++) {
			dataStorerList.add((StorageManagement) dataStorers[i].service);
		}
		return dataStorerList;
	}

	private DatabaseStorer.Store getStoreType(String type) {
		DatabaseStorer.Store storeType = DatabaseStorer.Store.all;
		String option = type.toLowerCase();
		if (option == null)
			storeType = DatabaseStorer.Store.all;
		else if (option.startsWith("c"))
			storeType = DatabaseStorer.Store.context;
		else if (option.startsWith("a"))
			storeType = DatabaseStorer.Store.all;
		else if (option.startsWith("e"))
			storeType = DatabaseStorer.Store.exertion;
		else if (option.startsWith("t"))
			storeType = DatabaseStorer.Store.table;
		else if (option.startsWith("v"))
			storeType = DatabaseStorer.Store.var;
		else if (option.startsWith("m"))
			storeType = DatabaseStorer.Store.varmodel;
		else if (option.startsWith("o"))
			storeType = DatabaseStorer.Store.object;

		selectedStore = storeType;
		return storeType;
	}
	
}
