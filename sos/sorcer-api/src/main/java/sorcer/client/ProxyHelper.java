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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Rafał Krupiński
 */
public class ProxyHelper {
    public static boolean isReady(Object proxy) {
        return getInvocationHandler(proxy).getProvider().get() != null;
    }

    protected static <T> RefreshableInvocationHandler<T> getInvocationHandler(T proxy) {
        if (!Proxy.isProxyClass(proxy.getClass())) throw new IllegalArgumentException("Not a proxy");
        InvocationHandler ih = Proxy.getInvocationHandler(proxy);
        if (!(ih instanceof RefreshableInvocationHandler))
            throw new IllegalArgumentException("Not a client proxy");
        return (RefreshableInvocationHandler) ih;
    }

    public static <T> T getUnderlyingProxy(T proxy) {
        return getInvocationHandler(proxy).getProvider().get();
    }
}
