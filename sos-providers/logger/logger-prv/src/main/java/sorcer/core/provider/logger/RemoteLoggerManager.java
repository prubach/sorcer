/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
package sorcer.core.provider.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.apache.commons.io.FileUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import sorcer.core.RemoteLogger;
import sorcer.core.SorcerEnv;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RemoteLoggerManager implements RemoteLogger {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RemoteLoggerManager.class);

    // The list of all known loggers.
    private List<LoggingConfig> knownLoggers = new LinkedList<LoggingConfig>();

    private LoggerContext loggerFactory;

    private File logDir = new File(SorcerEnv.getHomeDir(), "logs/remote");

    public RemoteLoggerManager() {
        ILoggerFactory loggerFactory;
        loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext))
            throw new IllegalStateException("This service must be running with Logback Classic");
        this.loggerFactory = (LoggerContext) loggerFactory;
    }

    public String[] getLogNames() throws RemoteException {
        if (this.logDir == null)
            return new String[0];
        List<String> list = new LinkedList<String>();

        String[] loggerNames;
        loggerNames = logDir.list();
        for (String loggerName : loggerNames)
            if (loggerName.endsWith(".log"))
                list.add(loggerName);
        return list.toArray(new String[list.size()]);
    }

    public void publish(List<LoggingEventVO> loggingEvents) {
        for (LoggingEventVO vo : loggingEvents)
            publish(vo);
    }

    protected void publish(ILoggingEvent loggingEvent) {
        String loggerName;
        Logger logger;
        synchronized (this) {
            loggerName = "remote." + loggingEvent.getLoggerName().intern();
            logger = loggerFactory.getLogger(loggerName);
            Appender<ILoggingEvent> appender = logger.getAppender(loggerName);
            if (appender == null) {
                logger.setAdditive(false);
                logger.addAppender(createAppender(loggerName));
            }
        }

        logger.callAppenders(loggingEvent);
        LoggingConfig lc = new LoggingConfig(loggerName, null);
        if (!knownLoggers.contains(lc)) {
            lc.setLevel(java.util.logging.Level.ALL);
            knownLoggers.add(lc);
        }
    }

    private Appender<ILoggingEvent> createAppender(String loggerName) {
        Appender<ILoggingEvent> appender;
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setName(loggerName);
        File file = new File(logDir, "remote-logger-" + loggerName + ".log");
        fileAppender.setFile(file.getPath());
        fileAppender.setContext(loggerFactory);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerFactory);
        encoder.setPattern("%-5level %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx");
        fileAppender.setEncoder(encoder);
        appender = fileAppender;
        encoder.start();
        appender.start();
        return appender;
    }

    public List<String> getLog(String fileName) throws RemoteException {
        try {
            return FileUtils.readLines(new File(logDir, fileName));
        } catch (IOException e) {
            String msg = MessageFormatter.format("Error reading file {}", fileName).getMessage();
            log.warn(msg, e);
            return Arrays.asList(msg);
        }
    }

    public List<LoggingConfig> getLoggers() throws IOException {
        return knownLoggers;
    }

    public void deleteLog(String loggerName) throws RemoteException {
    }
}
