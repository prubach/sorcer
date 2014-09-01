/*
 * Written by Dawid Kurzyniec and released to the public domain, as explained
 * at http://creativecommons.org/licenses/publicdomain
 */

package sorcer.util.exec;

import java.io.*;
import sorcer.util.GenericUtil;
import sorcer.util.StringUtils;

import static sorcer.util.StringUtils.tName;

/**
 * Utility methods to interact with and manage native processes started from
 * Java.
 *
 * @author Dawid Kurzyniec
 * @author updated for SORCER by Mike Sobolewski
 * @version 1.0
 */
public class ExecUtils {

    private ExecUtils() {}

    /**
     * Execute specified command and return its results. Waits for the command
     * to complete and returns its completion status and data written to
     * standard output and error streams. The process' standard input is set
     * to EOF. Example:
     *
     * <pre>
     * System.out.println(ExecUtils.execCommand("/bin/ls").getOut());
     * </pre>
     *
     * @param cmd the command to execute
     * @return the results of the command execution
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before command
     *         completes
     */
    public static CmdResult execCommand(String cmd)
        throws IOException, InterruptedException
    {
		if (GenericUtil.isWindows()) {
            return execCommandWin(cmd);
		}
        return execCommand(Runtime.getRuntime().exec(cmd));
    }

    // Windows-specific execCommand
    public static CmdResult execCommandWin(String cmd) throws IOException, InterruptedException {
        return execCommand(Runtime.getRuntime().exec(new String[]{"cmd", "/C", cmd}));
    }

    /**
     * Attach to the specified process and return its results.
     * Waits for the process to complete and returns its completion status and
     * data written to standard output and error streams. The process' standard
     * input is set to EOF. Example:
     *
     * <pre>
     * Process p = runtime.exec("/bin/ls");
     * System.out.println(ExecUtils.execCommand(p).getOut());
     * </pre>
     *
     * @param process the process to attach to
     * @return the results of the process
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before process
     *         ends
     */
    public static CmdResult execCommand(Process process)
        throws IOException, InterruptedException
    {
        return execCommand(process, new NullInputStream());
    }

    /**
     * Attach to the specified process, feed specified standard input,
     * and return process' results.
     * Waits for the process to complete and returns completion status and data
     * written to standard output and error streams.
     *
     * @see #execCommand(Process)
     *
     * @param process the process to attach to
     * @param stdin the data to redirect to process' standard input
     * @return the results of the process
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before process
     *         ends
     */
    public static CmdResult execCommand(final Process process, final InputStream stdin)
        throws IOException, InterruptedException
    {
        return execCommand(process, stdin, false);
    }

    /**
     * Attach to the specified process, feed specified standard input, and
     * return process' results. Waits for the process to complete and returns
     * completion status and data written to standard output and error streams.
     *
     * @see #execCommand(Process)
     *
     * @param process
     *            the process to attach to
     * @param stdin
     *            the data to redirect to process' standard input
     * @return the results of the process
     * @throws IOException
     *             if an I/O error occurs
     * @throws InterruptedException
     *             if thread is interrupted before process ends
     */
    public static CmdResult execCommand(final Process process,
                                        final InputStream stdin, boolean outLogged) throws IOException,
            InterruptedException {
        // concurrency to avoid stdio deadlocks
        Redir stdout = null;
        String out = null;
        if (!outLogged) {
            stdout = new Redir(process.getInputStream());
            new Thread(stdout, tName("STDOUT-" + process.toString())).start();
        }
        Redir stderr = new Redir(process.getErrorStream());
        new Thread(stderr, tName("STDERR-" + process.toString())).start();
        // redirect input in the current thread
        if (stdin != null) {
            OutputStream pout = process.getOutputStream();
            new RedirectingInputStream(stdin, true, true).redirectAll(pout);
        }
        process.waitFor();
        int exitValue = process.exitValue();
        Thread.sleep(50);
        if (stdout != null) {
            stdout.throwIfHadException();
            out = new String(stdout.getResult());
        }
        stderr.throwIfHadException();
        String err = new String(stderr.getResult());

        return new CmdResult(exitValue, out, err);
    }

