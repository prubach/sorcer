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

package sorcer.core.service;

import com.google.inject.Injector;
import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.start.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.container.core.BeanListenerModule;
import sorcer.container.core.SingletonModule;
import sorcer.util.InjectionHelper;

import javax.inject.Inject;

import java.rmi.RemoteException;

import static sorcer.container.core.InitializingModule.INIT_MODULE;

/**
 * Thin layer between jini-style ctor(String[], Lifecycle) and javax.inject worlds.
 * Intended to replace sorcer.core.provider.ServiceProvider in service descriptors.
 *
 * @author Rafał Krupiński
 */
public class ServiceBuilder implements DestroyAdmin{
    private static final Logger log = LoggerFactory.getLogger(ServiceBuilder.class);

    @Inject
    private Injector injector;
    private ActualServiceBuilder instance;

    public ServiceBuilder(String[] args, LifeCycle lifeCycle) {
        InjectionHelper.injectMembers(this);
        instance = injector.createChildInjector(
                injector.getInstance(BeanListenerModule.class),
                INIT_MODULE,
                new ServiceModule(lifeCycle, args),
                new SingletonModule(ActualServiceBuilder.class)
        ).getInstance(ActualServiceBuilder.class);
        log.debug("Created instance {}", instance);
    }

    @Override
    public void destroy() throws RemoteException {
        instance.destroy();
    }
}
