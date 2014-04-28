/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.tools.shell.cmds;

import org.apache.commons.cli.*;
import org.apache.commons.io.output.WriterOutputStream;
import sorcer.core.SorcerEnv;
import sorcer.launcher.*;
import sorcer.launcher.process.DestroyingListener;
import sorcer.launcher.process.ForkingLauncher;
import sorcer.launcher.process.ProcessDestroyer;
import sorcer.tools.shell.INetworkShell;
import sorcer.tools.shell.NetworkShell;
import sorcer.tools.shell.ShellCmd;
import sorcer.util.FileUtils;
import sorcer.util.StringUtils;
import sorcer.util.exec.ExecUtils;
import sorcer.util.exec.ExecUtils.CmdResult;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static sorcer.core.SorcerConstants.SORCER_HOME;

/**
 * Handles system commands
 */
public class BootCmd extends ShellCmd {

    private static final String PROFILE = "P";
    private static final String LOGS = "logs";
    private static final String RIO = "rio";
    private static final String ALL = "all";

    private Options options;

	{
		COMMAND_NAME = "boot, stop";

		NOT_LOADED_MSG = "***command not loaded due to conflict";

        COMMAND_USAGE = "boot [-P <mix|sorcer|rio|node>] " + "[-logs <log dir>]"
                + "\n\t\t\t  [ :<cfg-module> ]"
                + "\n\t\t\t  [ <config files>]"
                + "\n\t\t\t| stop [<id(s) of started node(s)>|all]";


		COMMAND_HELP = "boot [options] [service list files]\n"
                + "\t\t\tService list file may be:\n"
                + "\t\t\t- a .config file with list of service descriptors,\n"
                + "\t\t\t- an .opstring or .groovy Operational String file,\n"
                + "\t\t\t- an .oar or .jar file compliant with OAR specification,\n"
                + "\t\t\t- an :artifactId of a module that output is compliant with OAR\n"
                + "\t\t\t     specification (artifact*jar file is searched under $SORCER_HOME)\n"
                + "\t\t\toptions:\n"
                + "\t\t\t-P - select profile, possible choices are: mix, sorcer, rio, node\n"
                + "\t\t\t-logs <log dir> - logs directory\n"
                +"\nstop [<id of started node>|all]\n"
                +"\t\t\trun without arguments to list currently running instances of service nodes\n"
                +"\t\t\tgive the id(s) of the started instance(s) of a service node(s) to stop\n"
                +"\t\t\tall - kill all running service nodes";
    }

	private String input;

	private PrintStream out;

    private Map<String, ILauncher> startedLaunchers = new LinkedHashMap<String, ILauncher>();
	
	public void execute() throws Throwable {
		INetworkShell shell = NetworkShell.getInstance();
		out = NetworkShell.getShellOutputStream();
	    input = shell.getCmd();
		if (out == null)
			throw new NullPointerException("Must have an output PrintStream");
        Options options = buildOptions();

        CommandLineParser parser = new PosixParser();

        StringTokenizer tok = new StringTokenizer(input);
        List<String> argsList = new ArrayList<String>();
        String cmd = "";
        int i=0;
        while (tok.hasMoreElements()) {
            String arg = tok.nextToken();
            if (i>0) argsList.add(arg);
            else cmd = arg;
            i++;
        }

        CommandLine cmdLine = parser.parse(options, argsList.toArray(new String[0]));

        if (cmd.equalsIgnoreCase("boot")) {

            ILauncher sorcerLauncher = parseCommandLine(cmdLine);

            WaitingListener wait = new WaitingListener();
            sorcerLauncher.addSorcerListener(wait);
            sorcerLauncher.addSorcerListener(new DestroyingListener(ProcessDestroyer.installShutdownHook(), false));
            sorcerLauncher.preConfigure();
            sorcerLauncher.start();
            wait.wait(WaitMode.start);

            startedLaunchers.put(input+" ["+((ForkingLauncher)sorcerLauncher).getPid() + "]", sorcerLauncher);
        } else {
            List<String> idsToStop = cmdLine.getArgList();
            if (cmdLine.hasOption(ALL)) {
                for (ILauncher launcher : startedLaunchers.values())
                    launcher.stop();
            } else if (!idsToStop.isEmpty()) {
                List<Integer> idListToStop = new ArrayList<Integer>();
                for (String idStr : idsToStop) {
                    try {
                        idListToStop.add(Integer.parseInt(idStr));
                    } catch (NumberFormatException ne) {
                        out.println("Could not parse the service node id: " + idStr);
                    }
                }
                int j=0;
                List<String> launchersToRemove = new ArrayList<String>();
                for (String launcherStr: startedLaunchers.keySet()) {
                    if (idListToStop.contains(Integer.valueOf(j))) {
                        out.println("stopping node " + j + " started by command: " + launcherStr);
                        startedLaunchers.get(launcherStr).stop();
                        launchersToRemove.add(launcherStr);
                    }
                    j++;
                }
                for (String launcherStr : launchersToRemove)
                    startedLaunchers.remove(launcherStr);
            } else {
                int j=0;
                for (String launcherStr : startedLaunchers.keySet()) {
                    out.println(j + "\t" + launcherStr);
                    j++;
                }
            }
        }

	}

    private ILauncher parseCommandLine(CommandLine cmd) throws ParseException, IOException {
        ForkingLauncher launcher = new ForkingLauncher();
        launcher.setHome(SorcerEnv.getHomeDir());
        launcher.setErr(out);
        launcher.setOut(out);


        if (launcher == null)
            throw new IllegalStateException("Could not start SORCER");

        File logDir = FileUtils.getFile(SorcerEnv.getHomeDir(), cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : "logs");

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

    private Options buildOptions() {
        Options options = new Options();
        Option logs = new Option(LOGS, true, "Directory for logs");
        logs.setType(File.class);
        logs.setArgs(1);
        logs.setArgName("log-dir");
        options.addOption(logs);

        //see sorcer.launcher.Profile
        Option profile = new Option(PROFILE, "profile", true, "Profile, one of [sorcer, rio, mix] or a path");
        profile.setArgs(1);
        profile.setType(String.class);
        profile.setArgName("profile");
        options.addOption(profile);

        Option rio = new Option(RIO, "List of opstrings to be started by the Rio Monitor (opstring) separated by the system path separator (" + File.pathSeparator + ")");
        rio.setArgs(1);
        rio.setArgName("opstrings");
        rio.setType(String.class);
        options.addOption(rio);

        Option all = new Option(ALL, "List of opstrings to be started by the Rio Monitor (opstring) separated by the system path separator (" + File.pathSeparator + ")");
        all.setArgs(1);
        all.setArgName("all");
        all.setType(String.class);
        options.addOption(all);

        return options;
    }
}