    /**
     * User-specified IO exception handler for exceptions during
     * I/O redirection.
     */
    public static interface BrokenPipeHandler {
        /**
         * Invoked when pipe is broken, that is, when I/O error occurs while
         * reading from the source or writing to the sink
         * @param e the associated I/O exception
         * @param src the source of the pipe
         * @param sink the sink of the pipe
         */
        void brokenPipe(IOException e, InputStream src, OutputStream sink);
    }

    /**
     * User-specified handler invoked when associated native process exits.
     */
    public static interface ProcessExitHandler {
        /**
         * Invoked when associated process has exited.
         * @param process the process that exited.
         */
        void processExited(Process process);
    }

    /**
     * Represents the result of a native command. Consists of the process
     * exit value together with stdout and stderr dumped to strings.
     *
     * @author Dawid Kurzyniec
     * @version 1.0
     */
    public static class CmdResult {
        final int exitValue;
        final String out;
        final String err;
        CmdResult(int exitValue, String out, String err) {
            this.exitValue = exitValue;
            this.out = out;
            this.err = err;
        }
        public int getExitValue() { return exitValue; }
        public String getOut() { return out; }
        public String getErr() { return err; }
    }

    public static void handleProcess(Process process,
                                     InputStream stdin,
                                     OutputStream stdout,
                                     OutputStream stderr)
        throws IOException
    {
        handleProcess(process, stdin, stdout, stderr, true, false, null, null);
    }

    public static void handleProcess(Process process,
                                     InputStream stdin,
                                     OutputStream stdout,
                                     OutputStream stderr,
                                     boolean autoFlush, boolean autoClose,
                                     BrokenPipeHandler brokenPipeHandler,
                                     ProcessExitHandler exitHandler)
        throws IOException
    {
        handleProcess(process,
                      stdin, autoFlush, autoClose, brokenPipeHandler,
                      stdout, autoFlush, autoClose, brokenPipeHandler,
                      stderr, autoFlush, autoClose, brokenPipeHandler,
                      exitHandler);
    }


    public static void handleProcess(Process process,
                                     InputStream stdin,
                                     boolean inAutoFlush, boolean inAutoClose,
                                     BrokenPipeHandler inBrokenHandler,
                                     OutputStream stdout,
                                     boolean outAutoFlush, boolean outAutoClose,
                                     BrokenPipeHandler outBrokenHandler,
                                     OutputStream stderr,
                                     boolean errAutoFlush, boolean errAutoClose,
                                     BrokenPipeHandler errBrokenHandler,
                                     ProcessExitHandler exitHandler)
        throws IOException
    {
        ProcessHandler ph = new ProcessHandler(process,
                                               stdin, inAutoFlush, inAutoClose,
                                               inBrokenHandler,
                                               stdout, outAutoFlush, outAutoClose,
                                               outBrokenHandler,
                                               stderr, errAutoFlush, errAutoClose,
                                               errBrokenHandler,
                                               exitHandler);
        ph.start();
    }

    private static class Pipe implements Runnable {
        final InputStream src;
        final OutputStream sink;
        final boolean autoFlush;
        final boolean autoClose;
        final BrokenPipeHandler brokenPipeHandler;
        public Pipe(InputStream src, OutputStream sink,
                    BrokenPipeHandler brokenPipeHandler) {
            this(src, sink, brokenPipeHandler, true, false);
        }
        public Pipe(InputStream src, OutputStream sink,
                    BrokenPipeHandler brokenPipeHandler,
                    boolean autoFlush, boolean autoClose) {
            this.src = src;
            this.sink = sink;
            this.brokenPipeHandler = brokenPipeHandler;
            this.autoFlush = autoFlush;
            this.autoClose = autoClose;
        }
        public void run() {
            RedirectingInputStream sd =
                new RedirectingInputStream(src, autoFlush, autoClose);
            try {
                sd.redirectAll(sink);
            }
            catch (IOException e) {
                if (brokenPipeHandler != null) {
                    brokenPipeHandler.brokenPipe(e, src, sink);
                }
            }
        }
    }


