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
import org.slf4j.LoggerFactory;
import sorcer.launcher.process.DestroyingListener;
import sorcer.launcher.process.ForkingLauncher;
import sorcer.launcher.process.ProcessDestroyer;
import sorcer.util.FileUtils;
import sorcer.util.JavaSystemProperties;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static sorcer.core.SorcerConstants.*;

/**
 * @author Rafał Krupiński
 */
public class Sorcer {
    private static final String WAIT = "w";
    private static final String LOGS = "logs";
    private static final String DEBUG = "debug";
    private static final String PROFILE = "P";
    public static final String MODE = "M";

    /**
     * This method calls {@link java.lang.System#exit(int)} before returning, in case of any remaining non-daemon threads running.
     * There is a java API for starting SORCER in in-process or forked modes with WAIT options, so there shouldn't be any need to call this method from java code.
     * <p/>
     * -wait=[no,start,end]
     * -logDir {}
     * -home {}
     * <p/>
     * {}... config files for ServiceStarter
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        try {
            JavaSystemProperties.ensure(SORCER_HOME);
            Options options = buildOptions();
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp(80, "sorcer", "Start sorcer", options, null);
                return;
            }

            Launcher launcher = parseCommandLine(cmd);

            if (launcher instanceof ForkingLauncher) {
                ForkingLauncher forkingLauncher = (ForkingLauncher) launcher;
                forkingLauncher.setSorcerListener(new DestroyingListener(ProcessDestroyer.installShutdownHook()));
            }

            launcher.start();
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, "wait", true, "Wait style, one of:\n\t'no' - don't wait,\n\t'start' - wait until sorcer starts\n\t'end' - wait until sorcer finishes, stopping launcher also stops sorcer");
        wait.setType(Launcher.WaitMode.class);
        wait.setArgs(1);
        wait.setArgName("wait-mode");
        options.addOption(wait);

        Option logs = new Option(LOGS, true, "Directory for logs");
        logs.setType(File.class);
        logs.setArgs(1);
        logs.setArgName("log-dir");
        options.addOption(logs);

        Option debug = new Option(DEBUG, true, "Add debug option to JVM");
        debug.setType(Boolean.class);
        debug.setArgs(1);
        debug.setArgName("port");
        options.addOption(debug);

        //see sorcer.launcher.Profile
        Option profile = new Option(PROFILE, "profile", true, "Profile, one of [sorcer, rio, mix]");
        profile.setArgs(1);
        profile.setType(String.class);
        profile.setArgName("profile");
        options.addOption(profile);

        Option mode = new Option(MODE, "mode", true, "Select start mode:\n\tdirect - start SORCER in current JVM\n\tfork - start SORCER in a new process\n\tprefer - try different if selected is invalid for some reason\n\tforce - exits process with error if cannot start in selected mode\nValue is case-insensitive");
        mode.setType(Boolean.class);
        mode.setArgs(1);
        mode.setArgName(getModeArgName());
        options.addOption(mode);
        options.addOption("h", "help", false, "Print this help");

        return options;
    }

    private static String getModeArgName() {
        String[] modes = new String[Mode.values().length];
        for (int i = 0; i < modes.length; i++)
            modes[i] = Mode.values()[i].paramValue;
        return "[" + StringUtils.join(modes, "|") + "]";
    }

    private static Launcher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
        Mode mode;
        if (cmd.hasOption(MODE)) {
            String modeValue = cmd.getOptionValue(MODE);
            if (Mode.forceFork.paramValue.equalsIgnoreCase(modeValue))
                mode = Mode.forceFork;
            else if (Mode.preferFork.paramValue.equalsIgnoreCase(modeValue))
                mode = Mode.preferFork;
            else if (Mode.forceDirect.paramValue.equalsIgnoreCase(modeValue))
                mode = Mode.forceDirect;
            else if (Mode.preferDirect.paramValue.equalsIgnoreCase(modeValue))
                mode = Mode.preferDirect;
            else
                throw new IllegalAccessError("Illegal mode " + modeValue);
        } else
            mode = Mode.preferDirect;

        Integer debugPort = null;
        if (cmd.hasOption(DEBUG)) {
            debugPort = Integer.parseInt(cmd.getOptionValue(DEBUG));
        }

        Launcher launcher = null;
        if (!mode.fork) {
            boolean envOk = SorcerLauncher.checkEnvironment();
            if (envOk && debugPort == null) {
                SorcerLauncher.installLogging();
                launcher = new SorcerLauncher();
            } else {
                if (debugPort == null)
                    if (mode.force)
                        throw new IllegalArgumentException("Cannot run in force-direct mode");
                    else
                        LoggerFactory.getLogger(Sorcer.class).warn("Cannot run in force-direct mode");
                else if (mode.force)
                    throw new IllegalArgumentException("Cannot run in force-direct mode with debug");
                else
                    LoggerFactory.getLogger(Sorcer.class).warn("Cannot run in force-direct mode with debug");
            }
        }

        if (launcher == null) {
            ForkingLauncher forkingLauncher = new ForkingLauncher();
            launcher = forkingLauncher;
            if (debugPort != null)
                forkingLauncher.setDebugPort(debugPort);
/*
            File outFile = new File(logDir, "output.log");
            File errFile = new File(logDir, "error.log");
            if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
                forkingLauncher.setOutFile(outFile);
                forkingLauncher.setErrFile(errFile);
            }
            forkingLauncher.setOut(new PrintStream(outFile));
            forkingLauncher.setErr(new PrintStream(errFile));
*/
        }

        String homePath = System.getProperty(SORCER_HOME);
        File home = null;
        if (homePath != null) {
            home = new File(homePath).getCanonicalFile();
            launcher.setHome(home);
        }

        File logDir = FileUtils.getFile(home, cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs");

        launcher.setLogDir(logDir);

        try {
            launcher.setWaitMode(cmd.hasOption(WAIT) ? Launcher.WaitMode.valueOf(cmd.getOptionValue(WAIT)) : Launcher.WaitMode.start);
        } catch (IllegalArgumentException x) {
            throw new IllegalArgumentException("Illegal wait option " + cmd.getOptionValue(WAIT) + ". Use one of " + Arrays.toString(Launcher.WaitMode.values()), x);
        }

        @SuppressWarnings("unchecked")
        List<String> userConfigFiles = cmd.getArgList();
        launcher.setConfigs(userConfigFiles);

        if (cmd.hasOption(PROFILE))
            launcher.setProfile(cmd.getOptionValue(PROFILE));

        return launcher;
    }
}
