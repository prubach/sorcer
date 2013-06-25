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
import sorcer.service.Parameter;

/**
 * Extracted from operator
 *
 * @author Rafał Krupiński
 */
public class OutEntry<T> extends IndexedTriplet implements Parameter {
    private static final long serialVersionUID = 1L;
    public boolean flag;

    public OutEntry(String path, T value, boolean flag) {
        T v = value;
        if (v == null)
            v = (T) Context.Value.NULL;

        this._1 = path;
        this._2 = v;
        this.flag = flag;
    }

    public OutEntry(String path, T value, int index) {
        T v = value;
        if (v == null)
            v = (T) Context.Value.NULL;

        this._1 = path;
        this._2 = v;
        this.index = index;
    }

    public OutEntry(String path, Object fidelity) {
        this._1 = path;
        this._3 = fidelity;
    }
}
