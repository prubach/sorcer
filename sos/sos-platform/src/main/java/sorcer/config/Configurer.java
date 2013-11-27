package sorcer.config;
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


import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.NoSuchEntryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.provider.ServiceProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Configure object of which class is annotated with @Component and methods or fields annotated with ConfigEntry
 *
 * @author Rafał Krupiński
 */
public class Configurer extends AbstractBeanListener {

    final private static Logger log = LoggerFactory.getLogger(Configurer.class);

    public void activate(Object[] serviceBeans, ServiceProvider provider) throws ConfigurationException {
        for (Object serviceBean : serviceBeans) {
            try {
                process(serviceBean, provider.getProviderConfiguration());
            } catch (IllegalArgumentException x) {
                log.error("Error while processing {}", serviceBean);
                throw x;
            }
        }
    }

    public void process(Object object, Configuration config) throws ConfigurationException {
        log.debug("Processing {} with {}", object, config);
        if (object instanceof Configurable) {
            ((Configurable) object).configure(config);
        }
        Class<?> targetClass = object.getClass();
        Component configurable = targetClass.getAnnotation(Component.class);
        if (configurable == null) return;

        String component = configurable.value();

        for (Field field : targetClass.getDeclaredFields()) {
            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            if (configEntry != null) {
                updateField(object, field, config, component, configEntry);
            }
        }

        for (Method method : targetClass.getDeclaredMethods()) {
            ConfigEntry configEntry = method.getAnnotation(ConfigEntry.class);
            if (configEntry != null) {
                updateProperty(object, method, config, component, configEntry);
            }
        }
    }

    private void updateProperty(Object object, Method method, Configuration config, String component, ConfigEntry configEntry) {
        Class<?>[] ptypes = method.getParameterTypes();
        if (ptypes.length != 1) return;
        Class<?> type = ptypes[0];

        Object defaultValue = null;
        if (!ConfigEntry.NONE.equals(configEntry.defaultValue())) {
            defaultValue = configEntry.defaultValue();
        } else {
            defaultValue = Configuration.NO_DEFAULT;
        }
        Object value;
        String entryKey = getEntryKey(getPropertyName(method), configEntry);
        try {
            value = config.getEntry(component, entryKey, type, defaultValue);
        } catch (ConfigurationException e) {
            log.warn("Could not configure {} with {} {}", method, entryKey, e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            log.error("Could not configure " + method + " with " + entryKey, e);
            throw e;
        }

        if (type.isPrimitive() && value == null) {
            log.debug("Value for a primitive property is null");
            return;
        }

        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
        } catch (SecurityException ignored) {
            log.warn("Could not set value of {} because of access restriction", method);
            return;
        }

        try {
            method.invoke(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPropertyName(Method m) {
        String name = m.getName();
        if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(4))) {
            return "" + Character.toLowerCase(name.charAt(4)) + name.substring(5);
        }
        return name;
    }

    private void updateField(Object target, Field field, Configuration config, String component, ConfigEntry configEntry) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
        } catch (SecurityException x) {
            log.warn("Could not set value of {} because of access restriction", field);
            return;
        }

        Object defaultValue = null;
        if (!ConfigEntry.NONE.equals(configEntry.defaultValue()) && field.getType().isAssignableFrom(String.class)) {
            defaultValue = configEntry.defaultValue();
        }
        try {
            Object value = config.getEntry(component, getEntryKey(field.getName(), configEntry), field.getType(), defaultValue);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String getEntryKey(String propertyName, ConfigEntry entry) {
        String key;
        if (ConfigEntry.DEFAULT_KEY.equals(entry.value())) {
            return propertyName;
        } else {
            return entry.value();
        }
    }
}
