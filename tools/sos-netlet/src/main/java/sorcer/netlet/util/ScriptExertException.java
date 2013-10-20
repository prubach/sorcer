package sorcer.netlet.util;

import org.apache.commons.io.output.ThresholdingOutputStream;

/**
 * SORCER class
 * User: prubach
 * Date: 02.07.13
 */
public class ScriptExertException extends Exception {

    int lineNum;
    Throwable cause;

    public ScriptExertException(String msg) {
        super(msg);
    }


    public ScriptExertException(String msg, Throwable cause, int lineNum) {
        super(msg);
        this.lineNum = lineNum;
        this.cause = cause;
    }

    public int getLineNum() {
        return lineNum;
    }

    public Throwable getCause() {
        return cause;
    }
}
