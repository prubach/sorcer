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

/**
 * @author Rafał Krupiński
 */
public class Process2 {
	private Process process;

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

	public void waitFor() throws InterruptedException {
		process.waitFor();
	}

	/**
	 * {@link Process#waitFor()} with timeout
	 * 
	 * FIXME support the timeout
	 * 
	 * @param timeout
	 *            timeout in milliseconds, not supported yet
	 * @param destroy
	 *            wheather to kill the process after the timeout has passed
	 * @return
	 * @throws InterruptedException
	 *             if the waiting thread was interrupted or if it has reached
	 *             the timeout
	 */
	public int waitFor(long timeout, boolean destroy) throws InterruptedException {
		return process.waitFor();
	}

	public void destroy() {
		process.destroy();
	}
}
