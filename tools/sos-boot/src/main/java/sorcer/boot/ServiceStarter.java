/**
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
package sorcer.boot;

import com.sun.jini.start.ServiceDescriptor;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.EmptyConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.rioproject.impl.opstring.OpStringLoader;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.Artifact;
import org.rioproject.start.LogManagementHelper;
import org.rioproject.start.RioServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.core.SorcerConstants;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.resolver.Resolver;
import sorcer.util.IOUtils;
import sorcer.util.JavaSystemProperties;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

import static sorcer.provider.boot.AbstractServiceDescriptor.Service;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter {
    final private static Logger log = LoggerFactory.getLogger(ServiceStarter.class);
    final private static String START_PACKAGE = "com.sun.jini.start";

    private List<Service> services;
    private volatile boolean bootInterrupted;

    public static void main(String[] args) throws Exception {
        installLogging();
        checkRequiredProperties();
        log.info("******* Starting Sorcersoft.com SORCER *******");
        installSecurityManager();
        start(Arrays.asList(args));
    }

    public static void start(List<String> configs) throws Exception {
        List<Service> services = new LinkedList<Service>();
        ServiceStarter serviceStarter = new ServiceStarter(services);
        ServiceStopper.install(serviceStarter, services);
        serviceStarter.start((Collection<String>) configs);
    }

    private static void installSecurityManager() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    private static void installLogging() {
        //redirect java.util.logging to slf4j/logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManagementHelper.setup();
    }

    private static void checkRequiredProperties() {
        checkRequiredProperty(JavaSystemProperties.PROTOCOL_HANDLER_PKGS);
        checkRequiredProperty(JavaSystemProperties.UTIL_LOGGING_CONFIG_FILE);
        checkRequiredProperty(SorcerConstants.S_KEY_SORCER_ENV);
    }

    private static void checkRequiredProperty(String key) {
        if (System.getProperty(key) == null)
            log.warn("Required system property '{}' not set!", key);
    }

    public ServiceStarter(List<Service> services) {
        this.services = services;
    }

    public void interrupt() {
        bootInterrupted = true;
    }

    /**
     * Start services from the configs
     *
     * @param configs file path or URL of the services.config configuration
     */
    public void start(Collection<String> configs) throws Exception {
        log.debug("Starting from {}", configs);

        List<String> riverServices = new LinkedList<String>();
        List<File> cfgJars = new LinkedList<File>();
        List<File> opstrings = new LinkedList<File>();

        for (String path : configs) {
            File file = null;
            if (path.startsWith(":")) {
                file = findArtifact(path.substring(1));
            } else if (Artifact.isArtifact(path))
                file = new File(Resolver.resolveAbsolute(path));
            if (file == null) file = new File(path);

            IOUtils.ensureFile(file, IOUtils.FileCheck.readable);
            path = file.getPath();
            String ext = path.substring(path.lastIndexOf('.') + 1);

            if ("config".equals(ext))
                riverServices.add(path);
            else if ("oar".equals(ext) || "jar".equals(ext))
                cfgJars.add(file);
            else if ("opstring".equals(ext) || "groovy".equals(ext))
                opstrings.add(file);
            else
                throw new IllegalArgumentException("Unrecognized file " + path);
        }
        Map<Configuration, List<? extends ServiceDescriptor>> descs = new LinkedHashMap<Configuration, List<? extends ServiceDescriptor>>();
        descs.putAll(instantiateDescriptors(riverServices));

        List<OpstringServiceDescriptor> serviceDescriptors = createFromOpStrFiles(opstrings);
        serviceDescriptors.addAll(createFromOar(cfgJars));
        descs.put(EmptyConfiguration.INSTANCE, serviceDescriptors);

        instantiateServices(descs, services);
    }

    private Map<Configuration, List<ServiceDescriptor>> instantiateDescriptors(List<String> riverServices) throws ConfigurationException {
        List<Configuration> configs = new ArrayList<Configuration>(riverServices.size());
        for (String s : riverServices) {
            configs.add(ConfigurationProvider.getInstance(new String[]{s}));
        }
        return instantiateDescriptors(configs);
    }

    private File findArtifact(String artifactId) {
        Collection<File> files = FileUtils.listFiles(new File(System.getProperty("user.dir")), new ArtifactIdFileFilter(artifactId), DirectoryFileFilter.INSTANCE);
        if (files.size() == 0) {
            log.error("Artifact file {} not found", artifactId);
            return null;
        }
        if (files.size() > 1) {
            log.warn("Found {} files possibly matching artifactId, using the first", files.size());
            log.debug("Files found: {}", files);
        }
        return files.iterator().next();
    }

    protected List<OpstringServiceDescriptor> createFromOpStrFiles(Collection<File> files) throws Exception {
        List<OpstringServiceDescriptor> result = new LinkedList<OpstringServiceDescriptor>();
        String policyFile = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
        URL policyFileUrl = new File(policyFile).toURI().toURL();
        OpStringLoader loader = new OpStringLoader();
        for (File opString : files) {
            OperationalString[] operationalStrings = loader.parseOperationalString(opString);
            result.addAll(createServiceDescriptors(operationalStrings, policyFileUrl));
        }
        return result;
    }

    private List<OpstringServiceDescriptor> createFromOar(Iterable<File> oarFiles) throws Exception {
        List<OpstringServiceDescriptor> result = new LinkedList<OpstringServiceDescriptor>();
        for (File oarFile : oarFiles) {
            SorcerOAR oar = new SorcerOAR(oarFile);
            OperationalString[] operationalStrings = oar.loadOperationalStrings();
            URL policyFile = oar.getPolicyFile();
            result.addAll(createServiceDescriptors(operationalStrings, policyFile));
        }
        return result;
    }

    private List<OpstringServiceDescriptor> createServiceDescriptors(OperationalString[] operationalStrings, URL policyFile) throws ConfigurationException {
        List<OpstringServiceDescriptor> descriptors = new LinkedList<OpstringServiceDescriptor>();
        for (OperationalString op : operationalStrings) {
            for (ServiceElement se : op.getServices()) {
                descriptors.add(new OpstringServiceDescriptor(se, policyFile));
            }

            descriptors.addAll(createServiceDescriptors(op.getNestedOperationalStrings(), policyFile));
        }
        return descriptors;
    }

    /**
     * Create a service for each ServiceDescriptor in the map
     *
     * @throws Exception
     */
    public void instantiateServices(Map<Configuration, List<? extends ServiceDescriptor>> descriptorMap, List<AbstractServiceDescriptor.Service> result) throws Exception {
        for (Configuration config : descriptorMap.keySet()) {
            List<? extends ServiceDescriptor> descriptors = descriptorMap.get(config);
            ServiceDescriptor[] descs = descriptors.toArray(new ServiceDescriptor[descriptors.size()]);

            LoginContext loginContext = (LoginContext)
                    config.getEntry(START_PACKAGE, "loginContext",
                            LoginContext.class, null);
            if (loginContext != null)
                createWithLogin(descs, config, loginContext, result);
            else
                create(descs, config, result);
            checkResultFailures(result);
        }
    }

    public static Map<Configuration, List<ServiceDescriptor>> instantiateDescriptors(Collection<Configuration> configs) throws ConfigurationException {
        Map<Configuration, List<ServiceDescriptor>> result = new HashMap<Configuration, List<ServiceDescriptor>>();
        for (Configuration config : configs) {
            ServiceDescriptor[] descs = (ServiceDescriptor[])
                    config.getEntry(START_PACKAGE, "serviceDescriptors",
                            ServiceDescriptor[].class, null);
            if (descs == null || descs.length == 0) {
                log.warn("service.config.empty");
                return result;
            }
            result.put(config, Arrays.asList(descs));
        }
        return result;
    }

    /**
     * Generic service creation method that attempts to start the
     * services defined by the provided <code>ServiceDescriptor[]</code>
     * argument.
     *
     * @param descs  The <code>ServiceDescriptor[]</code> that contains
     *               the descriptors for the services to start.
     * @param config The associated <code>Configuration</code> object
     *               used to customize the service creation process.
     * @throws Exception If there was a problem creating the service.
     * @see com.sun.jini.start.ServiceStarter.Result
     * @see com.sun.jini.start.ServiceDescriptor
     * @see net.jini.config.Configuration
     */
    public void create(ServiceDescriptor[] descs, Configuration config, Collection<AbstractServiceDescriptor.Service> proxies) throws Exception {
        for (ServiceDescriptor desc : descs) {
            if (bootInterrupted)
                break;
            if (desc != null) {
                AbstractServiceDescriptor.Service service = null;
                try {
                    if (desc instanceof AbstractServiceDescriptor)
                        service = (Service) desc.create(config);
                    else if (desc instanceof RioServiceDescriptor) {
                        RioServiceDescriptor.Created created = (RioServiceDescriptor.Created) desc.create(config);
                        service = new Service(created.impl, created.proxy, desc);
                    } else
                        service = new AbstractServiceDescriptor.Service(desc.create(config), null, desc);
                } catch (Exception e) {
                    service = new Service(null, null, desc, e);
                } finally {
                    if (service != null)
                        proxies.add(service);
                }
            }
        }
    }

    /**
     * Generic service creation method that attempts to login via
     * the provided <code>LoginContext</code> and then call the
     * <code>create</code> overload without a login context argument.
     *
     * @param descs        The <code>ServiceDescriptor[]</code> that contains
     *                     the descriptors for the services to start.
     * @param config       The associated <code>Configuration</code> object
     *                     used to customize the service creation process.
     * @param loginContext The associated <code>LoginContext</code> object
     *                     used to login/logout.
     * @throws Exception If there was a problem logging in/out or
     *                   a problem creating the service.
     * @see com.sun.jini.start.ServiceStarter.Result
     * @see com.sun.jini.start.ServiceDescriptor
     * @see net.jini.config.Configuration
     * @see javax.security.auth.login.LoginContext
     */
    private void createWithLogin(
            final ServiceDescriptor[] descs, final Configuration config,
            final LoginContext loginContext,
            final Collection<AbstractServiceDescriptor.Service> result)
            throws Exception {
        loginContext.login();

        try {
            Subject.doAsPrivileged(
                    loginContext.getSubject(),
                    new PrivilegedExceptionAction() {
                        public Object run()
                                throws Exception {
                            create(descs, config, result);
                            return null;
                        }
                    },
                    null);
        } catch (PrivilegedActionException pae) {
            throw pae.getException();
        } finally {
            try {
                loginContext.logout();
            } catch (LoginException le) {
                log.warn("service.logout.exception", le);
            }
        }
    }

    /**
     * Utility routine that prints out warning messages for each service
     * descriptor that produced an exception or that was null.
     */
    private static void checkResultFailures(List<AbstractServiceDescriptor.Service> results) {
        for (AbstractServiceDescriptor.Service result : results) {
            if (result.exception != null) {
                log.warn("service.creation.unknown", result.exception);
                log.warn("service.creation.unknown.detail", result.descriptor);
            } else if (result.descriptor == null) {
                log.warn("service.creation.null");
            }
        }
    }

    private static class ArtifactIdFileFilter extends AbstractFileFilter {
        private String artifactId;

        public ArtifactIdFileFilter(String artifactId) {
            this.artifactId = artifactId;
        }

        @Override
        public boolean accept(File dir, String name) {
            String parent = dir.getName();
            String grandParent = dir.getParentFile().getName();
            return
                    new File(dir,name).isFile() && name.startsWith(artifactId + "-") && name.endsWith(".jar") && (
                            //check development structure
                            "target".equals(parent)
                                    //check repository just in case
                                    || artifactId.equals(grandParent)
                    )
                            //check distribution structure
                            || "lib".equals(grandParent) && (artifactId + ".jar").equals(name)
                    ;
        }
    }
}
