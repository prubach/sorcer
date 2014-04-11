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

import javax.inject.Provider;
import java.lang.reflect.Proxy;

/**
 * Factory of client proxy objects
 * <p/>
 * TODO support injection with @Inject
 *
 * @author Rafał Krupiński
 */
public class RefreshableProxyFactory<T> implements Provider<T> {
    private Provider<T> backend;
    private ClassLoader cl;
    private Class<T> type;

    public RefreshableProxyFactory(Class<T> type) {
        this.type = type;
        backend = new ProxyFactory<T>(type);
        cl = type.getClassLoader();
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) Proxy.newProxyInstance(cl, new Class[]{type}, new RefreshableInvocationHandler(backend));
    }
}
