/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.platform.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.sun.jini.start.LifeCycle;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install RemoteLoggerAppender in Logback
 *
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
        remoteAppender.setContext(loggerContext);
        remoteAppender.addFilter(MDCFilter.instance);
        remoteAppender.start();
        root.addAppender(remoteAppender);
    }
}
