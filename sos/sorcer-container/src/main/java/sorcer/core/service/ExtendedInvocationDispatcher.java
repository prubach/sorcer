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

package sorcer.core.service;

import com.google.common.collect.Lists;
import net.jini.core.constraint.MethodConstraints;
import net.jini.jeri.BasicInvocationDispatcher;
import net.jini.jeri.ServerCapabilities;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class ExtendedInvocationDispatcher extends BasicInvocationDispatcher {
    private List<MethodInterceptor> interceptors;
    private Object target;

    public ExtendedInvocationDispatcher(Collection methods, ServerCapabilities serverCapabilities, MethodConstraints serverConstraints, Class permissionClass, ClassLoader loader,
                                        List<MethodInterceptor> interceptors,
                                        Object target) throws ExportException {
        super(methods, serverCapabilities, serverConstraints, permissionClass, loader);
        this.interceptors = interceptors;
        this.target = target;
    }

    @Override
    protected Object invoke(Remote impl, Method method, Object[] args, Collection context) throws Throwable {
        MethodInvocation invocation = new DefaultMethodInvocation(method, args, target);

        for (MethodInterceptor interceptor : Lists.reverse(interceptors)) {
            invocation = new InterceptedMethodInvocation(interceptor, invocation);
        }
        return invocation.proceed();
    }
}

class DefaultMethodInvocation implements MethodInvocation {
    private Method method;
    private Object[] arguments;
    private Object target;

    public DefaultMethodInvocation(Method method, Object[] arguments, Object target) {
        this.method = method;
        this.arguments = arguments;
        this.target = target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, arguments);
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }
}

class InterceptedMethodInvocation implements MethodInvocation {
    private MethodInterceptor interceptor;
    private MethodInvocation invocation;

    public InterceptedMethodInvocation(MethodInterceptor interceptor, MethodInvocation invocation) {
        this.interceptor = interceptor;
        this.invocation = invocation;
    }

    @Override
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    public Object[] getArguments() {
        return invocation.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return interceptor.invoke(this);
    }

    @Override
    public Object getThis() {
        return interceptor;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return invocation.getStaticPart();
    }
}