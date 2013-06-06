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

package sorcer.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author Rafał Krupiński
 */
public class JavaProcessBuilder {
	protected Logger log = LoggerFactory.getLogger(JavaProcessBuilder.class);
	protected Map<String, String> properties;
	protected Collection<String> classPathList;
	protected String mainClass;
	protected List<String> parameters;
	protected File workingDir;
	protected boolean debugger;
	protected File output;
	protected File sorcerHome = SorcerEnv.getHomeDir();

	public void setProperties(Map<String, String> environment) {
		this.properties = environment;
	}

	public void setClassPath(Collection<String> classPath) {
		this.classPathList = classPath;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public void setDebugger(boolean debugger) {
		this.debugger = debugger;
	}

	/**
	 * Set standard and error output
	 *
	 * @param output output file
	 */
	public void setOutput(File output) {
		this.output = output;
	}

	public Process2 startProcess() throws IOException {
		ProcessBuilder procBld = new ProcessBuilder().command("java");

		if (debugger) {
			procBld.command().addAll(Arrays.asList("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=8000"));
		}

		procBld.command().addAll(_D(properties));
		String classPath = StringUtils.join(classPathList, File.pathSeparator);
		procBld.command().addAll(asList("-classpath", classPath, mainClass));
		if (parameters != null) {
			procBld.command().addAll(parameters);
		}

		if (workingDir == null) {
			// the default
			// make explicit for logging purpose
			workingDir = new File(System.getProperty("user.dir"));
		}
		procBld.directory(workingDir);

		Map<String, String> env = procBld.environment();
		env.put("SORCER_HOME",sorcerHome.getPath());
		env.put("RIO_HOME",new File(sorcerHome,"lib/rio").getPath());

		StringBuilder cmdStr = new StringBuilder("[").append(workingDir.getPath()).append("] ")
				.append(StringUtils.join(procBld.command(), " "));
		if (output != null) {
			cmdStr.append(" > ").append(output.getPath());
		}

		log.info(cmdStr.toString());

		redirectIO(procBld);

		Process proc = null;
		try {
            proc = procBld.start();

            try {
                // give it a moment to exit on error
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                //ignore
            }

            // if the next call throws exception, then we're probably good -
            // process hasn't finished yet.
            int x = proc.exitValue();
            throw new IllegalStateException("Process exited with value " + x);
        } catch (IllegalThreadStateException x) {
            return new Process2(proc);
		}
	}

	/**
	 * Redirect output and error to ours IF the method
	 * {@link ProcessBuilder#inheritIO()} is available (since jdk 1.7)
	 */
	private void redirectIO(ProcessBuilder processBuilder) {
		if (output != null) {
			invokeIgnoreErrors(processBuilder, "redirectErrorStream", new Class[]{Boolean.TYPE}, true);
			// processBuilder.redirectErrorStream(true);
			invokeIgnoreErrors(processBuilder, "redirectOutput", new Class[]{File.class}, output);
		} else {
			invokeIgnoreErrors(processBuilder, "inheritIO", new Class[0]);
		}
	}

	protected Object invokeIgnoreErrors(Object target, String methodName, Class[] argTypes, Object... args) {
		try {
			Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
			return method.invoke(target, args);
		} catch (NoSuchMethodException e) {
			// looks like we're not in jdk1.7
			log.warn(e.getMessage(), e);
			return null;
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private List<String> _D(Map<String, String> d) {
		List<String> result = new ArrayList<String>(d.size());
		for (Map.Entry<String, String> e : d.entrySet()) {
			result.add(_D(e.getKey(), e.getValue()));
		}
		return result;
	}

	private String _D(String key, String value) {
		return "-D" + key + '=' + value;
	}
}
