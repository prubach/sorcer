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

import com.google.inject.Injector;
import net.jini.config.Configuration;
import org.sorcersoft.sorcer.SpaceTaker;
import sorcer.config.AbstractBeanListener;
import sorcer.container.core.ConfiguringModule;
import sorcer.core.service.Configurer;
import sorcer.core.service.IServiceBuilder;

import javax.inject.Inject;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerProcessor extends AbstractBeanListener {
    @Inject
    private ExecutorService executor;

    @Inject
    private SpaceTaker spaceTaker;

    @Inject
    private Configurer configurer;

    @Inject
    private Injector injector;

    private final Class[] exclude = new Class[]{Serializable.class, Remote.class};

    @Override
    public <T> void preProcess(IServiceBuilder<T> builder, T bean) {
        SpaceTakerConfiguration cfg = getConfiguration(SpaceTakerConfiguration.class, builder.getProviderConfiguration());
        if (!cfg.spaceEnabled)
            return;

        Set<Class> excludeInterfaces = new HashSet<Class>(Arrays.asList(exclude));
        Set<Class> allInterfaces = getAllInterfaces(builder.getType(), excludeInterfaces);
        allInterfaces.removeAll(excludeInterfaces);
        cfg.interfaces = allInterfaces;

        spaceTaker.addService(builder, bean, cfg);
    }

    @Override
    public <T> void destroy(IServiceBuilder<T> serviceBuilder, T bean) {
        spaceTaker.removeService(bean);
    }

    protected static Set<Class> getAllInterfaces(Class type, Set<Class> excluded) {
        Set<Class> result = new HashSet<Class>();
        getAllInterfaces(type, result, excluded);
        return result;
    }

    protected static void getAllInterfaces(Class type, Set<Class> result, Set<Class> excluded) {
        if (excluded.contains(type))
            return;
        result.add(type);
        Class superclass = type.getSuperclass();
        if (superclass != null)
            getAllInterfaces(superclass, result);
        for (Class iface : type.getInterfaces())
            getAllInterfaces(iface, result);
    }

    protected <T> T getConfiguration(Class<T> configurableType, Configuration configuration) {
        return injector.createChildInjector(new ConfiguringModule(configuration, configurer, configurableType)).getInstance(configurableType);
    }
}
