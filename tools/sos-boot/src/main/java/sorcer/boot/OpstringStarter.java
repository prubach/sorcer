package sorcer.boot;
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


import com.sun.jini.start.AggregatePolicyProvider;
import com.sun.jini.start.ClassLoaderUtil;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.LoaderSplitPolicyProvider;
import com.sun.jini.start.ServiceProxyAccessor;
import net.jini.config.Configuration;
import net.jini.config.EmptyConfiguration;
import net.jini.export.ProxyAccessor;
import net.jini.security.BasicProxyPreparer;
import net.jini.security.ProxyPreparer;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.security.policy.PolicyFileProvider;
import org.rioproject.impl.opstring.OAR;
import org.rioproject.impl.opstring.OpStringLoader;
import org.rioproject.opstring.ClassBundle;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import sorcer.provider.boot.AbstractServiceDescriptor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RMISecurityManager;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Policy;

/**
 * Starts sorcer with opstring as a service descriptor
 *
 * @author Rafał Krupiński
 */
public class OpstringStarter {
    public static void main(String[] args) throws Exception {
        security();
        OpstringStarter opstringStarter = new OpstringStarter();
        for (String arg : args) {
            opstringStarter.createServices(new File(arg));
        }
    }

    private static void security() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }

    private void createServices(File file) throws Exception {
        OAR oar = new OAR(file);
        OperationalString[] operationalStrings = oar.loadOperationalStrings();
        URL oarUrl = file.toURI().toURL();

        for (OperationalString op : operationalStrings) {
            for (ServiceElement se : op.getServices()) {
                new OpstringServiceDescriptor(se, oarUrl).create(EmptyConfiguration.INSTANCE);
            }
        }

    }
}

class OpstringServiceDescriptor extends AbstractServiceDescriptor {
    public static final NoOpLifeCycle NOOP = new NoOpLifeCycle();
    private ServiceElement serviceElement;
    private ProxyPreparer servicePreparer = new BasicProxyPreparer();
    private URL oar;

    private static AggregatePolicyProvider globalPolicy = null;
    private static Policy initialGlobalPolicy = null;

    public OpstringServiceDescriptor(ServiceElement serviceElement, URL oar) {
        this.serviceElement = serviceElement;
        this.oar = oar;
    }

    @Override
    protected Object doCreate(Configuration config) throws Exception {
        ClassLoader cl = getClassLoader(serviceElement.getComponentBundle(), serviceElement, getCommonClassLoader(config), new URL[]{oar});

        synchronized (AbstractServiceDescriptor.class) {
            String policyFile = System.getenv("POLICY_FILE");
            if (policyFile == null) throw new IllegalStateException("POLICY_FILE environment variable must be defined");
            else {
                File pFile = new File(policyFile);
                if (!pFile.exists()) throw new IllegalStateException("POLICY_FILE must exist " + policyFile);
                if (!pFile.isFile()) throw new IllegalStateException("POLICY_FILE must be a file " + policyFile);
                if (!pFile.canRead()) throw new IllegalStateException("POLICY_FILE must be readable " + policyFile);
            }
            DynamicPolicyProvider service_policy = new DynamicPolicyProvider(
                    new PolicyFileProvider(policyFile));
            LoaderSplitPolicyProvider splitServicePolicy = new LoaderSplitPolicyProvider(
                    cl, service_policy, new DynamicPolicyProvider(
                    initialGlobalPolicy));
            /*
			 * Grant "this" code enough permission to do its work under the
			 * service policy, which takes effect (below) after the context
			 * loader is (re)set.
			 */
            splitServicePolicy.grant(OpstringServiceDescriptor.class, null,
                    new Permission[]{new AllPermission()});
            globalPolicy.setPolicy(cl, splitServicePolicy);
        }


        Class implClass = cl.loadClass(serviceElement.getComponentBundle().getClassName());
        ClassLoaderUtil.displayClassLoaderTree(cl);
        Object impl;
        Object proxy;
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);
        try {


            logger.trace("Attempting to get implementation constructor");
            Constructor constructor = implClass.getDeclaredConstructor(new Class[]{String[].class, LifeCycle.class});
            logger.trace("Obtained implementation constructor: {}", constructor.toString());
            constructor.setAccessible(true);
            impl = constructor.newInstance(serviceElement.getServiceBeanConfig().getConfigArgs(), NOOP);
            logger.trace("Obtained implementation instance: {}", impl.toString());
            if (impl instanceof ServiceProxyAccessor) {
                proxy = ((ServiceProxyAccessor) impl).getServiceProxy();
            } else if (impl instanceof ProxyAccessor) {
                proxy = ((ProxyAccessor) impl).getProxy();
            } else {
                proxy = null; // just for insurance
            }
            if (proxy != null) {
                proxy = servicePreparer.prepareProxy(proxy);
            }
            logger.trace("Proxy:  {}", proxy == null ? "<NULL>" : proxy.toString());

        } catch (InvocationTargetException e) {
            Throwable t = e.getCause() == null ? e.getTargetException() : e.getCause();
            if (t != null && t instanceof Exception)
                throw (Exception) t;
            throw e;

        } finally {
            currentThread.setContextClassLoader(currentClassLoader);
        }
        return new Created(impl, proxy);
    }


    private static URLClassLoader getClassLoader(ClassBundle bundle, ServiceElement serviceElement, ClassLoader parentCL, URL[] extraCp) throws ResolverException, MalformedURLException {
        String[] urlStrings = ResolverHelper.resolve(bundle.getArtifact(), ResolverHelper.getResolver(), serviceElement.getRemoteRepositories());
        URL[] urls = new URL[urlStrings.length + extraCp.length];

        for (int i = 0; i < urlStrings.length; i++) {
            String urlString = urlStrings[i];
            urls[i] = new URL(urlString);
        }
        System.arraycopy(extraCp, 0, urls, urlStrings.length, extraCp.length);
        return new URLClassLoader(urls, parentCL);
    }

    private static class NoOpLifeCycle implements LifeCycle {
        @Override
        public boolean unregister(Object impl) {
            return false;
        }
    }
}