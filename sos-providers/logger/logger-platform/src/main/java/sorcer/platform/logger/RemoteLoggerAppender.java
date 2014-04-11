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
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import sorcer.client.ClientProxyFactory;
import sorcer.core.RemoteLogger;

import java.rmi.RemoteException;

/**
 * publish log to remote logger service.
 *
 * @author Rafał Krupiński
 */
public class RemoteLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private ClientProxyFactory<RemoteLogger> remoteLogger = new ClientProxyFactory<RemoteLogger>(RemoteLogger.class);

    @Override
    protected void append(ILoggingEvent eventObject) {
        LoggingEventVO vo = LoggingEventVO.build(eventObject);

        try {
            RemoteLogger service = remoteLogger.get();

            if (service == null) {
                addWarn("No RemoteLogger service found");
                return;
            }

            service.publish(vo);
        } catch (RuntimeException e) {
            addError("Error while calling remote logger", e);
        } catch (RemoteException e) {
            addError("Error while calling remote logger", e);
        }
    }
}
