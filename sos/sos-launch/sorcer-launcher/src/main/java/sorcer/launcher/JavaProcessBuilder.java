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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.ByteDumper;
import sorcer.util.Process2;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static java.util.Arrays.asList;
import static sorcer.util.Collections.toMap;

/**
 * @author Rafał Krupiński
 */
public class JavaProcessBuilder {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected String name;
    protected Properties properties;
    protected Properties environment = new Properties();
    protected Collection<String> classPathList;
    protected String mainClass;
    protected List<String> parameters = new LinkedList<String>();
    protected File workingDir;
    protected boolean debugger;
    protected String command = "java";
    protected int debugPort = 8000;
    protected Map<String, String> javaAgent = new HashMap<String, String>();

    protected OutputStream out;
    protected OutputStream err;

    protected ThreadGroup ioThreads = new ThreadGroup("Sorcer launcher IO threads");
    private final ProcessBuilder builder = new ProcessBuilder();

    public JavaProcessBuilder(String name) {
        this.name = name;
    }

    public Process2 startProcess() throws IOException {
        if (mainClass == null || mainClass.trim().isEmpty()) {
            throw new IllegalStateException("mainClass must be set");
        }
        if (classPathList == null || classPathList.isEmpty())
            throw new IllegalStateException("Empty Class Path");

        appendJavaOptions();

        builder.command().add(mainClass);

        if (parameters != null) {
            builder.command().addAll(parameters);
        }

        if (workingDir == null) {
            // the default
            // make explicit for logging purpose
            workingDir = new File(System.getProperty("user.dir"));
        }
        builder.directory(workingDir);

        if (log.isInfoEnabled())
            for (String key : environment.stringPropertyNames()) {
                log.info("{} -> {}", key, environment.getProperty(key));
            }

        Map<String, String> procEnv = builder.environment();
        procEnv.putAll(toMap(environment));

        if (log.isInfoEnabled()) {
            StringBuilder cmdStr = new StringBuilder("[").append(workingDir.getPath()).append("]");
            for (String s : builder.command()) {
                cmdStr.append(' ').append(s);
            }
            log.info("{}", cmdStr);
        }

        Process proc = builder.start();
        Process2 p = new Process2(proc, name, ioThreads);
        redirectOutputs(p);

        try {
            // give it a moment to exit on error
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            //ignore
        }

        if (!p.running())
            throw new IllegalStateException("Process exited with value " + proc.exitValue());

        return p;
    }

    private void appendJavaOptions() {
        builder.command(command);

        if (debugger)
            builder.command().addAll(Arrays.asList("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=" + debugPort));

        if (properties != null)
            builder.command().addAll(_D(properties));

        List<String> _classPathList = new ArrayList<String>(classPathList);
        Collections.sort(_classPathList);
        String classPath = StringUtils.join(_classPathList, File.pathSeparator);
        builder.command().addAll(asList("-classpath", classPath));

        for (Map.Entry<String, String> e : javaAgent.entrySet()) {
            StringBuilder buf = new StringBuilder("-javaagent:").append(e.getKey());
            String value = e.getValue();
            if (value != null)
                buf.append('=').append(value);
            builder.command().add(buf.toString());
        }
    }

    private void redirectOutputs(Process proc) {
        if (out != null)
            redirectOutput(proc.getInputStream(), out, proc.toString() + " stdout reader thread");
        if (err != null)
            redirectOutput(proc.getErrorStream(), err, proc.toString() + " stderr reader thread");
    }

    private void redirectOutput(InputStream inputStream, OutputStream outputStream, String name) {
        Thread t = new Thread(ioThreads, new ByteDumper(inputStream, outputStream), name);
        t.setDaemon(true);
        t.start();
    }

    private List<String> _D(Properties d) {
        List<String> result = new ArrayList<String>(d.size());
        for (String key : d.stringPropertyNames())
            result.add(_D(key, d.getProperty(key)));
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

    public void setProperties(Properties properties) {
        this.properties = properties;
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

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public Properties getEnvironment() {
        return environment;
    }

    public void setEnvironment(Properties environment) {
        this.environment = environment;
    }

    public Map<String, String> getJavaAgent() {
        return javaAgent;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
