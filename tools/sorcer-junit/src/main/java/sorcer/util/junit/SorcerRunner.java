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

package sorcer.util.junit;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.bridge.SLF4JBridgeHandler;
import sorcer.core.SorcerEnv;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.resolver.Resolver;
import sorcer.tools.webster.Webster;
import sorcer.util.IOUtils;
import sorcer.util.JavaSystemProperties;

import java.io.File;

import static sorcer.core.SorcerConstants.SORCER_HOME;

/**
 * @author Rafał Krupiński
 */
public class SorcerRunner extends BlockJUnit4ClassRunner {
    private static Webster webster;
    private Class<?> klass;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws org.junit.runners.model.InitializationError if the test class is malformed.
     */
    public SorcerRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.klass = klass;
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            try {
                JavaSystemProperties.ensure(SORCER_HOME);
            } catch (IllegalStateException x) {
                notifier.fireTestFailure(new Failure(getDescription(), x));
                return;
            }

            JavaSystemProperties.ensure("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));

            File home = new File(System.getProperty(SORCER_HOME));
            JavaSystemProperties.ensure("logback.configurationFile", new File(home, "configs/logback.groovy").getPath());
            JavaSystemProperties.ensure(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");

            File policy;
            String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
            if (policyPath != null)
                policy = new File(policyPath);
            else {
                if (System.getSecurityManager() != null) {
                    notifier.fireTestFailure(new Failure(getDescription(), new IllegalStateException("SecurityManager set but no " + JavaSystemProperties.SECURITY_POLICY)));
                    return;
                }
                policy = new File(home, "configs/sorcer.policy");
                JavaSystemProperties.ensure(JavaSystemProperties.SECURITY_POLICY, policy.getPath());
            }
            IOUtils.checkFileExistsAndIsReadable(policy);

            if (System.getSecurityManager() == null) {
                SLF4JBridgeHandler.removeHandlersForRootLogger();
                SLF4JBridgeHandler.install();

                System.setSecurityManager(new SecurityManager());
            }


            SorcerEnv.debug = true;
            if (webster == null)
                webster = ServiceRequestor.prepareCodebase();

            String[]serviceConfigPaths=getServiceConfigPaths();
            if(serviceConfigPaths!=null)
                startSorcer(serviceConfigPaths);

            super.run(notifier);


        } catch (RuntimeException x) {
            notifier.fireTestFailure(new Failure(getDescription(), x));
        } catch (Error x) {
            notifier.fireTestFailure(new Failure(getDescription(), x));
        }
    }

    private void startSorcer(String[] serviceConfigPaths) {

    }

    private String[] getServiceConfigPaths() {
        return klass.getAnnotation(SorcerService.class).value();
    }
}
