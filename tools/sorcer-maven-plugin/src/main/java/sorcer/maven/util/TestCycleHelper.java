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
public class TestCycleHelper {
	protected static ThreadLocal<TestCycleHelper> instance = new ThreadLocal<TestCycleHelper>();

	protected String provider;
	protected int websterPort;
	protected String sorcerEnv;

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

	public int getWebsterPort() {
		return websterPort;
	}

	public void setWebsterPort(int websterPort) {
		this.websterPort = websterPort;
	}

	public String getSorcerEnv() {
		return sorcerEnv;
	}

	public void setSorcerEnv(String sorcerEnv) {
		this.sorcerEnv = sorcerEnv;
	}
}
