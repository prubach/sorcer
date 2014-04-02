package sorcer.platform.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.sun.jini.start.LifeCycle;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class RemoteLoggerClient {

    public RemoteLoggerClient(String[] args, LifeCycle lifeCycle) {
        init();
    }

    private void init() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext))
            throw new IllegalStateException("This service must be run with Logback Classic");

        LoggerContext loggerContext = (LoggerContext) loggerFactory;
        ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String appenderName = getClass().getName();
        Appender<ILoggingEvent> appender = root.getAppender(appenderName);
        if (appender != null)
            throw new IllegalStateException("Appender " + appenderName + " already configured");

        RemoteLoggerAppender remoteAppender = new RemoteLoggerAppender();
        remoteAppender.start();
        root.addAppender(remoteAppender);
    }
}
