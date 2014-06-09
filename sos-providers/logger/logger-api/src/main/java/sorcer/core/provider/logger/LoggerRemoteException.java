package sorcer.core.provider.logger;

/**
 * SORCER class
 * User: prubach
 * Date: 09.06.14
 */
public class LoggerRemoteException extends Exception {

    public LoggerRemoteException() {
    }

    public LoggerRemoteException(String message) {
        super(message);
    }

    public LoggerRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggerRemoteException(Throwable cause) {
        super(cause);
    }

    public LoggerRemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
