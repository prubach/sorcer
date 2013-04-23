package sorcer.core.context;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

public class ThrowableTrace implements Serializable {
    private static final long serialVersionUID = 1L;
    public String message;
    public Throwable throwable;
    public String stackTrace;

    public ThrowableTrace(Throwable t) {
        throwable = t;
        stackTrace = getStackTrace(t);
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }


    public ThrowableTrace(String message, Throwable t) {
        this(t);
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String toString() {
        String info = message != null ? message : throwable.getMessage();
        if (throwable != null)
            return throwable.getClass().getName() + ": " + info;
        else
            return info;
    }

    public String describe() {
        return stackTrace;
    }
}
