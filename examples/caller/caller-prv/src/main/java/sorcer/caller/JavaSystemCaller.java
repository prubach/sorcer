package sorcer.caller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.caller.CallerUtil;
import sorcer.service.Context;
import sorcer.service.ContextException;

/**
 * Make a system call through a system shell in a platform-independent manner in Java. <br />
 * This class only demonstrate a 'dir' or 'ls' within current (execution) path, if no parameters are used.
 * If parameters are used, the first one is the system command to execute, the others are its system command parameters. <br />
 * To be system independent, an <b><a href="http://www.allapplabs.com/java_design_patterns/abstract_factory_pattern.htm">
 * Abstract Factory Pattern</a></b> will be used to build the right underlying system shell in which the system command will be executed.
 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
 * @see <a href="http://stackoverflow.com/questions/236737#236873">
   How to make a system call that returns the stdout output as a string in various languages?</a>
 */
public class JavaSystemCaller 
{
	public static StreamGobbler errorGobbler;
	
	public static StreamGobbler outputGobbler;
	
	static private String[] commands;
	
	static private String[] envps;

	static private String[] arguments;

	static private File workingDir;

    private static Logger logger = LoggerFactory.getLogger(JavaSystemCaller.class);
	
  	/**
	 * Execute a system command. <br />
	 * Listen asynchronously to stdout and stderr
	 * @param context system command to be executed (must not be null or empty)
	 * @return final output (stdout only)
	 */
	public static Context execute(Context context) {
			String output = "";
			String[] result = null;
			try
			{			
					commands = CallerUtil.getCmds(context);
					if (commands == null)
						throw new ContextException("No command Provided");

					String strWdir = CallerUtil.getWorkingDir(context);

					if (strWdir != null)
						workingDir = getWorkingDir(strWdir);
					else
						workingDir = null;
					envps = CallerUtil.getEnvp(context);
					arguments = CallerUtil.getArgs(context);
					

					result = new String[commands.length];
					String cmd[] = new String[commands.length];
					String os = CallerUtil.getOS();

					/*******************************************************************
					 * logic for executing the commands
					 ******************************************************************/

						StringBuffer command = new StringBuffer(commands[0]);

					/*	if (arguments != null)
							for (int index = 0; index < arguments.length; index++)
								command.append(" " + arguments[index]);
*/
						cmd[0] = command.toString();

						logger.info(">>>>>>>>>>> CallerImpl::execute() Before exec() cmd = "
                                + cmd[0] + "\n>>>>>>>> os = " + os);

						ExecEnvironmentFactory anExecEnvFactory = getExecEnvironmentFactory(cmd[0], arguments);
						final IShell aShell = anExecEnvFactory.createShell();
						final String aCommandLine = anExecEnvFactory.createCommandLine();

						final Runtime rt = Runtime.getRuntime();
						logger.info("Executing " + aShell.getShellCommand() + " " + aCommandLine);

                        CallerUtil.setStarted(context, new Date());
						
						//final Process proc = rt.exec(aShell.getShellCommand() + " '" + aCommandLine + "'", envps, workingDir);
						final Process proc = rt.exec(aCommandLine, envps, workingDir);
						// any error message?
						errorGobbler = new 
							StreamGobbler(proc.getErrorStream(), "ERROR");			

						// any output?
						outputGobbler = new 
							StreamGobbler(proc.getInputStream(), "OUTPUT");

						// kick them off
						if (errorGobbler.getState().compareTo(Thread.State.NEW)==0) 
							errorGobbler.start();
						if (outputGobbler.getState().compareTo(Thread.State.NEW)==0) 
							outputGobbler.start();

						// any error???
						final int exitVal = proc.waitFor();
						logger.info("ExitValue: " + exitVal);
                        if (exitVal!=0) {
                            context.reportException(new CallerException(exitVal, errorGobbler.getOutput(), aCommandLine));
                        }

						output = outputGobbler.getOutput();
						//if (output.length()>1000)
						//    output = output.substring(output.length()-1000, output.length());
						
						if (output != null)
							result[0] = output;

                        if (output.isEmpty() && !errorGobbler.getOutput().isEmpty())
                            output = errorGobbler.getOutput();
                        CallerUtil.setStopped(context, new Date());
                        CallerUtil.setCallOutput(context, output);

			} catch (sorcer.service.ContextException ce) {
				ce.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			return context;
	}
		
	private static ExecEnvironmentFactory getExecEnvironmentFactory(final String aCommand, final String... someParameters) {
			final String anOSName = System.getProperty("os.name" );
			if(anOSName.toLowerCase().startsWith("windows"))
			{
				return new WindowsExecEnvFactory(aCommand, someParameters);
			}
			return new UnixExecEnvFactory(aCommand, someParameters);
			// TODO be more specific for other OS.
	}
			
	public JavaSystemCaller() { /**/ }
	
	
	private static File getWorkingDir(String dir) {
		String operatingSystem = CallerUtil.getOS();

		if (("linux".equalsIgnoreCase(operatingSystem))
				|| ("SunOS".equalsIgnoreCase(operatingSystem)))
			dir = validatePath("\\", dir);
		else
			dir = validatePath("/", dir);

		return new File(dir);
	}
	
	private static String validatePath(String searchString, String dir) {
		return dir;
	}

/*
	private static void updateContext(Context context, String[] result)
		throws RemoteException {
		try {
			// append the machine name, time taken to execute
			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostName = inetAddress.getHostName();
	
			CallerUtil.setCallOutput(context, result);
			System.out.println(context);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("execute method failed", e);
		}
	}
*/

	
	/**
	 * Asynchronously read the output of a given input stream. <br />
	 * Any exception during execution of the command in managed in this thread.
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public static class StreamGobbler extends Thread
	{
		private InputStream is;
		private String type;
		private StringBuffer output = new StringBuffer();

		StreamGobbler(final InputStream anIs, final String aType)
		{
			this.is = anIs;
			this.type = aType;
		}

		/**
		 * Asynchronous read of the input stream. <br />
		 * Will report output as its its displayed.
		 * @see java.lang.Thread#run()
		 */
		@Override
		public final void run()
		{
			try
			{
				final InputStreamReader isr = new InputStreamReader(this.is);
				final BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
				{
					System.out.println(this.type + ">" + line);
					this.output.append(line+System.getProperty("line.separator"));
				}
			} catch (final IOException ioe)
			{
				ioe.printStackTrace();  
			}
		}
		/**
		 * Get output filled asynchronously. <br />
		 * Should be called after execution
		 * @return final output
		 */
		public final String getOutput()
		{
			return this.output.toString();
		}
	}
	
