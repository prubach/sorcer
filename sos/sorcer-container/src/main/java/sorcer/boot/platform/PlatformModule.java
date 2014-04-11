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

package sorcer.boot.platform;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import sorcer.container.sdi.DiscoveryManagerRegistry;
import sorcer.container.sdi.IDiscoveryManagerRegistry;
import sorcer.core.service.IServiceBeanListener;
import sorcer.core.service.ServiceBeanListener;

/**
 * @author Rafał Krupiński
 */
public class PlatformModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IServiceBeanListener.class).to(ServiceBeanListener.class).in(Scopes.SINGLETON);
        bind(IDiscoveryManagerRegistry.class).to(DiscoveryManagerRegistry.class).in(Scopes.SINGLETON);
    }
}
