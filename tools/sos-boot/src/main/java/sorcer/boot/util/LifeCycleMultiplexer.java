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

package sorcer.boot.util;

import com.sun.jini.start.LifeCycle;

import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class LifeCycleMultiplexer implements LifeCycle {
    private Set<LifeCycle> backend;

    public LifeCycleMultiplexer(Set<LifeCycle> backend) {
        this.backend = backend;
    }

    @Override
    public boolean unregister(Object impl) {
        boolean result = true;
        for (LifeCycle lifeCycle : backend) {
            result &= lifeCycle.unregister(impl);
        }
        return result;
    }

    public boolean add(LifeCycle lifeCycle){
        return backend.add(lifeCycle);
    }
}
