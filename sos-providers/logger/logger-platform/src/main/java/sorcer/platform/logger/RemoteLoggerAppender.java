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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.RemoteLogger;
import sorcer.core.SorcerEnv;

import java.net.UnknownHostException;
import java.util.Queue;

/**
 * Publish log to remote logger service through a queue.
 *
 * @author Rafał Krupiński
 */
public class RemoteLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    final private static Logger log = LoggerFactory.getLogger(RemoteLoggerAppender.class);
    private Queue<ILoggingEvent> queue;
    private static String hostname;

    static {
        try {
            hostname = SorcerEnv.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Couldn't determine own hostname");
            hostname = "remote";
        }
    }

    public RemoteLoggerAppender(Queue<ILoggingEvent> queue) {
        assert queue != null;
        this.queue = queue;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        eventObject.getMDCPropertyMap().put(RemoteLogger.KEY_HOSTNAME, hostname);
        queue.add(eventObject);
    }
}
