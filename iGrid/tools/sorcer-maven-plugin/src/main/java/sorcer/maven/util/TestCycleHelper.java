package sorcer.maven.util;

/**
 * @author Rafał Krupiński
 */
public class TestCycleHelper {
	protected static ThreadLocal<TestCycleHelper> instance = new ThreadLocal<TestCycleHelper>();

	protected String provider;

	/**
	 * The provider process to kill in DestroyMojo (post-integration-test)
	 */
	protected Process2 process;

	public static TestCycleHelper getInstance() {
		if (instance.get() == null) {
			instance.set(new TestCycleHelper());
		}
		return instance.get();
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Process2 getProcess() {
		return process;
	}

	public void setProcess(Process2 process) {
		this.process = process;
	}
}
