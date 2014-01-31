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

package sorcer.launcher.process;

import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.launcher.Launcher;
import sorcer.launcher.OutputConsumer;
import sorcer.launcher.SorcerOutputConsumer;
import sorcer.launcher.SorcerProcessBuilder;
import sorcer.resolver.Resolver;
import sorcer.util.Process2;
import sorcer.util.ProcessDownCallback;
import sorcer.util.ProcessMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static sorcer.core.SorcerConstants.*;
import static sorcer.util.JavaSystemProperties.*;

/**
 * @author Rafał Krupiński
 */
public class ForkingLauncher extends Launcher {
    private final static Logger log = LoggerFactory.getLogger(ForkingLauncher.class);

    private File outFile;
    private File errFile;
    private PrintStream out;
    private PrintStream err;

    private Integer debugPort;

    private List<Process> children = new ArrayList<Process>();

    protected void doStart() throws IOException, InterruptedException {
        log.debug("*******   *******   *******   SORCER launcher   *******   *******   *******");

        logDir.mkdirs();

        SorcerProcessBuilder bld = new SorcerProcessBuilder(home.getPath());

        bld.setRioHome(rio.getPath());
        Map<String, String> env = bld.getEnvironment();
        env.put(E_SORCER_EXT, ext.getPath());

        Pipe pipe = Pipe.open();
        if (waitMode != WaitMode.no) {
            out = getStream(out, outFile, System.out);
            err = getStream(err, errFile, System.err);
            OutputStream pipeStream = Channels.newOutputStream(pipe.sink());
            bld.setOut(new TeeOutputStream(out, pipeStream));
            bld.setErr(new TeeOutputStream(err, pipeStream));
        } else {
            bld.setOutFile(outFile);
            bld.setErrFile(errFile);
        }

        bld.setProperties(getProperties());

        if (debugPort != null) {
            bld.setDebugger(true);
            bld.setDebugPort(debugPort);
        }

        Collection<String> classPath = getClassPath();
        bld.setClassPath(classPath);

        bld.setMainClass(MAIN_CLASS);

        bld.setParameters(getConfigs());

        bld.getJavaAgent().put(Resolver.resolveAbsolute("org.rioproject:rio-start"), null);

        Process2 sorcerProcess = bld.startProcess();
        sorcerListener.processLaunched(sorcerProcess);

        if (waitMode == WaitMode.no) {
            if (!sorcerProcess.running()) {
                throw new IllegalStateException("SORCER has not started properly; exit value: " + sorcerProcess.exitValue());
            } else
                return;
        }
        children.add(sorcerProcess);

        installProcessMonitor(sorcerListener, sorcerProcess);

        BufferedReader reader = new BufferedReader(Channels.newReader(pipe.source(), Charset.defaultCharset().name()));
        OutputConsumer consumer = new SorcerOutputConsumer();

        String line;
        while ((line = reader.readLine()) != null) {
            boolean keepGoing = consumer.consume(line);
            if (!keepGoing) break;
        }
        sorcerListener.sorcerStarted();
        log.info("{} has started", sorcerProcess);

        if (waitMode == WaitMode.end) {
            sorcerProcess.waitFor();
        }

        //avoid killing sorcer on launcher exit
        children.remove(sorcerProcess);

    }

    private static PrintStream getStream(PrintStream stream, File file, PrintStream defaultStream) throws FileNotFoundException {
        if (stream != null)
            return stream;
        if (file != null) {
            return new PrintStream(file);
        }
        return defaultStream;
    }

    private void installProcessMonitor(ProcessDownCallback callback, Process2 process) {
        ProcessMonitor.install(process, callback, true);
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public void setErrFile(File errFile) {
        this.errFile = errFile;
    }

    public void setChildProcesses(List<Process> childProcesses) {
        this.children = childProcesses;
    }

}
