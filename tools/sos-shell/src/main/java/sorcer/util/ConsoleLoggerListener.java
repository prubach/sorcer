package sorcer.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import org.slf4j.LoggerFactory;
import sorcer.core.RemoteLogger;
import sorcer.core.provider.logger.LoggerRemoteEvent;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * SORCER class
 * User: prubach
 * Date: 09.06.14
 */
public class ConsoleLoggerListener implements RemoteEventListener, Serializable {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConsoleLoggerListener.class);

    @Override
    public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
        LoggerRemoteEvent logEvent = (LoggerRemoteEvent)event;
        ILoggingEvent le = logEvent.getLoggingEvent();
        // Print everything to console as if it was a local log
        String exId = le.getMDCPropertyMap().get(RemoteLogger.KEY_EXERTION_ID);
        // TODO: print nicely with a marker showing that it's a remote log
        logger.info(exId);
        ((ch.qos.logback.classic.Logger)logger).callAppenders(logEvent.getLoggingEvent());
    }
}
