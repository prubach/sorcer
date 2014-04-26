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

package sorcer.core.provider.container;

import sorcer.config.AbstractBeanListener;
import sorcer.core.provider.Provider;
import sorcer.core.service.IProviderServiceBuilder;
import sorcer.core.service.IServiceBuilder;

import java.lang.reflect.Method;

/**
 * Extracted from ProviderDelegate#initBean
 * @author Rafał Krupiński
 */
public class LegacyInitializer extends AbstractBeanListener {
    @Override
    public void preProcess(IServiceBuilder serviceBuilder, Object serviceBean) {
        if (!(serviceBuilder instanceof IProviderServiceBuilder))
            return;

        try {
            Method m = serviceBean.getClass().getMethod("init", new Class[]{Provider.class});
            log.info("Initializing service bean {}", serviceBean.getClass().getName());
            m.invoke(serviceBean, ((IProviderServiceBuilder) serviceBuilder).getProvider());
        } catch (Exception e) {
            log.debug("No 'init' method for this service bean: {}", serviceBean.getClass().getName());
        }
    }
}
