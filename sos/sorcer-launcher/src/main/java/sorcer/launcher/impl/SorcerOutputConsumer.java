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

package sorcer.launcher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.launcher.SorcerListener;
import sorcer.util.io.LineWriter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rafał Krupiński
 */
public class SorcerOutputConsumer extends LineWriter {
    private static final Logger log = LoggerFactory.getLogger(SorcerOutputConsumer.class);
    private Pattern pattern = Pattern.compile("Started (\\d+)/(\\d+) services; (\\d+) errors");
    private SorcerListener sorcerListener;

    public SorcerOutputConsumer(SorcerListener sorcerListener) {
        this.sorcerListener = sorcerListener;
    }

    @Override
    public void writeLine(String line) throws IOException {
        Matcher m = pattern.matcher(line);
        if (!m.find()) {
            return;
        }
        int started = Integer.parseInt(m.group(1));
        int all = Integer.parseInt(m.group(2));
        int erred = Integer.parseInt(m.group(3));

        log.debug("Started {} of {} with {} erred", started, all, erred);

        if (all == started + erred) {
            if (erred != 0)
                log.warn("Errors while starting services");
            sorcerListener.sorcerStarted();
        }
    }
}