    public static Thread executeCommandWithWorkerNoBlocking(
            String[] command, final boolean printOutput,
            final boolean printError, final long timeOut, File dir,
            File logFile, boolean doSynchronizedLaunch) {

        Runtime runtime;
        WorkerNoBlock worker;
        Process process;
        StreamGobbler outputGobbler, errorGobbler;

        // windows platform independent
        //
        if (GenericUtil.isWindows()) {
            String[] ncmdarray = new String[command.length + 2];
            ncmdarray[0] = "cmd";
            ncmdarray[1] = "/C";
            int ctr = 2;
            for (int i = 0; i < command.length; i++) {
                ncmdarray[ctr] = command[i];
                ctr++;
            }
            command = ncmdarray;
        }

        // mkdirs
        //
        if (!dir.exists()) dir.mkdirs();

        try {
            if (doSynchronizedLaunch) {
                synchronized (GenericUtil.class) {
                    runtime = Runtime.getRuntime();
                    process = runtime.exec(command, null, dir);
                    outputGobbler = new StreamGobbler(process.getInputStream(),
                            "STD OUT", printOutput, logFile, dir);
                    errorGobbler = new StreamGobbler(process.getErrorStream(),
                            "STD ERR", printError, logFile, dir);
                    outputGobbler.start();
                    errorGobbler.start();
                    worker = new WorkerNoBlock(process);
                    worker.start();
                }
            } else {
                runtime = Runtime.getRuntime();
                process = runtime.exec(command, null, dir);
                outputGobbler = new StreamGobbler(process.getInputStream(),
                        "STD OUT", printOutput, logFile, dir);
                errorGobbler = new StreamGobbler(process.getErrorStream(),
                        "STD ERR", printError, logFile, dir);
                outputGobbler.start();
                errorGobbler.start();
                worker = new WorkerNoBlock(process);
                worker.start();
            }

        } catch (FileNotFoundException ex) {
            String errorMessage = "the log file was not found.";
            throw new RuntimeException(errorMessage, ex);

        } catch (IOException ex) {
            String errorMessage = "the command: " + command
                    + ", did not complete due to an " + "io error.";
            throw new RuntimeException(errorMessage, ex);
        }
        return (Thread) worker;
    }

    /**
     * This is a class used for running shell scripts without blocking I/O
     *
     * @author S. A. Burton April 2011
     *
     */
    private static class WorkerNoBlock extends Thread {
        private Integer exitValue;
        private final Process process;

        /**
         * Constructor
         *
         * @param process
         */
        protected WorkerNoBlock(final Process process) {
            this.process = process;
        }

        /**
         * This method gets the exit value of the shell script
         *
         * @return
         */
        public Integer getExitValue() {
            return exitValue = process.exitValue();
        }

        /**
         * Implementation is commented out
         */
        public void run() {
            // try {
            // //exitValue = process.waitFor();
            // } catch (InterruptedException e) {
            // return;
            // }
        }
    }

    private static class StreamGobbler extends Thread {

        private boolean displayStreamOutput;
        private InputStream is;
        private PrintWriter logPw;
        private String type;
        public boolean keepGoing = true;
        private File dir = null;

        public StreamGobbler(InputStream is, String type,
                             boolean displayStreamOutput, File logFile, File dir)
                throws FileNotFoundException {
            this.is = is;
            this.type = type;
            this.displayStreamOutput = displayStreamOutput;
            logPw = new PrintWriter(new FileOutputStream(logFile));
            this.dir = dir;
        }

