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
import sorcer.launcher.JavaProcessBuilder;
import sorcer.launcher.Launcher;
import sorcer.launcher.OutputConsumer;
import sorcer.launcher.SorcerOutputConsumer;
import sorcer.resolver.Resolver;
import sorcer.util.Process2;
import sorcer.util.ProcessDownCallback;
import sorcer.util.ProcessMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_EXT;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;

/**
 * @author Rafał Krupiński
 */
public class ForkingLauncher extends Launcher {
    private final static Logger log = LoggerFactory.getLogger(ForkingLauncher.class);

    private File outFile;
    private File errFile;
    private OutputStream out;
    private OutputStream err;

    private Integer debugPort;
    private Process2 process;

    protected void doStart() throws IOException, InterruptedException {
        log.debug("*******   *******   *******   SORCER launcher   *******   *******   *******");

        logDir.mkdirs();

        JavaProcessBuilder bld = new JavaProcessBuilder(home.getPath());

        bld.getEnvironment().putAll(environment);

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

        bld.setProperties(properties);

        if (debugPort != null) {
            bld.setDebugger(true);
            bld.setDebugPort(debugPort);
        }

        Collection<String> classPath = getClassPath();
        bld.setClassPath(classPath);

        bld.setMainClass(MAIN_CLASS);

        bld.setParameters(getConfigs());

        bld.getJavaAgent().put(Resolver.resolveAbsolute("org.rioproject:rio-start"), null);

        process = bld.startProcess();
        sorcerListener.processLaunched(process);

        if (waitMode == WaitMode.no) {
            if (!process.running()) {
                throw new IllegalStateException("SORCER has not started properly; exit value: " + process.exitValue());
            } else
                return;
        }

        installProcessMonitor(sorcerListener, process);

        BufferedReader reader = new BufferedReader(Channels.newReader(pipe.source(), Charset.defaultCharset().name()));
        OutputConsumer consumer = new SorcerOutputConsumer();

        String line;
        while ((line = reader.readLine()) != null) {
            boolean keepGoing = consumer.consume(line);
            if (!keepGoing) break;
        }
        sorcerListener.sorcerStarted();
        log.info("{} has started", process);

        if (waitMode == WaitMode.end) {
            process.waitFor();
        }

    }

    @Override
    public void stop() {
        process.destroy();
    }

    @Override
    protected void configure() {
        if (process != null)
            throw new IllegalStateException("This instance have already started a process");
        super.configure();
    }

    @Override
    protected Map<String, String> getEnvironment() {
        Map<String, String> sysEnv = new HashMap<String, String>();
        sysEnv.put(E_SORCER_HOME, home.getPath());
        sysEnv.put(E_RIO_HOME, rio.getPath());
        sysEnv.put(E_SORCER_EXT, ext.getPath());
        return sysEnv;
    }

    private static OutputStream getStream(OutputStream stream, File file, OutputStream defaultStream) throws FileNotFoundException {
        if (stream != null)
            return stream;
        if (file != null) {
            return new FileOutputStream(file);
        }
        return defaultStream;
    }

    private void installProcessMonitor(ProcessDownCallback callback, Process2 process) {
        ProcessMonitor.install(process, callback, true);
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public void setErr(OutputStream err) {
        this.err = err;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public void setErrFile(File errFile) {
        this.errFile = errFile;
    }

}
