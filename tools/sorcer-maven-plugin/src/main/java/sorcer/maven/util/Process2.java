/*
 * Copyright 2013 Rafał Krupiński.
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

package sorcer.maven.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rafał Krupiński
 */
public class Process2 {
	final private Process process;

	public Process2(Process process) {
		this.process = process;
	}

	public boolean running() {
		try {
			process.exitValue();
			return false;
		} catch (IllegalThreadStateException x) {
			return true;
		}
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	/**
	 * {@link Process#waitFor()} with timeout
	 *
	 * @param timeout timeout in milliseconds
	 * @return process's exit value, or null if process was destroyed after timeout
	 * @throws InterruptedException if the waiting thread was interrupted
	 */
	public Integer waitFor(long timeout) throws InterruptedException {
		/*
		* Normal waitFor() calls wait() in loop until its internal thread detects process is destroyed, in which case it calls
		* notifyAll().
		* We're calling wait() and wait for someone to call notify(). It might be either because process has ended or because our
		* Notifier has triggered. In the latter case timeout has passed, process hasn't finished and must be destroyed.
		*/
		if (timeout > 0) {
			new Timer().schedule(new Notifier(process), timeout);
			synchronized (process) {
				//in case vary small value of timeout and notify is called before wait
				process.wait(timeout);
			}
		}
		return exitValueOrDestroy();
	}

	private Integer exitValueOrDestroy() {
		try {
			return process.exitValue();
		} catch (IllegalThreadStateException x) {
			process.destroy();
			return null;
		}
	}

	public void destroy() {
		process.destroy();
	}
}

/**
 *
 */
class Notifier extends TimerTask {
	private final Object monitor;

	Notifier(Object monitor) {
		this.monitor = monitor;
	}

	@Override
	public void run() {
		synchronized (monitor) {
			monitor.notify();
		}
	}
}