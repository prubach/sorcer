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

package sorcer.core.provider.outrigger;

import net.jini.config.ConfigurationException;
import sorcer.config.AbstractBeanListener;
import sorcer.config.Configurer;
import sorcer.core.provider.ProviderDelegate;
import sorcer.core.provider.ServiceProvider;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerProcessor extends AbstractBeanListener {
    @Inject
    private Configurer configurer;

    @Inject
    private ExecutorService executor;

    @Override
    public void destroy(ServiceProvider provider) {
        Object o = provider.configurations.get(SpaceTakerConfiguration.class.getName());
        if(o instanceof SpaceTakerConfiguration)
            ((SpaceTakerConfiguration) o).destroy();
    }

    @Override
    public void activate(ServiceProvider provider) throws ConfigurationException {
        ProviderDelegate delegate = provider.getDelegate();
        if (!delegate.spaceEnabled())
            return;

        SpaceTakerConfiguration config = new SpaceTakerConfiguration(provider);
        configurer.process(config, provider.getProviderConfiguration());
        executor.submit(config);
    }
}
