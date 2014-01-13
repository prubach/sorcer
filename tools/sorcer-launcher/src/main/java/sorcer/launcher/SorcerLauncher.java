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

import java.io.File;

/**
 * @author Rafał Krupiński
 */
public class SorcerLauncher {

    public static final String WAIT = "wait";
    public static final String HOME = "home";
    public static final String RIO = "rio";
    public static final String LOGS = "logs";

    enum WaitMode {
        no, start, end
    }

    private WaitMode waitMode;
    private File home;
    private File rio;
    private File logs;

    /**
     * -wait=[no,start,end]
     * -logDir {}
     * -home {}
     * -rioHome {} = home/lib/rio
     * <p/>
     * {}... pliki dla ServiceStarter
     *
     * @param args
     */
    public static void main(String[] args) throws ParseException {
        Options options = buildOptions();

        if (args.length == 0) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(80, "sorcer", "Start sorcer", options, null);
        } else {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            SorcerLauncher launcher = new SorcerLauncher();
            launcher.setWaitMode(cmd.hasOption(WAIT) ? WaitMode.valueOf(cmd.getOptionValue(WAIT)) : WaitMode.start);

            String homePath = cmd.hasOption(HOME) ? cmd.getOptionValue(HOME) : System.getenv("SORCER_HOME");
            File home = new File(homePath);
            launcher.setHome(home);

            String rioPath;
            if (cmd.hasOption(RIO)) {
                rioPath = cmd.getOptionValue(RIO);
            } else if ((rioPath = System.getenv("RIO_HOME")) == null)
                rioPath = new File(home, "lib/rio").getPath();
            launcher.setRio(new File(rioPath));

            launcher.setLogs(new File(cmd.hasOption(LOGS) ? cmd.getOptionValue(LOGS) : new File(home, "logs").getPath()));
            Runtime.getRuntime().addShutdownHook(new Thread(launcher.new Killer(),"Sorcer shutdown hook"));
        }
    }

    class Killer implements Runnable{
        @Override
        public void run() {
            System.out.println("pif paf");
            SorcerLauncher.this.rio.getPath();
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
}
