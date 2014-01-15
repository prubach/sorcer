/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

package sorcer.launcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.ByteDumper;
import sorcer.util.Process2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author Rafał Krupiński
 */
public class JavaProcessBuilder {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected Map<String, String> properties;
    protected Map<String, String> environment = new HashMap<String, String>(System.getenv());
	protected Collection<String> classPathList;
	protected String mainClass;
	protected List<String> parameters;
	protected File workingDir;
	protected boolean debugger;
	protected File outFile;
	protected File errFile;
    protected String command = "java";
    protected int debugPort = 8000;

    protected OutputStream out;
    protected OutputStream err;

    protected ThreadGroup ioThreads = new ThreadGroup("Sorcer launcher IO threads");
    private final ProcessBuilder builder = new ProcessBuilder();

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

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    /**
     * Set standard and error output
	 *
	 * @param output output file
	 */
	public void setOutFile(File output) {
		this.outFile = output;
	}

    public void setErrFile(File errFile) {
        this.errFile = errFile;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

	public Process2 startProcess() throws IOException {
        if (mainClass == null || mainClass.trim().isEmpty()) {
            throw new IllegalStateException("mainClass must be set");
        }
        builder.command(command);

        if (debugger)
            builder.command().addAll(Arrays.asList("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=" + debugPort));

		if(properties!=null)
            builder.command().addAll(_D(properties));

		String classPath = StringUtils.join(classPathList, File.pathSeparator);
		builder.command().addAll(asList("-classpath", classPath, mainClass));
		if (parameters != null) {
			builder.command().addAll(parameters);
		}

		if (workingDir == null) {
			// the default
			// make explicit for logging purpose
			workingDir = new File(System.getProperty("user.dir"));
		}
		builder.directory(workingDir);

        Map<String, String> procEnv = builder.environment();
        if(!procEnv.equals(environment)){
            procEnv.clear();
            procEnv.putAll(environment);
        }

		StringBuilder cmdStr = new StringBuilder("[").append(workingDir.getPath()).append("] ")
				.append(StringUtils.join(builder.command(), " "));
		if (outFile != null) {
			cmdStr.append(" > ").append(outFile.getPath());
		}

		log.info(cmdStr.toString());

		redirectIO();

		Process proc = null;
		try {
            proc = builder.start();

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
        } catch (IllegalThreadStateException ignored) {
            redirectOutputs(proc);
            return new Process2(proc, ioThreads);
		}
    }

    private void redirectOutputs(Process proc) {
        if (out != null)
            redirectOutput(proc.getInputStream(), out, proc.toString() + " stdout reader thread");
        if (err != null)
            redirectOutput(proc.getErrorStream(), err, proc.toString() + " stderr reader thread");
    }

    private void redirectOutput(InputStream inputStream, OutputStream outputStream, String name) {
        new Thread(ioThreads, new ByteDumper(inputStream, outputStream), name).start();
    }

    /**
	 * Redirect output and error to ours IF the method
	 * {@link ProcessBuilder#inheritIO()} is available (since jdk 1.7)
	 */
    private void redirectIO() throws FileNotFoundException {
        if (outFile != null)
            redirectOutput(outFile);
        if (errFile != null) {
            redirectError(errFile);
        } else if (outFile != null)
            redirectErrorStream(true);
    }

    protected JavaProcessBuilder redirectOutput(File file) throws FileNotFoundException {
        try {
            invokeIgnoreErrors(builder, "redirectOutput", new Class[]{File.class}, file);
        } catch (Exception e) {
            out = new FileOutputStream(file);
        }
        return this;
    }

    protected JavaProcessBuilder redirectError(File file) throws FileNotFoundException {
        try {
            invokeIgnoreErrors(builder, "redirectError", new Class[]{File.class}, file);
        } catch (Exception e) {
            err = new FileOutputStream(file);
        }
        return this;
    }

    protected JavaProcessBuilder redirectErrorStream(boolean redirect) {
        try {
            invokeIgnoreErrors(builder, "redirectErrorStream", new Class[]{Boolean.TYPE}, redirect);
        } catch (Exception e) {
            err = out;
        }
        return this;
    }

    protected Object invokeIgnoreErrors(Object target, String methodName, Class[] argTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
			Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
			return method.invoke(target, args);
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

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public void setErr(OutputStream err) {
        this.err = err;
    }
}