        public void closeDown() {
            GenericUtil.appendFileContents("StreamGobbler.closingDown(): setting flag to stop running "
                    + " keepGoing = false now; stream gobbler type = " + type, dir);
            keepGoing = false;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null && keepGoing) {

                    GenericUtil.appendFileContents("StreamGobbler.run(): while loop; "
                            + " stream gobbler type = " + type + "; line = " + line, dir);

                    if (displayStreamOutput) System.out.println(type + ">" + line);

                    GenericUtil.appendFileContents("StreamGobbler.run(): while loop; "
                            + " stream gobbler type = " + type + "; here0", dir);

                    logPw.println(line);


                    GenericUtil.appendFileContents("StreamGobbler.run(): while loop; "
                            + " stream gobbler type = " + type + "; here1", dir);

                    logPw.flush();


                    GenericUtil.appendFileContents("StreamGobbler.run(): while loop; "
                            + " stream gobbler type = " + type + "; here2", dir);

                    while (!br.ready() && keepGoing) {
                        //System.out.println("gobbler type = " + type + " is not ready.");
                        GenericUtil.appendFileContents("StreamGobbler.run(): inner while loop; "
                                + "br not ready; stream gobbler type = " + type + "keepGoing = " + keepGoing, dir);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!keepGoing) break;
                }
                GenericUtil.appendFileContents("StreamGobbler.run(): exited run inner loop for type = "
                        + type + "; keepGoing = " + keepGoing, dir);
                logPw.flush();
                logPw.close();
                br.close();
                isr.close();
                is.close();
            } catch (IOException ioe) {
                System.out.println("***exception in gobbler type = " + type + ": " + ioe);
                GenericUtil.appendFileContents("StreamGobbler.run(): exception = "
                        + ioe, dir);
                ioe.printStackTrace();
            } finally {
                logPw.flush();
                logPw.close();
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    private static class Redir implements Runnable {
        final Pipe pipe;
        IOException ex;
        Redir(InputStream is) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BrokenPipeHandler bph = new BrokenPipeHandler() {
                public void brokenPipe(IOException ex, InputStream src,
                                       OutputStream sink) {
                    setException(ex);
                }
            };
            this.pipe = new Pipe(is, bos, bph, true, true);
        }
        public void run() {
            pipe.run();
        }
        synchronized void setException(IOException e) {
            this.ex = e;
        }
        synchronized void throwIfHadException() throws IOException {
            if (ex != null) throw ex;
        }
        public byte[] getResult() {
            return ((ByteArrayOutputStream)pipe.sink).toByteArray();
        }
    }

    private static class ProcessHandler {
        final Process process;
        final Thread tstdin;
        final Thread tstdout;
        final Thread tstderr;
        final Thread texitHandler;
        ProcessHandler(final Process process,
                       InputStream stdin, boolean inAutoFlush, boolean inAutoClose,
                       BrokenPipeHandler inBrokenHandler,
                       OutputStream stdout, boolean outAutoFlush, boolean outAutoClose,
                       BrokenPipeHandler outBrokenHandler,
                       OutputStream stderr, boolean errAutoFlush, boolean errAutoClose,
                       BrokenPipeHandler errBrokenHandler,
                       final ProcessExitHandler exitHandler)
            throws IOException
        {
            this.process = process;
            this.tstdin = createPipe(stdin, process.getOutputStream(),
                                     inBrokenHandler, inAutoFlush, inAutoClose);
            this.tstdout = createPipe(process.getInputStream(), stdout,
                                      outBrokenHandler, outAutoFlush, outAutoClose);
            this.tstderr = createPipe(process.getErrorStream(), stderr,
                                      errBrokenHandler, errAutoFlush, errAutoClose);
            if (exitHandler != null) {
                this.texitHandler = new Thread(new ExitHandler(process, exitHandler), "Process-" + process + "-Exit");
            }
            else {
                texitHandler = null;
            }
        }

        void start() {
            if (tstdin != null) tstdin.start();
            if (tstdout != null) tstdout.start();
            if (tstderr != null) tstderr.start();
            if (texitHandler != null) texitHandler.start();
        }

