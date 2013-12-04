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


import net.jini.config.EmptyConfiguration;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;

import java.io.File;
import java.net.URL;
import java.rmi.RMISecurityManager;

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
        SorcerOAR oar = new SorcerOAR(file);
        OperationalString[] operationalStrings = oar.loadOperationalStrings();
        URL policyFile = oar.getPolicyFile();
        URL oarUrl = file.toURI().toURL();

        for (OperationalString op : operationalStrings) {
            for (ServiceElement se : op.getServices()) {
                new OpstringServiceDescriptor(se, oarUrl, policyFile).create(EmptyConfiguration.INSTANCE);
            }
        }
    }
}
