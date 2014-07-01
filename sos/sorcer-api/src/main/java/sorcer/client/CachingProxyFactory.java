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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * @author Rafał Krupiński
 */
public class CachingProxyFactory<T> implements Provider<T> {
    private Reference<T> localCache;

    private final Provider<T> parent;

    public CachingProxyFactory(Provider<T> parent) {
        this.parent = parent;
    }

    @Override
    public T get() {
        if (localCache != null) {
            T result = localCache.get();
            if (result != null)
                return result;
        }

        T result = parent.get();

        if (result != null)
            localCache = new SoftReference<T>(result);

        return result;
    }
}