	/*
	 * ABSTRACT FACTORY PATTERN
	 */
	/**
	 * Environment needed to be build for the Exec class to be able to execute the system command. <br />
	 * Must have the right shell and the right command line. <br />
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public abstract static class ExecEnvironmentFactory
	{
		private String command = null;
		private ArrayList<String> parameters = new ArrayList<String>();
		final String getCommand() { return this.command; }
		final ArrayList<String> getParameters() { return this.parameters; }
		/**
		 * Builds an execution environment for a system command to be played. <br />
		 * Independent from the OS.
		 * @param aCommand system command to be executed (must not be null or empty)
		 * @param someParameters parameters of the command (must not be null or empty)
		 */
		public ExecEnvironmentFactory(final String aCommand, final String... someParameters)
		{
			if(aCommand == null || aCommand.length() == 0) { throw new IllegalArgumentException("Command must not be empty"); }
			this.command = aCommand;
			for (int i = 0; i < someParameters.length; i++) {
				final String aParameter = someParameters[i];
				if(aParameter == null || aParameter.length() == 0) { throw new IllegalArgumentException("Parameter n° '"+i+"' must not be empty"); }
				this.parameters.add(aParameter);
			}
		}
		/**
		 * Builds the right Shell for the current OS. <br />
		 * Allow for independent platform execution.
		 * @return right shell, NEVER NULL
		 */
		public abstract IShell createShell();
		/**
		 * Builds the right command line for the current OS. <br />
		 * Means that a command might be translated, if it does not fit the right OS ('dir' => 'ls' on unix)
		 * @return  right complete command line, with parameters added (NEVER NULL)
		 */
		public abstract String createCommandLine();
		
