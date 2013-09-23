package sorcer.config;
/**
 *
 * Copyright 2013 Rafał Krupiński.
 * Copyright 2013 Sorcersoft.com S.A.
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


import net.jini.config.ConfigurationException;
import sorcer.core.SorcerEnv;
import sorcer.core.provider.ServiceProvider;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Rafał Krupiński
 */
public class LoggerConfigurer implements ServiceActivator{
    @Override
    public void activate(Object[] serviceBeans, ServiceProvider provider) throws ConfigurationException {
        try {
            Handler h = new FileHandler(SorcerEnv.getHomeDir()
                    + "/logs/remote/local-Cataloger-" + provider.getDelegate().getHostName()
                    + "-" + provider.getProviderName() + ".log", 2000000, 8, true);
            h.setFormatter(new SimpleFormatter());

            for (Object serviceBean : serviceBeans) {


            Logger logger = Logger.getLogger(serviceBean.getClass().getName());
            logger.addHandler(h);
            logger.setUseParentHandlers(false);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
