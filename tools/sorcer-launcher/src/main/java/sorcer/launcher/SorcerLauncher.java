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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.resolver.Resolver;
import sorcer.util.IOUtils;
import sorcer.util.Process2;
import sorcer.util.ProcessMonitor;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.System.err;
import static java.lang.System.out;
import static sorcer.core.SorcerConstants.*;
import static sorcer.util.JavaSystemProperties.*;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher {
    private final static Logger log = LoggerFactory.getLogger(SorcerLauncher.class);

    public static final String WAIT = "wait";
    public static final String HOME = "home";
    public static final String RIO = "rio";
    public static final String LOGS = "logs";
    public static final String DEBUG = "debug";
    public static final String FLAVOUR = "flavour";

    enum WaitMode {
        no, start, end
    }

    enum Flavour {
        sorcer,
        rio
    }

    private WaitMode waitMode;
    private File home;
    private File rio;
    private File logs;
    private List<String> args;
    private Integer debugPort;
    private Flavour flavour;
    private boolean quiet;

    protected Process2 sorcerProcess;
    private ProcessDestroyer processDestroyer;

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
                log.error("Illegal wait option {}. Use one of {}", waitValue, Arrays.toString(WaitMode.values()));
                System.exit(-1);
            }

            File home = new File(cmd.hasOption(HOME) ? cmd.getOptionValue(HOME) : System.getenv(E_SORCER_HOME));
            launcher.setHome(home);

            String rioPath;
            if (cmd.hasOption(RIO)) {
                rioPath = cmd.getOptionValue(RIO);
            } else if ((rioPath = System.getenv(E_RIO_HOME)) == null)
                rioPath = "lib/rio";
            launcher.setRio(new File(home, rioPath));

            if (cmd.hasOption(DEBUG))
                launcher.setDebugPort(Integer.parseInt(cmd.getOptionValue(DEBUG)));

            launcher.setLogs(new File(cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : new File(home, "logs").getPath()));
            launcher.setFlavour(cmd.hasOption(FLAVOUR) ? Flavour.valueOf(cmd.getOptionValue(FLAVOUR)) : Flavour.sorcer);
            launcher.setQuiet(cmd.hasOption('q'));

            launcher.setArgs(cmd.getArgList());

            try {
                launcher.start();
            } catch (IllegalStateException x) {
                log.error("Child process immediately died",x);
                System.exit(-1);
            }
        }
        System.exit(0);
    }

    private void start() throws IOException, InterruptedException {
        log.debug("*******   *******   *******   SORCER launcher   *******   *******   *******");

        File config = new File(home, "configs");

        logs.mkdirs();
        System.setProperty(SORCER_HOME, home.getPath());
        System.setProperty(S_KEY_SORCER_ENV, new File(config, "sorcer.env").getPath());

        SorcerProcessBuilder bld = new SorcerProcessBuilder(home.getPath());
        bld.setWorkingDir(home);
        bld.setRioHome(rio.getPath());

        File errFile = new File(logs, "error.txt");
        File outFile = new File(logs, "output.txt");
        Pipe pipe = Pipe.open();
        if (waitMode != WaitMode.no) {
            OutputStream pipeStream = Channels.newOutputStream(pipe.sink());
            bld.setOut(new TeeOutputStream(new FileOutputStream(outFile), pipeStream));
            bld.setErr(new TeeOutputStream(new FileOutputStream(errFile), pipeStream));
        } else {
            bld.setOutFile(outFile);
            bld.setErrFile(errFile);
        }

        bld.setProperties(getProperties(config));

        if (debugPort != null) {
            bld.setDebugger(true);
            bld.setDebugPort(debugPort);
        }

        SorcerFlavour sorcerFlavour;
        if (flavour == Flavour.rio)
            sorcerFlavour = new RioSorcerFlavour();
        else
            sorcerFlavour = new SorcerSorcerFlavour();

        Collection<String> classPath = resolveClassPath(sorcerFlavour.getClassPath());
        classPath.addAll(sorcerFlavour.getNonResolvableClassPath());
        bld.setClassPath(classPath);

        bld.setMainClass(sorcerFlavour.getMainClass());

        List<String> configs = args.isEmpty() ? sorcerFlavour.getDefaultConfigs() : args;
        bld.setParameters(configs);

        bld.getJavaAgent().put(Resolver.resolveAbsolute("org.rioproject:rio-start"), null);

        sorcerProcess = bld.startProcess();

        if (waitMode == WaitMode.no) {
            if (!sorcerProcess.running()) {
                out.println("SORCER not started properly");
                System.exit(sorcerProcess.exitValue());
            } else
                System.exit(0);
        } else {
            //install shutdown hook also in start mode, so sorcer can be stopped with ^C before it finishes starting
            processDestroyer = new ProcessDestroyer(sorcerProcess, "SORCER");
            installShutdownHook();
            installProcessMonitor();
        }

        BufferedReader reader = new BufferedReader(Channels.newReader(pipe.source(), Charset.defaultCharset().name()));
        OutputConsumer consumer = sorcerFlavour.getConsumer();

        String line;
        while ((line = reader.readLine()) != null) {
            if (!quiet) out.println(line);
            boolean starting = consumer.consume(line);
            if (!starting) break;
        }

        if (waitMode == WaitMode.start) {
            //don't kill sorcer on launcher exit
            processDestroyer.setEnabled(false);
        }
    }

    private Map<String, String> getProperties(File config) {
        Map<String, String> systemProps = new HashMap<String, String>();
        systemProps.put(RMI_SERVER_USE_CODEBASE_ONLY, Boolean.FALSE.toString());
        systemProps.put(PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        systemProps.put(MAX_DATAGRAM_SOCKETS, "1024");

        systemProps.put(SORCER_HOME, home.getPath());
        systemProps.put(S_WEBSTER_TMP_DIR, new File(home, "databases").getPath());
        systemProps.put(SECURITY_POLICY, new File(config, "sorcer.policy").getPath());
        systemProps.put(S_KEY_SORCER_ENV, new File(config, "sorcer.env").getPath());
        //systemProps.put(S_WEBSTER_INTERFACE, getInetAddress());

        systemProps.put(E_RIO_HOME, rio.getPath());
        systemProps.put("RIO_LOG_DIR", logs.getPath());
        systemProps.put(RMI_SERVER_CLASS_LOADER, "org.rioproject.rmi.ResolvingLoader");

        systemProps.put(UTIL_LOGGING_CONFIG_FILE, new File(config, "sorcer.logging").getPath());
        systemProps.put("logback.configurationFile", new File(config, "logback.groovy").getPath());

        //rio specific
        systemProps.put("org.rioproject.service", "all");
        return systemProps;
    }

    interface OutputConsumer {
        public boolean consume(String line);
    }

    private void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(processDestroyer, "Sorcer shutdown hook"));
    }

    private void installProcessMonitor() {
        ProcessMonitor.install(sorcerProcess, new ProcessMonitor.ProcessDownCallback() {
            @Override
            public void processDown(Process process) {
                out.println("Sorcer is down, closing launcher");
                System.exit(-1);
            }
        }, true);
    }

    protected Collection<String> resolveClassPath(List<String> artifacts) {
        Set<String> result = new HashSet<String>(artifacts.size());
        try {
            for (String artifact : artifacts) {
                String p = Resolver.resolveAbsolute(artifact);
                IOUtils.ensureFile(new File(p));
                result.add(p);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, true, "Wait style, one of:\n\t'no' - don't wait,\n\t'start' - wait until sorcer starts\n\t'end' - wait until sorcer finishes, stopping launcher also stops sorcer");
        wait.setRequired(true);
        wait.setType(WaitMode.class);
        wait.setArgs(1);
        wait.setArgName("wait-mode");
        options.addOption(wait);

        Option logs = new Option(LOGS, true, "Directory for logs");
        logs.setType(File.class);
        logs.setArgs(1);
        logs.setArgName("dir");
        options.addOption(logs);

        Option home = new Option(HOME, true, "SORCER_HOME variable, read from environment by default");
        home.setArgs(1);
        home.setType(File.class);
        home.setArgName("dir");
        options.addOption(home);

        Option rioHome = new Option(RIO, true, "Force RIO_HOME variable. by default it's read from environment or $SORCER_HOME/lib/rio");
        rioHome.setType(File.class);
        rioHome.setArgs(1);
        rioHome.setArgName("dir");
        options.addOption(rioHome);

        Option debug = new Option(DEBUG, true, "Add debug option to JVM");
        debug.setType(Boolean.class);
        debug.setArgs(1);
        debug.setArgName("port");
        options.addOption(debug);

        Option flav = new Option(FLAVOUR, true, "Starting mechanism, either sorcer or rio");
        flav.setArgs(1);
        flav.setType(Flavour.class);
        flav.setArgName("start-mode");
        options.addOption(flav);

        //Option ext

        options.addOption(new Option("q", "quiet", false, "Don't pass SORCER's output to Launcher's console"));

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

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public void setDebugPort(Integer debugPort) {
        this.debugPort = debugPort;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}
