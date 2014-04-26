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

package sorcer.container.discovery;


import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
class LoggingDiscoveryListener implements DiscoveryListener, ServiceDiscoveryListener {
    private static final Logger log = LoggerFactory.getLogger(LoggingDiscoveryListener.class);

    public static final LoggingDiscoveryListener LOGGING_LISTSER = new LoggingDiscoveryListener();

    @Override
    public void discovered(DiscoveryEvent e) {
        log.info("{}", e);
    }

    @Override
    public void discarded(DiscoveryEvent e) {
        log.info("{}", e);
    }

    @Override
    public void serviceAdded(ServiceDiscoveryEvent event) {
        log.info("{}", event);
    }

    @Override
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        log.info("{}", event);
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        log.info("{}", event);
    }
}
