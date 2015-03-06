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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sorcer.core.RemoteLogger;
import sorcer.core.SorcerConstants;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.junit.SorcerServiceConfiguration;

/**
 * @author Rafał Krupiński
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase("org.sorcersoft.sorcer:logger-api")

@SorcerServiceConfiguration("org.sorcersoft.sorcer:logger-cfg:" + SorcerConstants.SORCER_VERSION)
public class RemoteLoggerAppenderTest {
    @Test
    public void testLogger() throws Exception {
        MDC.put(RemoteLogger.LOGGER_CONTEXT_KEY, "anything");
        LoggerFactory.getLogger(getClass()).info("test message");
    }
}
