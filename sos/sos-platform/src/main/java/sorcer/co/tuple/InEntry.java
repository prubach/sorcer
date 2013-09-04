package sorcer.co.tuple;
/**
 *
 * Copyright 2013 Rafał Krupiński.
 * Copyright 2013 Sorcersoft.com S.A.
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


import sorcer.service.Context;
import sorcer.service.Arg;

import java.net.URL;

/**
 * extracted from operator
 *
 * @author Rafał Krupiński
 */
public class InEntry<T> extends IndexedTriplet implements Arg {
    private static final long serialVersionUID = 1L;

    public InEntry(String path, T value, int index) {
        T v = value;
        if (v == null)
            v = (T) Context.none;

        this._1 = path;
        this._2 = v;
        this.index = index;
    }

    public InEntry(String path, T value, boolean isPersistant, int index) {
        this(path, value, index);
        this.isPersistant = isPersistant;
    }

    public InEntry(String path, T value, boolean isPersistant, URL url, int index) {
        this(path, value, index);
        this.isPersistant = isPersistant;
        datastoreURL = url;
    }
}
