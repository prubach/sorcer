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

package sorcer.junit;

import org.junit.Ignore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.launcher.Launcher;
import sorcer.launcher.SorcerLauncher;
import sorcer.launcher.WaitMode;
import sorcer.launcher.WaitingListener;
import sorcer.protocol.ProtocolHandlerRegistry;
import sorcer.resolver.Resolver;
import sorcer.resolver.VersionResolver;
import sorcer.util.*;
import sorcer.util.url.HandlerInstaller;

import java.io.File;
import java.net.URL;
import java.security.Policy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static sorcer.core.SorcerConstants.CODEBASE_SEPARATOR;
import static sorcer.core.SorcerConstants.SORCER_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;
import static sorcer.util.JavaSystemProperties.RMI_SERVER_CLASS_LOADER;

/**
 * @author Rafał Krupiński
 */
public class SorcerRunner extends SorcerTestRunner {
    private static File home;
    private static final Logger log;

    private SorcerServiceConfiguration configuration;

    static {
        String homePath = System.getProperty(SORCER_HOME, System.getenv(E_SORCER_HOME));
        if (homePath != null)
            home = new File(homePath);

        JavaSystemProperties.ensure("logback.configurationFile", new File(home, "configs/logback.groovy").getPath());
        JavaSystemProperties.ensure(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|org.rioproject.url");
        JavaSystemProperties.ensure("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));
        JavaSystemProperties.ensure(RMI_SERVER_CLASS_LOADER, "sorcer.rio.rmi.SorcerResolvingLoader");
        JavaSystemProperties.ensure(SorcerConstants.SORCER_WEBSTER_INTERNAL, Boolean.TRUE.toString());
        log = LoggerFactory.getLogger(SorcerRunner.class);
    }

        public SorcerRunner(Class<?> klass) throws InitializationError {
            this(klass, klass.getAnnotation(SorcerServiceConfiguration.class));
        }

    /**
     * @throws org.junit.runners.model.InitializationError if the test class is malformed.
     */
    public SorcerRunner(Class<?> klass, SorcerServiceConfiguration configuration) throws InitializationError {
        super(klass);
        if (home == null)
            throw new InitializationError("sorcer.home property is required");

        String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
        if (policyPath != null) {
            File policy = new File(policyPath);
            IOUtils.checkFileExistsAndIsReadable(policy);
        } else {
            if (System.getSecurityManager() != null)
                throw new InitializationError("SecurityManager set but no " + JavaSystemProperties.SECURITY_POLICY);
            File policy = new File(home, "configs/sorcer.policy");
            IOUtils.checkFileExistsAndIsReadable(policy);
            System.setProperty(JavaSystemProperties.SECURITY_POLICY, policy.getPath());
            Policy.getPolicy().refresh();
        }

        this.configuration = configuration;
        checkAnnotations(klass);

        System.setSecurityManager(new SecurityManager());

        //SLF4JBridgeHandler.removeHandlersForRootLogger();
        //SLF4JBridgeHandler.install();

        ExportCodebase exportCodebase = klass.getAnnotation(ExportCodebase.class);
        String[] codebase = exportCodebase != null ? exportCodebase.value() : null;
        if (codebase != null && codebase.length > 0)
            prepareCodebase(codebase);
    }

    private void prepareCodebase(String[] codebase) {
        Set<URL> codebaseUrls = new HashSet<URL>();
        for (String cbentry : codebase) {
            ArtifactCoordinates artifact = ArtifactCoordinates.coords(cbentry);
            if (artifact.getVersion() == null)
                artifact.setVersion(VersionResolver.instance.resolveVersion(artifact));
            codebaseUrls.add(GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), artifact.toString()));
        }
        System.setProperty(JavaSystemProperties.RMI_SERVER_CODEBASE, StringUtils.join(codebaseUrls, CODEBASE_SEPARATOR));
    }

    private void checkAnnotations(Class<?> klass) throws InitializationError {
        if (configuration == null && klass.getAnnotation(SorcerServiceConfigurations.class) != null)
            throw new InitializationError(SorcerServiceConfigurations.class.getName() + " annotation present on class " + klass.getName() + ". Please run with SorcerSuite.");
    }

    @Override
    public void run(final RunNotifier notifier) {
        if (configuration == null) {
            // handler is installed by the SorcerLauncher, here we must do it ourselves
            new HandlerInstaller(ProtocolHandlerRegistry.get());
            super.run(notifier);
            return;
        }

        Launcher sorcerLauncher = null;
        String[] configs = configuration.value();
        try {
            sorcerLauncher = startSorcer(configs);
            super.run(notifier);
        } catch (Throwable e) {
            log.error("Error", e);
            notifier.fireTestFailure(new Failure(getDescription(), new Exception("Error while starting SORCER with configs: " + Arrays.toString(configs), e)));
        } finally {
            try {
                if (sorcerLauncher != null)
                    sorcerLauncher.stop();
            }catch(RuntimeException e){
                log.error("Error",e);
                notifier.fireTestFailure(new Failure(getDescription(), new Exception("Error while stopping SORCER with configs: " + Arrays.toString(configs), e)));
            }
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (method.getAnnotation(Ignore.class) == null)
            log.info("Testing {}", method.getMethod());
        super.runChild(method, notifier);
    }

    private Launcher startSorcer(String[] serviceConfigPaths) throws Exception {
        WaitingListener listener = new WaitingListener();

        Launcher launcher = new SorcerLauncher();
        launcher.setConfigs(new LinkedList<String>(Arrays.asList(serviceConfigPaths)));
        launcher.addSorcerListener(listener);
        launcher.setHome(home);
        log.info("Setting S_HOME: " + home);
        File logDir = new File(System.getProperty("java.io.tmpdir"), "logs");
        logDir.mkdir();
        launcher.setLogDir(logDir);

        log.info("Starting SORCER instance for test {}", getDescription());
        launcher.preConfigure();
        launcher.start();

        listener.wait(WaitMode.start);

        return launcher;
    }
}
