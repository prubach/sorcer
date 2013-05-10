package sorcer.maven.util;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * @author Rafał Krupiński
 */
public class JavaProcessBuilder {
	protected Map<String, String> properties;
	protected Collection<String> classPathList;
	protected String mainClass;
	protected List<String> parameters;
	protected File workingDir;
protected boolean debugger;
	protected Log log;

	public JavaProcessBuilder(Log log) {
		this.log = log;
	}

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

	public Process2 startProcess() throws MojoExecutionException, MojoFailureException {
		ProcessBuilder procBld = new ProcessBuilder().command("java");

		if(debugger){
			procBld.command().addAll(Arrays.asList("-Xdebug","-Xrunjdwp:transport=dt_socket,server=y,address=8000"));
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

		log.info("[" + workingDir.getPath() + "] " + StringUtils.join(procBld.command(), " "));

		redirectIO(procBld);

		Process proc = null;
		try {
			try {
				proc = procBld.start();

				// give it a second to exit on error
				Thread.sleep(1000);

				// if the next call throws exception, then we're probably good -
				// process hasn't finished yet.
				int x = proc.exitValue();
				throw new MojoExecutionException("Process exited with value " + x);
			} catch (IllegalThreadStateException x) {
				if (proc != null) {
					return new Process2(proc);
				} else {
					throw new MojoFailureException("Could not start java process");
				}
			} catch (IOException e) {
				throw new MojoFailureException("Could not start java process", e);
			}
		} catch (InterruptedException e) {
			throw new MojoFailureException("Could not start java process", e);
		}
	}

	/**
	 * Redirect output and error to ours IF the method
	 * {@link ProcessBuilder#inheritIO()} is available (since jdk 1.7)
	 */
	private void redirectIO(ProcessBuilder processBuilder) throws MojoFailureException {
		Class<? extends ProcessBuilder> pbClass = processBuilder.getClass();
		try {
			Method inheritIOMethod = pbClass.getDeclaredMethod("inheritIO");
			inheritIOMethod.invoke(processBuilder);
		} catch (NoSuchMethodException e) {
			// looks like we're not in jdk1.7
			// return
		} catch (InvocationTargetException e) {
			throw new MojoFailureException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}

	private List<String> _D(Map<String, String> d) {
		List<String> result = new ArrayList<String>(d.size());
		for (Map.Entry<String, String> e : d.entrySet()) {
			String value = e.getValue();
			if (value.contains(" ")) {
				value = "\"" + value + "\"";
			}
			result.add(_D(e.getKey(), value));
		}
		return result;
	}

	private String _D(String key, String value) {
		return "-D" + key + '=' + value;
	}
}