        private static class ExitHandler implements Runnable {
            final Process process;
            final ProcessExitHandler exitHandler;
            ExitHandler(Process process, ProcessExitHandler exitHandler) {
                this.process = process;
                this.exitHandler = exitHandler;
            }
            public void run() {
                try {
                    process.waitFor();
                }
                catch (InterruptedException e) {
                    // silently ignore and destroy all in finally
                }
                finally {
                    // just in case, or if interrupted
                    process.destroy();
                    exitHandler.processExited(process);
                }
            }
        }

        private static Thread createPipe(InputStream src, OutputStream sink,
                                         BrokenPipeHandler bph,
                                         boolean autoFlush, boolean autoClose)
            throws IOException
        {
            if (src == null) {
                if (sink != null && autoClose) sink.close();
                return null;
            }
            else if (sink == null) {
                if (autoClose) src.close();
                return null;
            }
            else {
                return new Thread(new Pipe(src, sink, bph, autoFlush, autoClose));
            }
        }
    }

    /**
     * Added by E. D. Thompson AFRL/RZTT 20100827 Execute specified command and
     * it arguments and return its results. Waits for the command to complete
     * and returns its completion status and data written to standard output and
     * error streams. The process' standard input is set to EOF. Example:
     * <p/>
     * <pre>
     * String[] cmd = { &quot;/bin/ls&quot;, &quot;-lah&quot; };
     * System.out.println(ExecUtils.execCommand(cmd).getOut());
     * </pre>
     *
     * @param cmdarray the command and arguments to execute
     * @return the results of the command execution
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before command completes
     */
    public static CmdResult execCommand(String[] cmdarray) throws IOException,
            InterruptedException {

        if (GenericUtil.isWindows()) {
            String[] ncmdarray = new String[cmdarray.length + 2];
            ncmdarray[0] = "cmd";
            ncmdarray[1] = "/C";
            ncmdarray[2] = "\"" + cmdarray[0] + "\"";
            System.arraycopy(cmdarray, 1, ncmdarray, 3, cmdarray.length-1);
            cmdarray = ncmdarray;
        }
        return execCommand(Runtime.getRuntime().exec(cmdarray));
    }

    /**
     * Added by E. D. Thompson AFRL/RZTT 20100827 Attach to the specified
     * process and return its results. Returns data written to standard output
     * and error streams. The process standard input is set to EOF. Example:
     * <p/>
     * <pre>
     * Process p = runtime.exec(&quot;/bin/ls&quot;);
     * System.out.println(ExecUtils.execCommand(p).getOut());
     * </pre>
     *
     * @param process the process to attach to
     * @return the results of the process
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before process ends
     */
    public static CmdResult execCommandNoBlocking(Process process)
            throws IOException, InterruptedException {
        return execCommandNoBlocking(process, new NullInputStream());
    }

    /**
     * Added by E. D. Thompson AFRL/RZTT 20100827 Attach to the specified
     * process, feed specified standard input, and return process' results.
     * returns data written to standard output and error streams.
     *
     * @param process the process to attach to
     * @param stdin   the data to redirect to process' standard input
     * @return the results of the process
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if thread is interrupted before process ends
     * @see #execCommand(Process)
     */
    public static CmdResult execCommandNoBlocking(final Process process,
                                                  final InputStream stdin) throws IOException, InterruptedException {
        // concurrency to avoid stdio deadlocks
        Redir stdout = new Redir(process.getInputStream());
        Redir stderr = new Redir(process.getErrorStream());
        new Thread(stdout, tName("STDOUT - " + process.toString())).start();
        new Thread(stderr, tName("STDERR - " + process.toString())).start();
        // redirect input in the current thread
        if (stdin != null) {
            OutputStream pout = process.getOutputStream();
            new RedirectingInputStream(stdin, true, true).redirectAll(pout);
        }

        stdout.throwIfHadException();
        stderr.throwIfHadException();
        String out = new String(stdout.getResult());
        String err = new String(stderr.getResult());

        return new CmdResult(-1, out, err);
    }
}
