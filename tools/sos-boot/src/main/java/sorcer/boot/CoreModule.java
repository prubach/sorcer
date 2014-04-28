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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.sun.jini.start.AggregatePolicyProvider;
import org.rioproject.resolver.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.util.JarClassPathHelper;
import sorcer.boot.util.ReferenceHolder;
import sorcer.core.service.Configurer;
import sorcer.protocol.ProtocolHandlerRegistry;
import sorcer.util.ConfigurableThreadFactory;

import javax.inject.Named;
import javax.inject.Provider;
import java.security.Policy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author Rafał Krupiński
 */
class CoreModule extends AbstractModule {
    static final Logger log = LoggerFactory.getLogger(CoreModule.class);

    @Override
    protected void configure() {
        /*
         This is a hack against Guice injector hierarchy. We want Resolver, which is loaded by the platform
         in it's own class loader, be accessible by the whole system. For this we ensure that resolver platform
         service is loaded first, and keep artificial reference here in the root injector.
         ReferenceHolder object is used by the resolver Activator to inject the value, which is then accessible
         to other clients thanks to singleton bind of Resolver class.
        */
        TypeLiteral<ReferenceHolder<Resolver>> resolverType = new TypeLiteral<ReferenceHolder<Resolver>>() {
        };
        ReferenceHolder<Resolver> resolverHolder = new ReferenceHolder<Resolver>();
        bind(Resolver.class).toProvider(resolverHolder).in(Scopes.SINGLETON);
        bind(resolverType).toInstance(resolverHolder);

        bind(ProtocolHandlerRegistry.class).toInstance(ProtocolHandlerRegistry.get());
        bind(Policy.class).annotatedWith(Names.named("initialGlobalPolicy")).toInstance(Policy.getPolicy());
        bind(AggregatePolicyProvider.class).annotatedWith(Names.named("globalPolicy")).toProvider(new Provider<AggregatePolicyProvider>() {
            @Inject
            @Named("initialGlobalPolicy")
            Policy initialGlobalPolicy;

            @Override
            public AggregatePolicyProvider get() {
                AggregatePolicyProvider globalPolicy = new AggregatePolicyProvider(initialGlobalPolicy);
                Policy.setPolicy(globalPolicy);
                log.debug("Global policy set: {}", globalPolicy);
                return globalPolicy;
            }
        }).in(Scopes.SINGLETON);
        bind(JarClassPathHelper.class).in(Scopes.SINGLETON);

        final ConfigurableThreadFactory tf = new ConfigurableThreadFactory();
        tf.setDaemon(true);
        tf.setNameFormat("SORCER-%2$d");
        bind(ThreadFactory.class).toInstance(tf);

        Provider<ScheduledExecutorService> executorServiceProvider = new Provider<ScheduledExecutorService>() {
            @Override
            public ScheduledExecutorService get() {
                return Executors.newScheduledThreadPool(1, tf);
            }
        };
        bind(ScheduledExecutorService.class).toProvider(executorServiceProvider).in(Scopes.SINGLETON);
        bind(Configurer.class).in(Scopes.SINGLETON);
    }
}
