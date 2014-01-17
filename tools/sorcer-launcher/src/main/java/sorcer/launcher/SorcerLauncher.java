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

import org.apache.commons.cli.*;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.resolver.Resolver;
import sorcer.util.*;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.*;

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
    private static final String EXT = "ext";

    public SorcerLauncher(File home) {
        this.home = home;
    }

    enum WaitMode {
        no, start, end
    }

    enum Flavour {
        sorcer,
        rio
    }

    private WaitMode waitMode;
    private File home;
    private File ext;
    private File rio;
    private File logDir;

    private File outFile;
    private File errFile;
    private PrintStream out;
    private PrintStream err;

    private List<String> args;
    private Integer debugPort;
    private Flavour flavour;

    protected Process2 sorcerProcess;
    private List<Process> children = new ArrayList<Process>();

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
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(80, "sorcer", "Start sorcer", options, null);
            return;
        }

        SorcerLauncher launcher = parseCommandLine(cmd);

        List<Process> children = new ArrayList<Process>();
        launcher.setChildProcesses(children);
        installShutdownHook(children);

        try {
            launcher.start(new ExitingCallback());
        } catch (Exception x) {
            log.error(x.getMessage(), x);
            System.exit(-1);
        }

        System.exit(0);
    }

    static class ExitingCallback implements ProcessDownCallback {
        @Override
        public void processDown(Process process) {
            log.info("{} is down, closing launcher", process);
            System.exit(-1);
        }
    }

    public Process2 start(ProcessDownCallback callback) throws IOException, InterruptedException {
        log.debug("*******   *******   *******   SORCER launcher   *******   *******   *******");

        File config = new File(home, "configs");

        logDir.mkdirs();
        System.setProperty(SORCER_HOME, home.getPath());
        System.setProperty(S_KEY_SORCER_ENV, new File(config, "sorcer.env").getPath());

        SorcerProcessBuilder bld = new SorcerProcessBuilder(home.getPath());
        //bld.setWorkingDir(home);
        bld.setRioHome(rio.getPath());
        bld.getEnvironment().put(E_SORCER_EXT, ext.getPath());

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
                throw new IllegalStateException("SORCER has not started properly; exit value: " + sorcerProcess.exitValue());
            } else
                return sorcerProcess;
        }
        children.add(sorcerProcess);

        if (callback != null)
            installProcessMonitor(callback, sorcerProcess);

        BufferedReader reader = new BufferedReader(Channels.newReader(pipe.source(), Charset.defaultCharset().name()));
        OutputConsumer consumer = sorcerFlavour.getConsumer();

        String line;
        while ((line = reader.readLine()) != null) {
            boolean starting = consumer.consume(line);
            if (!starting) break;
        }

        if (waitMode == WaitMode.start) {
            //don't kill sorcer on launcher exit
            children.remove(sorcerProcess);
        }
        return sorcerProcess;
    }

    private static PrintStream getStream(PrintStream stream, File file, PrintStream defaultStream) throws FileNotFoundException {
        if (stream != null)
            return stream;
        if (file != null) {
            return new PrintStream(file);
        }
        return defaultStream;
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, true, "Wait style, one of:\n\t'no' - don't wait,\n\t'start' - wait until sorcer starts\n\t'end' - wait until sorcer finishes, stopping launcher also stops sorcer");
        wait.setType(WaitMode.class);
        wait.setArgs(1);
        wait.setArgName("wait-mode");
        options.addOption(wait);

        Option logs = new Option(LOGS, true, "Directory for logs");
        logs.setType(File.class);
        logs.setArgs(1);
        logs.setArgName("log-dir");
        options.addOption(logs);

        Option home = new Option(HOME, true, "SORCER_HOME variable, read from environment by default");
        home.setArgs(1);
        home.setType(File.class);
        home.setArgName("home-dir");
        options.addOption(home);

        Option rioHome = new Option(RIO, true, "Force RIO_HOME variable. by default it's read from environment or $SORCER_HOME/lib/rio");
        rioHome.setType(File.class);
        rioHome.setArgs(1);
        rioHome.setArgName("rio-dir");
        options.addOption(rioHome);

        Option debug = new Option(DEBUG, true, "Add debug option to JVM");
        debug.setType(Boolean.class);
        debug.setArgs(1);
        debug.setArgName("port");
        options.addOption(debug);

        Option flav = new Option(FLAVOUR, true, "Starting mechanism, on of " + Arrays.asList(Flavour.values()));
        flav.setArgs(1);
        flav.setType(Flavour.class);
        flav.setArgName("start-mode");
        options.addOption(flav);

        Option ext = new Option(EXT, true, "SORCER_EXT variable");
        ext.setArgName("ext-dir");
        ext.setArgs(1);
        ext.setType(File.class);
        options.addOption(ext);

        options.addOption("h", "help", false, "Print this help");

        return options;
    }

    private static SorcerLauncher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
        String homePath = cmd.hasOption(HOME) ? cmd.getOptionValue(HOME) : System.getenv(E_SORCER_HOME);
        if (homePath == null)
            throw new IllegalArgumentException("No SORCER_HOME defined");
        File home = new File(homePath).getCanonicalFile();
        SorcerLauncher launcher = new SorcerLauncher(home);

        try {
            launcher.setWaitMode(cmd.hasOption(WAIT) ? WaitMode.valueOf(cmd.getOptionValue(WAIT)) : WaitMode.start);
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Illegal wait option " + cmd.getOptionValue(WAIT) + ". Use one of " + Arrays.toString(WaitMode.values()));
        }

        String rioPath;
        if (cmd.hasOption(RIO)) {
            rioPath = cmd.getOptionValue(RIO);
        } else if ((rioPath = System.getenv(E_RIO_HOME)) == null)
            rioPath = "lib/rio";
        launcher.setRio(FileUtils.getFile(home, rioPath));

        String extPath = cmd.hasOption(EXT) ? cmd.getOptionValue(EXT) : homePath;
        launcher.setExt(FileUtils.getFile(home, extPath).getCanonicalFile());

        if (cmd.hasOption(DEBUG))
            launcher.setDebugPort(Integer.parseInt(cmd.getOptionValue(DEBUG)));

        String logPath = cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs";
        launcher.setLogDir(FileUtils.getFile(home, logPath));
        File outFile = new File(logPath, "output.log");
        File errFile = new File(logPath, "error.log");
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
            launcher.setOutFile(outFile);
            launcher.setErrFile(errFile);
        } else {
            launcher.setOut(new PrintStream(outFile));
            launcher.setErr(new PrintStream(errFile));
        }

        launcher.setFlavour(cmd.hasOption(FLAVOUR) ? Flavour.valueOf(cmd.getOptionValue(FLAVOUR)) : Flavour.sorcer);

        launcher.setArgs(cmd.getArgList());
        return launcher;
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
        systemProps.put("RIO_LOG_DIR", logDir.getPath());
        systemProps.put(RMI_SERVER_CLASS_LOADER, "org.rioproject.rmi.ResolvingLoader");

        systemProps.put(UTIL_LOGGING_CONFIG_FILE, new File(config, "sorcer.logging").getPath());
        systemProps.put("logback.configurationFile", new File(config, "logback.groovy").getPath());

        //rio specific
        systemProps.put("org.rioproject.service", "all");
        return systemProps;
    }

    private static void installShutdownHook(List<Process> children) {
        Runtime.getRuntime().addShutdownHook(new Thread(new ProcessDestroyer(children), "Sorcer shutdown hook"));
    }

    private void installProcessMonitor(ProcessDownCallback callback, Process2 process) {
        ProcessMonitor.install(process, callback, true);
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

    public void setWaitMode(WaitMode waitMode) {
        this.waitMode = waitMode;
    }

    public void setRio(File rio) {
        this.rio = rio;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
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

    public void setExt(File ext) {
        this.ext = ext;
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
