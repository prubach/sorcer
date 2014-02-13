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

package sorcer.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class ServiceStopper extends Thread {
    private ServiceStarter serviceStarter;
    private static final Logger log = LoggerFactory.getLogger(ServiceStopper.class);

    public ServiceStopper(String name, ServiceStarter serviceStarter) {
        super(name);
        this.serviceStarter = serviceStarter;
    }

    static void install(ServiceStarter serviceStarter) {
        Runtime.getRuntime().addShutdownHook(new ServiceStopper("SORCER service destroyer", serviceStarter));
    }

    @Override
    public void run() {
        serviceStarter.stop();
    }
}
