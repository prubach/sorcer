/*
 * Copyright 2014 Sorcersoft.com S.A.
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
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.launcher.process.ExitingCallback;
import sorcer.launcher.process.ForkingLauncher;
import sorcer.launcher.process.ProcessDestroyer;
import sorcer.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;

/**
 * @author Rafał Krupiński
 */
public class Sorcer {
    private static final Logger log = LoggerFactory.getLogger(Sorcer.class);

    private static final String WAIT = "w";
    private static final String HOME = "home";
    private static final String RIO = "rio";
    private static final String LOGS = "logs";
    private static final String DEBUG = "debug";
    private static final String FLAVOUR = "flavour";
    private static final String EXT = "ext";

    /**
     * This method calls {@link java.lang.System#exit(int)} before returning, in case of any remaining non-daemon threads running.
     * There is a java API for starting SORCER in in-process or forked modes with WAIT options, so there shouldn't be any need to call this method from java code.
     * <p/>
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

        try {
            Launcher launcher = parseCommandLine(cmd);

            if (launcher instanceof ForkingLauncher) {
                ForkingLauncher forkingLauncher = (ForkingLauncher) launcher;
                forkingLauncher.setSorcerListener(new ExitingCallback());
                List<Process> children = new ArrayList<Process>();
                installShutdownHook(children);
                forkingLauncher.setChildProcesses(children);
            }

            launcher.start();
        } catch (Exception x) {
            log.error(x.getMessage(), x);
            System.exit(-1);
        }
        System.exit(0);
    }

    private static void installShutdownHook(List<Process> children) {
        Runtime.getRuntime().addShutdownHook(new Thread(new ProcessDestroyer(children), "Sorcer shutdown hook"));
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, "wait", true, "Wait style, one of:\n\t'no' - don't wait,\n\t'start' - wait until sorcer starts\n\t'end' - wait until sorcer finishes, stopping launcher also stops sorcer");
        wait.setType(ForkingLauncher.WaitMode.class);
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

        Option flav = new Option(FLAVOUR, true, "Starting mechanism, on of " + Arrays.asList(Launcher.Flavour.values()));
        flav.setArgs(1);
        flav.setType(ForkingLauncher.Flavour.class);
        flav.setArgName("start-mode");
        options.addOption(flav);

        Option ext = new Option(EXT, true, "SORCER_EXT variable");
        ext.setArgName("ext-dir");
        ext.setArgs(1);
        ext.setType(File.class);
        options.addOption(ext);

        Option proc = new Option("p", "inproc", true, "Allow starting SORCER in the current JVM process; true by default, but only if the environment is properly set");
        proc.setType(Boolean.class);
        proc.setArgs(1);
        proc.setArgName("[true|false]");
        options.addOption(proc);

        options.addOption("h", "help", false, "Print this help");

        return options;
    }

    private static Launcher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
        boolean inProcess = !cmd.hasOption("p") || Boolean.parseBoolean(cmd.getOptionValue("p"));

        Integer debugPort = null;
        if (cmd.hasOption(DEBUG)) {
            debugPort = Integer.parseInt(cmd.getOptionValue(DEBUG));
        }

        String homePath = cmd.hasOption(HOME) ? cmd.getOptionValue(HOME) : System.getenv(E_SORCER_HOME);
        if (homePath == null)
            throw new IllegalArgumentException("No SORCER_HOME defined");
        File home = new File(homePath).getCanonicalFile();
        File logDir = FileUtils.getFile(home, cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs");
        File ext = FileUtils.getFile(home, cmd.hasOption(EXT) ? cmd.getOptionValue(EXT) : homePath);

        Launcher.Flavour flavour = cmd.hasOption(FLAVOUR) ? Launcher.Flavour.valueOf(cmd.getOptionValue(FLAVOUR)) : Launcher.Flavour.sorcer;
        SorcerFlavour sorcerFlavour;
        if (flavour == Launcher.Flavour.rio)
            sorcerFlavour = new RioSorcerFlavour();
        else
            sorcerFlavour = new SorcerSorcerFlavour(home, ext);

        Launcher launcher = null;
        if (inProcess) {
            if (SorcerLauncher.checkEnvironment(sorcerFlavour) && debugPort == null) {
                launcher = new SorcerLauncher();
            }else{
                log.warn("User has requested in-process launch, but it's impossible");
                throw new IllegalStateException("Environment doesn't meet requirements or debugPort set");
            }
        }
        if (launcher == null) {
            ForkingLauncher forkingLauncher = new ForkingLauncher();
            launcher = forkingLauncher;
            if (debugPort != null)
                forkingLauncher.setDebugPort(debugPort);
            File outFile = new File(logDir, "output.log");
            File errFile = new File(logDir, "error.log");
            if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
                forkingLauncher.setOutFile(outFile);
                forkingLauncher.setErrFile(errFile);
            }
            forkingLauncher.setOut(new PrintStream(outFile));
            forkingLauncher.setErr(new PrintStream(errFile));
        }

        launcher.setLogDir(logDir);

        launcher.setHome(home);
        launcher.setConfigDir(new File(home, "configs"));

        try {
            launcher.setWaitMode(cmd.hasOption(WAIT) ? Launcher.WaitMode.valueOf(cmd.getOptionValue(WAIT)) : Launcher.WaitMode.start);
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Illegal wait option " + cmd.getOptionValue(WAIT) + ". Use one of " + Arrays.toString(Launcher.WaitMode.values()));
        }

        String rioPath;
        if (cmd.hasOption(RIO)) {
            rioPath = cmd.getOptionValue(RIO);
        } else if ((rioPath = System.getenv(E_RIO_HOME)) == null)
            rioPath = "lib/rio";
        launcher.setRio(FileUtils.getFile(home, rioPath));

        launcher.setExt(ext.getCanonicalFile());

        launcher.setFlavour(sorcerFlavour);

        launcher.setConfigs(cmd.getArgList());
        return launcher;
    }
}
