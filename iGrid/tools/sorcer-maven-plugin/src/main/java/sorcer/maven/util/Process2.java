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
