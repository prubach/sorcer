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

package sorcer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * @author Rafał Krupiński
 */
public class RefreshableInvocationHandler<T> implements InvocationHandler {
    private Provider<T> targetFactory;
    private T target;
    private final static Logger log = LoggerFactory.getLogger(RefreshableInvocationHandler.class);

    private static final int retries = 3;

    public RefreshableInvocationHandler(Provider<T> targetFactory) {
        this.targetFactory = targetFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            if (target == null)
                target = targetFactory.get();
        }
        if (target == null)
            throw new NullPointerException("No target object from " + targetFactory);

        Object result = null;
        for (int i = 0; i < retries && result == null; i++)
            try {
                try {
                    result = method.invoke(target, args);
                } catch (InvocationTargetException x) {
                    throw x.getCause();
                }
            } catch (RemoteException x) {
                log.warn("Error while calling {} on {} from {}", method, target, targetFactory);
                if (i < retries - 1) {
                    target = targetFactory.get();
                    if (target == null)
                        throw new NullPointerException("No target object from " + targetFactory);
                    log.debug("Using {}", target);
                }
            }
        return result;
    }
}
