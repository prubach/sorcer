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

package sorcer.container.core;

import com.google.inject.MembersInjector;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import sorcer.core.service.Configurer;

/**
 * Call {@link sorcer.core.service.Configurer#process(Object, net.jini.config.Configuration)} on processed bean.
 *
 * This class is called by Guice Injector during members injection.
 *
 * @author Rafał Krupiński
 */
public class ConfigurationInjector<I> implements MembersInjector<I> {
    private Configurer configurer;
    private Configuration config;

    public ConfigurationInjector(Configurer configurer, Configuration config) {
        this.configurer = configurer;
        this.config = config;
    }

    @Override
    public void injectMembers(I instance) {
        try {
            configurer.process(instance, config);
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException("Error while configuring " + instance, e);
        }
    }
}
