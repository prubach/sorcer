package sorcer.core.context;
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.schema.Path;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Direction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Rafał Krupiński
 */
public class DomainModelHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(DomainModelHandler.class);

    private String prefix;
    private Context context;
    private Class iface;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Path path = method.getAnnotation(Path.class);
        if (path == null)
            throw new IllegalArgumentException("Method " + method + " must be annotated with @sorcer.schema.Path");
        String contextPath = getPath(path);
        if (method.getReturnType().equals(Void.class)) {
            setValue(method, args, path, contextPath);
            return null;
        } else {
            return getValue(method, args, path, contextPath);
        }

    }

    private String getPath(Path path) {
        return prefix + "/" + path.value();
    }

    private Object getValue(Method method, Object[] args, Path path, String contextPath) {
        if (args.length != 0) log.warn("Schema method " + method + " has non-void return type and parameters");
        return context.get(contextPath);
    }

    private void setValue(Method method, Object[] args, Path path, String contextPath) throws IllegalAccessException, ContextException {
        if (args.length == 0)
            throw new IllegalAccessException("Schema method " + method + " does not take exactly one parameter");
        context.putValue(contextPath, args);
    }
}
