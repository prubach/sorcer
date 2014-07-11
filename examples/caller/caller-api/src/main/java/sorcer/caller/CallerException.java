package sorcer.caller;

/**
 * SORCER class
 * User: prubach
 * Date: 11.07.14
 */
public class CallerException extends Exception {

    private String command = null;

    private String output = null;

    private int exitValue = 0;

    public CallerException(int exitValue, String output, String command) {
        super("Got exit value: " + exitValue + " running: " + command + "\n" + output);
        this.command = command;
        this.output = output;
        this.exitValue = exitValue;
    }
}
