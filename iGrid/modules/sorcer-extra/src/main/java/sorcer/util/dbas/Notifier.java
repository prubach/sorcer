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

/**
 * A daemon thread that calls the updateNotifications() every waitnig time
 * interval. This method should be implemented in subclasses.
 */
abstract public class Notifier extends Thread implements MonitoredProcess {
	// private static int WAITING_TIME = 10 * 60 * 1000; //10 min
	private static int WAITING_TIME = 1 * 60 * 1000; // 10 min
	private boolean isMonitored = false, running = true;
	protected ProcessMonitor monitor;
	private ApplicationDomain appDomain;

	public Notifier() {
		// do nothing, created dynamically
	}

	public void initialize(ApplicationDomain domain) {
		appDomain = domain;

		// expected waiting time in application properties in minutes
		String wt = appDomain.props.getProperty("notifier.waitingTime");
		if (wt != null)
			WAITING_TIME = Integer.parseInt(wt) * 60 * 1000;

		isMonitored = appDomain.isNotifierMonitored();
		if (isMonitored)
			openNotifierMonitor();

		setDaemon(true);
		start();
	}

	public void run() {
		while (running) {
			try {
				update();
				Thread.sleep(WAITING_TIME);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void stopProcess() {
		running = false;
	}

	public String appName() {
		return ApplicationDomain.appName + " Notifier";
	}

	abstract public void update();

	// Just testing example, use as a template
	/*
	 * { //Should be implemented by subclassesSystem.err.println(
	 * "Notifier>>updateNotifications: Should be implemented by subclasses");
	 * 
	 * // to handle database connectivity use dps, do not foget to close it
	 * DefaultProtocolStatement dps = new DefaultProtocolStatement();
	 * 
	 * String[] msg = new String[Protocol.MSIZE]; msg[Protocol.MTO] =
	 * "sobol@cs.ttu.edu"; msg[Protocol.MFROM] = "sobol@cs.ttu.edu";
	 * msg[Protocol.MSUBJECT] = "GApp notification"; msg[Protocol.MTEXT] =
	 * "Notifier called updateNotifications, " + new java.util.Date();
	 * 
	 * if (isMonitored()) monitor.addItem(msg[Protocol.MTEXT]); else
	 * System.out.println(msg[Protocol.MTEXT]);
	 * 
	 * dps.controller.runIt(Integer.toString(Protocol.SEND_MAIL), msg);
	 * 
	 * // or when command is not provided by dps.controller // Command mail =
	 * new EmailCmd(String.valueOf(Protocol.SEND_MAIL)); // mail.setArgs(dps,
	 * msg); // mail.doIt();
	 * 
	 * dps.close(); } catch(SQLException e) { e.printStackTrace(); } }
	 */

	private void openNotifierMonitor() {
		// Create a window to display notifier messages in
		int monitorSize = -1;
		String ms = appDomain.props.getProperty("notifier.monitorSize");
		if (ms != null)
			monitorSize = Integer.parseInt(ms);
		monitor = new ProcessMonitor(this, monitorSize);
		monitor.pack();
		monitor.show();
	}

	public String passwd() {
		return appDomain.passwd();
	}

	public boolean isMonitored() {
		return isMonitored;
	}

	public void isMonitored(boolean value) {
		isMonitored = value;
	}
}
