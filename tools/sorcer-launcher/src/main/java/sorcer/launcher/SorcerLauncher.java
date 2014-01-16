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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.output.TeeOutputStream;
import sorcer.resolver.Resolver;
import sorcer.util.Process2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.err;
import static java.lang.System.out;
import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.S_KEY_SORCER_ENV;
import static sorcer.util.JavaSystemProperties.*;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher {

    public static final String WAIT = "wait";
    public static final String HOME = "home";
    public static final String RIO = "rio";
    public static final String LOGS = "logs";
    public static final String DEBUG = "debug";

    enum WaitMode {
        no, start, end
    }

    private WaitMode waitMode;
    private File home;
    private File rio;
    private File logs;
    private String[] args;
    private Integer debugPort;

    protected Process2 sorcerProcess;

    /**
     * -wait=[no,start,end]
     * -logDir {}
     * -home {}
     * -rioHome {} = home/lib/rio
     * <p/>
     * {}... pliki dla ServiceStarter
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        Options options = buildOptions();

        if (args.length == 0) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(80, "sorcer", "Start sorcer", options, null);
        } else {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            SorcerLauncher launcher = new SorcerLauncher();
            String waitValue = cmd.getOptionValue(WAIT);
            try {
                launcher.setWaitMode(cmd.hasOption(WAIT) ? WaitMode.valueOf(waitValue) : WaitMode.start);
            } catch (IllegalArgumentException ignored) {
                out.println("Illegal " + WAIT + " option " + waitValue + ". Use one of " + Arrays.toString(WaitMode.values()));
                System.exit(-1);
            }

            File home = new File(cmd.hasOption(HOME) ? cmd.getOptionValue(HOME) : System.getenv("SORCER_HOME"));
            launcher.setHome(home);

            String rioPath;
            if (cmd.hasOption(RIO)) {
                rioPath = cmd.getOptionValue(RIO);
            } else if ((rioPath = System.getenv("RIO_HOME")) == null)
                rioPath = new File(home, "lib/rio").getPath();
            launcher.setRio(new File(rioPath));

            if(cmd.hasOption(DEBUG))
                launcher.setDebugPort(Integer.parseInt(cmd.getOptionValue(DEBUG)));

            launcher.setLogs(new File(cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : new File(home, "logs").getPath()));
            launcher.setArgs(cmd.getArgs());

            launcher.start();
        }
    }

    private void start() throws IOException, InterruptedException {
        out.println("*******   *******   *******   SORCER launcher   *******   *******   *******");
        out.println("SORCER_HOME = " + home);
        out.println("RIO_HOME    = " + rio);

        SorcerProcessBuilder bld = new SorcerProcessBuilder(home.getPath());
        File errFile = new File(logs, "error.txt");
        File outFile = new File(logs, "output.txt");
        if (waitMode != WaitMode.no) {
//            Pipe pipe = Pipe.open();
//            OutputStream pipeStream = Channels.newOutputStream(pipe.sink());
            bld.setOut(new TeeOutputStream(new FileOutputStream(outFile), out));
            bld.setErr(new TeeOutputStream(new FileOutputStream(errFile), System.err));
        } else {
            bld.setOutFile(outFile);
            bld.setErrFile(errFile);
        }

        bld.setWorkingDir(home);
        bld.setRioHome(rio.getPath());
        bld.setMainClass("sorcer.boot.ServiceStarter");

        File config = new File(home, "configs");
        Map<String, String> defaultSystemProps = new HashMap<String, String>();
        defaultSystemProps.put(UTIL_LOGGING_CONFIG_FILE, new File(config, "sorcer.logging").getPath());
        defaultSystemProps.put(SECURITY_POLICY, new File(config, "sorcer.policy").getPath());
        defaultSystemProps.put(S_KEY_SORCER_ENV, new File(config, "sorcer.env").getPath());
        defaultSystemProps.put(RMI_SERVER_USE_CODEBASE_ONLY, Boolean.FALSE.toString());
        //defaultSystemProps.put(S_WEBSTER_INTERFACE, getInetAddress());
        defaultSystemProps.put(E_RIO_HOME, rio.getPath());

        defaultSystemProps.put(RMI_SERVER_CLASS_LOADER, "org.rioproject.rmi.ResolvingLoader");
        defaultSystemProps.put(PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        defaultSystemProps.put("logback.configurationFile", new File(config, "logback.groovy").getPath());
        //defaultSystemProps.put("logback.configurationFile", new File(config, "logback.xml").getPath());
        defaultSystemProps.put("webster.tmp.dir", new File(home, "databases").getPath());
        defaultSystemProps.put("sorcer.home", home.getPath());

        bld.setProperties(defaultSystemProps);
        bld.setParameters(Arrays.asList(args));
        if (debugPort != null) {
            bld.setDebugger(true);
            bld.setDebugPort(debugPort);
        }

        bld.setClassPath(resolveClassPath(
                "net.jini:jsk-platform",
                "net.jini:jsk-lib",
                "net.jini:jsk-resources",
                "org.apache.river:start",
                "net.jini.lookup:serviceui",

                "org.rioproject:rio-platform",
                "org.rioproject:rio-logging-support",
                "org.rioproject:rio-start",
                "org.rioproject:rio-lib",
                "org.rioproject.resolver:resolver-api",

                "org.sorcersoft.sorcer:sorcer-api",
                "org.sorcersoft.sorcer:sorcer-resolver",
                "org.sorcersoft.sorcer:sos-boot",
                "org.sorcersoft.sorcer:util-rio",
                "org.sorcersoft.sorcer:sos-util",
                "org.sorcersoft.sorcer:sos-webster",
                "org.sorcersoft.sorcer:sos-rio-start",

                "org.codehaus.groovy:groovy-all:2.1.3",
                "com.google.guava:guava:15.0",
                "org.apache.commons:commons-lang3",
                "commons-io:commons-io",

                "org.slf4j:slf4j-api",
                "org.slf4j:jul-to-slf4j:1.7.5",
                "ch.qos.logback:logback-core:1.0.11",
                "ch.qos.logback:logback-classic:1.0.11"
        ));

        sorcerProcess = bld.startProcess();

        if (waitMode != WaitMode.no)
            Runtime.getRuntime().addShutdownHook(new Thread(new Killer(sorcerProcess), "Sorcer shutdown hook"));

        if (waitMode == WaitMode.end)
            sorcerProcess.waitFor();

        if (waitMode == WaitMode.no)
            if (!sorcerProcess.running()) {
                out.println("SORCER not started properly");
                System.exit(sorcerProcess.waitFor());
            }
        //if (waitMode == WaitMode.start)
        //read input, wait for some marker

    }

    protected Collection<String> resolveClassPath(String... artifacts) {
        Set<String> result = new HashSet<String>(artifacts.length);
        for (String artifact : artifacts) {
            result.add(Resolver.resolveAbsolute(artifact));
        }
        return result;
    }

    static class Killer implements Runnable {
        private Process2 process;

        public Killer(Process2 sorcerProcess) {
            process = sorcerProcess;
        }

        @Override
        public void run() {
            if (process != null) {
                if (process.running()) {
                    out.print("Killing SORCER process");
                    int exit = process.destroy();
                    out.println("; exit code = " + exit);
                } else {
                    out.println("SORCER has stopped");
                }
            } else {
                out.println("No SORCER running");
            }
            out.flush();
            err.flush();
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, true, "Wait style, one of:\n\t'no' - don't wait,\n\t'start' - wait until sorcer starts\n\t'end' - wait until sorcer finishes, stopping launcher also stops sorcer");
        wait.setRequired(true);
        wait.setType(WaitMode.class);
        wait.setArgs(1);
        options.addOption(wait);

        Option logs = new Option(LOGS, true, "Directory for logs");
        logs.setType(File.class);
        logs.setArgs(1);
        options.addOption(logs);

        Option home = new Option(HOME, true, "SORCER_HOME variable, read from environment by default");
        home.setArgs(1);
        home.setType(File.class);
        options.addOption(home);

        Option rioHome = new Option(RIO, true, "Force RIO_HOME variable. by default it's read from environment or $SORCER_HOME/lib/rio");
        rioHome.setType(File.class);
        options.addOption(rioHome);

        Option debug = new Option(DEBUG,true,"Add debug option to JVM");
        debug.setType(Boolean.class);
        options.addOption(debug);
        return options;
    }

    public void setWaitMode(WaitMode waitMode) {
        this.waitMode = waitMode;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public void setRio(File rio) {
        this.rio = rio;
    }

    public void setLogs(File logs) {
        this.logs = logs;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }
}
