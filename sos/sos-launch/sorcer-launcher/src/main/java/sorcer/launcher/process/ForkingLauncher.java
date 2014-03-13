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
import sorcer.core.SorcerEnv;
import sorcer.launcher.*;
import sorcer.resolver.Resolver;
import sorcer.util.Process2;
import sorcer.util.ProcessDownCallback;
import sorcer.util.ProcessMonitor;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_EXT;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;

/**
 * @author Rafał Krupiński
 */
public class ForkingLauncher extends Launcher implements IForkingLauncher {
    private final static Logger log = LoggerFactory.getLogger(ForkingLauncher.class);
    final protected static String MAIN_CLASS = "sorcer.launcher.Sorcer";

    private File outFile;
    private File errFile;
    private OutputStream out;
    private OutputStream err;

    private Integer debugPort;
    private Process2 process;
    private String profile;

    @Override
    public void start() throws IOException {
        if (process != null)
            throw new IllegalStateException("This instance has already started a process");

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

        bld.getJavaAgent().put(Resolver.resolveAbsolute("org.rioproject:rio-start"), null);

        bld.setMainClass(MAIN_CLASS);

        List<String> parameters = bld.getParameters();

        //-Mforce-direct enforces SorcerLauncher, so we ensure that there is no loop ForkingLauncher->Sorcer->ForkingLauncher
        parameters.add("-M");
        parameters.add(Mode.forceDirect.paramValue);

        if (profile != null) {
            parameters.add("-P");
            parameters.add(profile);
        }

        //last parameters
        if (configs != null)
            parameters.addAll(configs);

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
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                log.warn("Interrupted", e);
            }
        }

        String pidFile = null;
        if ((profile!=null) && (process.getPid()!=-1) &&
                (waitMode== WaitMode.start || waitMode== WaitMode.no)) {
            try
            {
                pidFile = SorcerEnv.getHomeDir() + File.separator + "logs" + File.separator + "sorcer.pid";
                File fPidFile = new File(pidFile);
                BufferedWriter writer = new BufferedWriter(new FileWriter(fPidFile));
                writer.write(new Integer(process.getPid()).toString() + "\n");
                writer.close();
            } catch(Exception e) {
                log.warn("Cannot write pid to file: " + pidFile);
            }
        }
    }

    @Override
    public void stop() {
        process.destroy();
    }

    @Override
    protected Properties getEnvironment() {
        Properties sysEnv = new Properties();
        sysEnv.put(E_SORCER_HOME, home.getPath());
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

    @Override
    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }

    @Override
    public void setOut(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErr(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    @Override
    public void setErrFile(File errFile) {
        this.errFile = errFile;
    }

    @Override
    public void setProfile(String profile) {
        this.profile = profile;
    }
}
