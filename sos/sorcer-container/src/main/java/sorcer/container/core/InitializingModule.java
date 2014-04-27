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
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static sorcer.util.reflect.Methods.findFirst;

/**
 * Guice module that calls method annotated with {@link javax.annotation.PostConstruct} after the injection
 *
 * @author Rafał Krupiński
 */
public class InitializingModule extends AbstractModule {
    public static final AbstractModule INIT_MODULE = new InitializingModule(new HasInitMethod(), new InitInvoker());

    private HasInitMethod hasInitMethod;
    private InitInvoker initInvoker;

    public InitializingModule(HasInitMethod hasInitMethod, InitInvoker initInvoker) {
        this.hasInitMethod = hasInitMethod;
        this.initInvoker = initInvoker;
    }

    @Override
    protected void configure() {
        bindListener(hasInitMethod, new TypeListener() {
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(initInvoker);
            }
        });
    }
}

class HasInitMethod extends AbstractMatcher<TypeLiteral<?>> {
    private final static Logger log = LoggerFactory.getLogger(HasInitMethod.class);

    public boolean matches(TypeLiteral<?> tpe) {
        Method method = findFirst(tpe.getRawType(), PostConstruct.class);
        log.debug("Found {}", method);
        return method != null;
    }
}

class InitInvoker implements InjectionListener {
    private final static Logger log = LoggerFactory.getLogger(InitInvoker.class);

    public void afterInjection(Object injectee) {
        try {
            Method init = findFirst(injectee.getClass(), PostConstruct.class);
            if (init == null)
                throw new IllegalArgumentException("No init method found");
            else {
                if (log.isDebugEnabled())
                    log.debug("Initializing {} with {}", injectee, init);
                else
                    log.info("Initializing {}", injectee);
                init.invoke(injectee);
            }
        } catch (InvocationTargetException e) {
            handle(e);
        } catch (IllegalAccessException e) {
            handle(e);
        }
    }

    private void handle(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof Error)
            throw (Error) cause;
        else if (cause instanceof RuntimeException)
            throw (RuntimeException) cause;
        else
            throw new IllegalArgumentException(cause);
    }
}
