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
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import sorcer.launcher.impl.process.DestroyingListener;
import sorcer.launcher.process.ProcessDestroyer;
import sorcer.util.FileUtils;
import sorcer.util.JavaSystemProperties;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final String RIO = "rio";

    private WaitMode waitMode;

    /**
     * There is a java API for starting SORCER in in-process or forked modes with WAIT options, so there shouldn't be any need to call this method from java code.
     */
    public static void main(String[] args) {
        new Sorcer().run(args);
    }

    private void run(String[] args) {
        try {
            JavaSystemProperties.ensure(SORCER_HOME);
            Options options = buildOptions();
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('h')) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp(160,
                        "sorcer-boot [options] [service list files]\n"
                                + "Service list file may be:\n" +
                                "- a .config file with list of service descriptors,\n"
                                + "- an .opstring or .groovy Operational String file,\n"
                                + "- an .oar or .jar file compliant with OAR specification,\n"
                                + "- an :artifactId of a module that output is compliant with OAR specification (artifact*jar file is searched under $SORCER_HOME)",
                        "Start sorcer", options, null
                );
                return;
            }

            ILauncher launcher = parseCommandLine(cmd);
            WaitingListener listener = new WaitingListener();
            launcher.addSorcerListener(listener);
            launcher.preConfigure();

            launcher.start();
            listener.wait(waitMode);
            System.exit(0);
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        Option wait = new Option(WAIT, "wait", true, "Wait style, one of:\n"
                + "'no' - exit immediately, forces forked mode\n"
                + "'start' - wait until sorcer starts, then exit, forces forked mode\n"
                + "'end' - wait until sorcer finishes, stopping launcher also stops SORCER");
        wait.setType(WaitMode.class);
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
        Option profile = new Option(PROFILE, "profile", true, "Profile, one of [sorcer, rio, mix] or a path");
        profile.setArgs(1);
        profile.setType(String.class);
        profile.setArgName("profile");
        options.addOption(profile);

        String modeDesc = "Select start mode, one of:\n"
                + Mode.preferDirect.paramValue + " - default, prefer current JVM, start in forked if debug is enabled or required environment variable is not set\n"
                + Mode.forceDirect.paramValue + " - try current JVM, exit on failure\n"
                + Mode.preferFork.paramValue + " - prefer new process, try in current JVM if cannot run a process (e.g. insufficient privileges)\n"
                + Mode.forceFork.paramValue + " try new process, exit on failure";
        Option mode = new Option(MODE, "mode", true, modeDesc);
        mode.setType(Boolean.class);
        mode.setArgs(1);
        mode.setArgName("mode");
        options.addOption(mode);
        options.addOption("h", "help", false, "Print this help");

        Option rio = new Option(RIO, "List of opstrings to be started by the Rio Monitor (opstring) separated by the system path separator (" + File.pathSeparator + ")");
        rio.setArgs(1);
        rio.setArgName("opstrings");
        rio.setType(String.class);
        options.addOption(rio);

        return options;
    }

    private ILauncher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
        Mode mode = null;
        if (cmd.hasOption(MODE)) {
            String modeValue = cmd.getOptionValue(MODE);
            for (Mode m : Mode.values()) {
                if (m.paramValue.equalsIgnoreCase(modeValue))
                    mode = m;
            }
            if (mode == null)
                throw new IllegalAccessError("Illegal mode " + modeValue);
        } else
            mode = Mode.preferDirect;

        Integer debugPort = null;
        if (cmd.hasOption(DEBUG)) {
            debugPort = Integer.parseInt(cmd.getOptionValue(DEBUG));
        }

        try {
            waitMode = cmd.hasOption(WAIT) ? WaitMode.valueOf(cmd.getOptionValue(WAIT)) : WaitMode.end;
        } catch (IllegalArgumentException x) {
            throw new IllegalArgumentException("Illegal wait option " + cmd.getOptionValue(WAIT) + ". Use one of " + Arrays.toString(WaitMode.values()), x);
        }

        ILauncher launcher = null;
        if (!mode.fork) {
            boolean envOk = SorcerLauncher.checkEnvironment();
            if (envOk && debugPort == null) {
                if (waitMode != WaitMode.end)
                    System.err.println("WARN Starting SORCER with " + waitMode + " mode will result with early exit.");
                launcher = createSorcerLauncher();
            } else {
                if (debugPort != null)
                    report(mode, "Cannot run in {} mode with debug", mode.paramValue);
                else
                    report(mode, "Cannot run in {} mode; see above", mode.paramValue);
            }
        }

        if (launcher == null) {
            IForkingLauncher forkingLauncher = null;
            try {
                forkingLauncher = createForkingLauncher(debugPort, waitMode);
            } catch (IllegalStateException e) {
                if ((mode.fork && mode.force) || (!mode.fork))
                    throw e;
            }
            launcher = forkingLauncher;
        }

        // called prefer-fork but didn't make it
        // fallback to direct
        if (launcher == null && mode.fork && !mode.force)
            launcher = createSorcerLauncher();

        if (launcher == null)
            throw new IllegalStateException("Could not start SORCER");

        String homePath = System.getProperty(SORCER_HOME);
        File home = null;
        if (homePath != null) {
            home = new File(homePath).getCanonicalFile();
            launcher.setHome(home);
        }

        File logDir = FileUtils.getFile(home, cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs");

        launcher.setLogDir(logDir);

        @SuppressWarnings("unchecked")
        List<String> userConfigFiles = cmd.getArgList();
        launcher.setConfigs(userConfigFiles);

        if (cmd.hasOption(PROFILE))
            launcher.setProfile(cmd.getOptionValue(PROFILE));

        if (cmd.hasOption(RIO)) {
            String[] rioConfigs = StringUtils.tokenizerSplit(cmd.getOptionValue(RIO), File.pathSeparator);
            launcher.setRioConfigs(new ArrayList<String>(Arrays.asList(rioConfigs)));
        }

        return launcher;
    }

    private static void report(Mode mode, String message, Object... args) {
        if (mode.force)
            throw new IllegalArgumentException(MessageFormatter.arrayFormat(message, args).getMessage());
        else
            LoggerFactory.getLogger(Sorcer.class).warn(message, args);
    }

    private static IForkingLauncher createForkingLauncher(Integer debugPort, WaitMode mode) {
        IForkingLauncher forkingLauncher;
        try {
            forkingLauncher = (IForkingLauncher) Class.forName("sorcer.launcher.impl.process.ForkingLauncher").newInstance();
            if (debugPort != null)
                forkingLauncher.setDebugPort(debugPort);

            forkingLauncher.addSorcerListener(new DestroyingListener(ProcessDestroyer.installShutdownHook(), mode == WaitMode.start));
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate ForkingLauncher", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access ForkingLauncher", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not instantiate ForkingLauncher", e);
        }
        forkingLauncher.setOut(System.out);
        forkingLauncher.setErr(System.err);
        return forkingLauncher;
    }

    private static ILauncher createSorcerLauncher() {
        SorcerLauncher.installLogging();
        SorcerLauncher launcher = new SorcerLauncher();

        SorcerLauncher.installSecurityManager();
        return launcher;
    }
}
