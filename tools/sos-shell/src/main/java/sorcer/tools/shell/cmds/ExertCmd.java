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

import sorcer.core.context.ControlContext.ThrowableTrace;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.tools.shell.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ExertCmd extends ShellCmd {

	{
		COMMAND_NAME = "exert";

		NOT_LOADED_MSG = "***command not loaded due to conflict";

		COMMAND_USAGE = "exert [-cc] [[-s | --s | --m] <output filename>] <input filename>";

		COMMAND_HELP = "Manage and execute the federation of services specified by the <input filename>;"
				+ "\n  -cc   print the executed exertion with control dataContext"
				+ "\n  -s   save the command output in a file"
				+ "\n  --s   serialize the command output in a file"
				+ "\n  --m   marshal the the command output in a file";
	}

	private final static Logger logger = Logger.getLogger(ExertCmd.class
			.getName());

    private ScriptExerter scriptExerter;

	private String input;

	private PrintStream out;

	private File outputFile;

	private File scriptFile;

	private String script;

    private Object result;

    private Object target;

	private static StringBuilder staticImports;

	public ExertCmd() {
        out = NetworkShell.getShellOutputStream();
        scriptExerter = new ScriptExerter(out, ShellStarter.getLoader());
	}

	public void execute() throws Throwable {
		NetworkShell shell = NetworkShell.getInstance();
        input = shell.getCmd();
		if (out == null)
			throw new NullPointerException("Must have an output PrintStream");

		File d = NetworkShell.getInstance().getCurrentDir();
		String nextToken = null;
		String scriptFilename = null;
		boolean outPersisted = false;
		boolean outputControlContext = false;
		boolean marshalled = false;
		boolean commandLine = NetworkShell.isCommandLine();
		StringTokenizer tok = new StringTokenizer(input);
		if (tok.countTokens() >= 1) {
			while (tok.hasMoreTokens()) {
				nextToken = tok.nextToken();
				if (nextToken.equals("-s")) {
					outPersisted = true;
					outputFile = new File("" + d + File.separator + nextToken);
				} else if (nextToken.equals("-controlContext"))
					outputControlContext = true;
				else if (nextToken.equals("-m"))
					marshalled = true;
				// evaluate text
				else if (nextToken.equals("-t")) {
					if (script == null || script.length() == 0) {
						throw new NullPointerException("Must have not empty script");
					}
				}
				// evaluate file script
				else if (nextToken.equals("-f"))
					scriptFilename = nextToken;
				else
					scriptFilename = nextToken;
			}
		} else {
			out.println(COMMAND_USAGE);
			return;
		}
		StringBuilder sb = null;
        if (script != null) {
            scriptExerter.readScriptWithHeaders(script);
		} else if (scriptFilename != null) {
			if ((new File(scriptFilename)).isAbsolute()) {
				scriptFile = NetworkShell.huntForTheScriptFile(scriptFilename);
			} else {
				scriptFile = NetworkShell.huntForTheScriptFile("" + d
						+ File.separator + scriptFilename);
			}
			try {
                scriptExerter.readFile(scriptFile);
			} catch (IOException e) {
				out.append("File: " + scriptFile.getAbsolutePath() + " could not be found or read: " + e.getMessage());
			}
		} else {
			out.println("Missing exertion input filename!");
			return;
		}
        Object target = scriptExerter.parse();
        Object result = scriptExerter.execute();
        // System.out.println(">>>>>>>>>>> result: " + result);
		if (result != null) {
			if (!(result instanceof Exertion)) {
				out.println("\n---> EVALUATION RESULT --->");
				out.println(result);
				return;
			}
			Exertion xrt = (Exertion) result;
			if (xrt.getExceptions().size() > 0) {
				if (commandLine) {
					out.println("Exceptions: ");
					out.println(xrt.getExceptions());
				} else {
					List<ThrowableTrace> ets = xrt.getExceptions();
					for (ThrowableTrace t : ets) {
						System.err.println(t.describe());
					}
				}
			}
			out.println("\n---> OUTPUT EXERTION --->");
			out.println(((ServiceExertion) xrt).describe());
			out.println("\n---> OUTPUT DATA CONTEXT --->");
			if (xrt.isJob())
				out.println(((Job)xrt).getJobContext());
			else
				out.println(xrt.getDataContext());
			if (outputControlContext) {
				out.println("\n---> OUTPUT CONTROL CONTEXT --->");
				out.println(xrt.getControlContext());
			}
		} else {
			if (target != null) {
				out.println("\n--- Failed to excute exertion ---");
				out.println(((ServiceExertion) target).describe());
				out.println(((ServiceExertion) target).getDataContext());
				if (!commandLine) {
					out.println("Script failed: " + scriptFilename);
					out.println(script);
				}
			}
			// System.out.println(">>> executing script: \n" + sb.toString());
		}
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

    public static class NetletFileParser {
        public String result;
        public List<String> loadLines;
        public List<String> codebaseLines;

        public NetletFileParser(String result, List<String> loadLines, List<String> codebaseLines ) {
            this.result = result;
            this.loadLines = loadLines;
            this.codebaseLines = codebaseLines;
        }

    }


	public static NetletFileParser readFile(File file) throws IOException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        List<String> loadLines = new ArrayList<String>();
        List<String> codebaseLines = new ArrayList<String>();
		// String lineSep = System.getProperty("line.separator");
		String lineSep = "\n";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String nextLine = "";
		StringBuffer sb = new StringBuffer();
		nextLine = br.readLine();
		// skip shebang line
		if (nextLine.indexOf("#!") < 0) {
			sb.append(nextLine);
			sb.append(lineSep);
		}
		while ((nextLine = br.readLine()) != null) {
            // Check for "load" of jars
            if (nextLine.trim().startsWith(LoaderConfigurationHelper.LOAD_PREFIX)) {
                loadLines.add(nextLine.trim());
            } else if (nextLine.trim().startsWith(LoaderConfigurationHelper.CODEBASE_PREFIX)) {
                codebaseLines.add(nextLine.trim());
            } else {
                sb.append(nextLine);
                sb.append(lineSep);
            }
		}
        return new NetletFileParser(sb.toString(), loadLines, codebaseLines);
	}

	private StringBuilder readTextFromJar(String filename) {
		InputStream is = null;
		BufferedReader br = null;
		String line;
		StringBuilder sb = new StringBuilder();

		try {
			is = getClass().getClassLoader().getResourceAsStream(filename);
            logger.finest("Loading " + filename + " from is: " + is);
			if (is != null) {
				br = new BufferedReader(new InputStreamReader(is));
				while (null != (line = br.readLine())) {
					sb.append(line);
					sb.append("\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb;
	}

}
