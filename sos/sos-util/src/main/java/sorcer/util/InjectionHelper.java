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

package sorcer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class InjectionHelper {
    private static final Logger log = LoggerFactory.getLogger(InjectionHelper.class);

    public static Injector getInstance() {
        return instance;
    }

    public static void setInstance(Injector instance) {
        InjectionHelper.instance = instance;
    }

    public static interface Injector {
        public void injectMembers(Object target);

        public <T> T create(Class<T> type);
    }

    private static Injector instance;

    public static void injectMembers(Object target) {
        if (instance != null)
            instance.injectMembers(target);
        else
            log.debug("NOT injecting members, InjectorHelper not initialized while injecting members to {}", target);
    }

    public static <T> T create(Class<T> type) {
        if (instance != null)
            return instance.create(type);
        else {
            log.debug("InjectorHelper not initialized while creating an instance of {}", type);
            try {
                return type.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Could not create " + type);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not create " + type);
            }
        }
    }

    public static boolean valid() {
        return instance != null;
    }
}
