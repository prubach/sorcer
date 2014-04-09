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
import com.google.inject.multibindings.Multibinder;
import net.jini.core.discovery.LookupLocator;
import net.jini.lease.LeaseRenewalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.config.BeanListener;
import sorcer.container.core.BeanListenerModule;
import sorcer.container.discovery.ILookupManagerRegistry;
import sorcer.container.discovery.LookupManagerRegistry;
import sorcer.container.discovery.ServiceManagerRegistry;
import sorcer.container.discovery.IDiscoveryManagerRegistry;
import sorcer.core.SorcerEnv;
import sorcer.core.service.Configurer;
import sorcer.core.service.IServiceBeanListener;
import sorcer.core.service.ServiceBeanDestroyer;
import sorcer.core.service.ServiceBeanListener;

import javax.inject.Provider;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class PlatformModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(PlatformModule.class);

    @Override
    protected void configure() {
        Multibinder<BeanListener> listenerBinder = Multibinder.newSetBinder(binder(), BeanListener.class);
        listenerBinder.addBinding().to(ServiceBeanDestroyer.class);
        listenerBinder.addBinding().to(Configurer.class);

        bind(IServiceBeanListener.class).to(ServiceBeanListener.class).in(Scopes.SINGLETON);
        bind(DiscoveryManagerRegistry.class).in(Scopes.SINGLETON);
        bind(LeaseRenewalManager.class).in(Scopes.SINGLETON);
        bind(BeanListenerModule.class).in(Scopes.SINGLETON);

        final LookupLocator[] lookupLocators = getLookupLocators();
        bind(ILookupManagerRegistry.class).toProvider(new Provider<ILookupManagerRegistry>() {
            @Override
            public ILookupManagerRegistry get() {
                return new LookupManagerRegistry(lookupLocators, SorcerEnv.getLookupGroups());
            }
        }).in(Scopes.SINGLETON);
        bind(IDiscoveryManagerRegistry.class).toProvider(new Provider<IDiscoveryManagerRegistry>() {
            @Override
            public IDiscoveryManagerRegistry get() {
                return new ServiceManagerRegistry(lookupLocators, SorcerEnv.getLookupGroups());
            }
        }).in(Scopes.SINGLETON);
    }

    private LookupLocator[] getLookupLocators() {
        String[] locURLs = SorcerEnv.getLookupLocators();
        if (locURLs == null || locURLs.length == 0) {
            return null;
        }
        List<LookupLocator> locators = new ArrayList<LookupLocator>(locURLs.length);
        log.debug("ProviderAccessor Locators: {}", locURLs);

        for (String locURL : locURLs)
            try {
                locators.add(new LookupLocator(locURL));
            } catch (MalformedURLException e) {
                log.warn("Invalid Lookup URL: {}", locURL, e);
            }

        if (locators.isEmpty())
            return null;
        return locators.toArray(new LookupLocator[locators.size()]);
    }
}
