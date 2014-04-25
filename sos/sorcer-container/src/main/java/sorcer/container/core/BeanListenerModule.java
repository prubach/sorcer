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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import sorcer.core.service.IServiceBeanListener;
import sorcer.core.service.IServiceBuilder;

import javax.inject.Inject;

/**
 * @author Rafał Krupiński
 */
public class BeanListenerModule extends AbstractModule{

    @Inject
    protected IServiceBeanListener beanListener;

    @Override
    protected void configure() {
        bindListener(new TypeMatcher(IServiceBuilder.class), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(I injectee) {
                        IServiceBuilder builder = (IServiceBuilder) injectee;
                        beanListener.preProcess(builder);
                    }
                });
            }
        });
    }
}
