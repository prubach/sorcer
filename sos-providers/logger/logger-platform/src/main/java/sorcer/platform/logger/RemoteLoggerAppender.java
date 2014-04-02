package sorcer.platform.logger;

import ch.qos.logback.classic.jul.JULHelper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sorcer.core.RemoteLogger;
import sorcer.service.Accessor;

import java.rmi.RemoteException;
import java.util.logging.LogRecord;

/**
 * @author Rafał Krupiński
 */
public class RemoteLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private static final Logger log = LoggerFactory.getLogger(RemoteLoggerAppender.class);

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (MDC.get("SORCER-REMOTE-CALL") == null)
            return;

        try {
            RemoteLogger service = Accessor.getService(RemoteLogger.class);
            if (service == null)
                return;

            LogRecord record = new LogRecord(JULHelper.asJULLevel(eventObject.getLevel()), eventObject.getMessage());
            record.setLoggerName(JULHelper.asJULLoggerName(eventObject.getLoggerName()));
            record.setMillis(eventObject.getTimeStamp());
            record.setParameters(eventObject.getArgumentArray());
            StackTraceElement ste = eventObject.getCallerData()[0];
            record.setSourceClassName(ste.getClassName());
            record.setSourceMethodName(ste.getMethodName());
            //TODO support Throwable

            service.publish(record);
        } catch (RemoteException e) {
            log.warn("Error", e);
        }
    }
}