		protected final String buildCommandLine(final String aCommand, final ArrayList<String> someParameters)
		{
			final StringBuilder aCommandLine = new StringBuilder();
			//aCommandLine.append("'");
			aCommandLine.append(aCommand);
			for (String aParameter : someParameters) {
				aCommandLine.append(" ");
				aCommandLine.append(aParameter);
			}
			//aCommandLine.append("'");
			return aCommandLine.toString();
		}
	}
	
	/**
	 * Builds a Execution Environment for Windows. <br />
	 * Cmd with windows commands
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public static final class WindowsExecEnvFactory extends ExecEnvironmentFactory
	{

		/**
		 * Builds an execution environment for a Windows system command to be played. <br />
		 * Any command not from windows will be translated in its windows equivalent if possible.
		 * @param aCommand system command to be executed (must not be null or empty)
		 * @param someParameters parameters of the command (must not be null or empty)
		 */
		public WindowsExecEnvFactory(final String aCommand, final String... someParameters)
		{
			super(aCommand, someParameters);
		}
		/**
		 * @see JavaSystemCaller.ExecEnvironmentFactory#createShell()
		 */
		@Override
		public IShell createShell() {
			return new WindowsShell();
		}

		/**
		 * @see JavaSystemCaller.ExecEnvironmentFactory#createCommandLine()
		 */
		@Override
		public String createCommandLine() {
			String aCommand = getCommand();
			if(aCommand.toLowerCase().trim().equals("ls")) { aCommand = "dir"; }
			// TODO translates other Unix commands
			return buildCommandLine(aCommand, getParameters());
		}	
	}
	
	/**
	 * Builds a Execution Environment for Unix. <br />
	 * Sh with Unix commands
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public static final class UnixExecEnvFactory extends ExecEnvironmentFactory
	{

		/**
		 * Builds an execution environment for a Unix system command to be played. <br />
		 * Any command not from Unix will be translated in its Unix equivalent if possible.
		 * @param aCommand system command to be executed (must not be null or empty)
		 * @param someParameters parameters of the command (must not be null or empty)
		 */
		public UnixExecEnvFactory(final String aCommand, final String... someParameters)
		{
			super(aCommand, someParameters);
		}
		/**
		 * @see JavaSystemCaller.ExecEnvironmentFactory#createShell()
		 */
		@Override
		public IShell createShell() {
			return new UnixShell();
		}

		/**
		 * @see JavaSystemCaller.ExecEnvironmentFactory#createCommandLine()
		 */
		@Override
		public String createCommandLine() {
			String aCommand = getCommand();
			if(aCommand.toLowerCase().trim().equals("dir")) { aCommand = "ls"; }
			// TODO translates other Windows commands
			return buildCommandLine(aCommand, getParameters());
		}	
	}
	
	/**
	 * System Shell with its right OS command. <br />
	 * 'cmd' for Windows or 'sh' for Unix, ...
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public interface IShell
	{
		/**
		 * Get the right shell command. <br />
		 * Used to launch a new shell
		 * @return command used to launch a Shell (NEVEL NULL)
		 */
		String getShellCommand();
	}
	/**
	 * Windows shell (cmd). <br />
	 * More accurately 'cmd /C'
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public static class WindowsShell implements IShell
	{
		/**
		 * @see JavaSystemCaller.IShell#getShellCommand()
		 */
		public final String getShellCommand() {
			final String osName = System.getProperty("os.name" );
			if( osName.equals( "Windows 95" ) ) { return "command.com /C"; }
			return "cmd.exe /C";
		}
	}
	/**
	 * Unix shell (sh). <br />
	 * More accurately 'sh -C'
	 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
	 */
	public static class UnixShell implements IShell
	{
		/**
		 * @see JavaSystemCaller.IShell#getShellCommand()
		 */
		public final String getShellCommand() {
			return "/bin/sh -c";
		}
	}
}
