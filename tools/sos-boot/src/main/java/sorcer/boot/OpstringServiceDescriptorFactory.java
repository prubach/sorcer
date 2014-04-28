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

package sorcer.boot;

import com.sun.jini.start.ServiceDescriptor;
import net.jini.config.ConfigurationException;
import org.rioproject.impl.opstring.OpStringLoader;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;
import sorcer.util.JavaSystemProperties;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class OpstringServiceDescriptorFactory implements IServiceDescriptorFactory {
    protected OpStringLoader loader = new OpStringLoader();

    @Override
    public java.util.Collection<String> handledExtensions() {
        return Arrays.asList("groovy", "opstring");
    }

    @Override
    public List<? extends ServiceDescriptor> create(File file) throws ConfigurationException {
        try {
            OperationalString[] operationalStrings = loader.parseOperationalString(file);
            String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
            File policyFile = policyPath != null ? new File(policyPath) : null;
            return createServiceDescriptors(operationalStrings, policyFile);
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not parse Operational String " + file, x);
        } catch (NoClassDefFoundError x) {
            throw new IllegalArgumentException("Could not parse Operational String " + file, x);
        }
    }

    protected List<OpstringServiceDescriptor> createServiceDescriptors(OperationalString[] operationalStrings, File policyFile) throws ConfigurationException {
        List<OpstringServiceDescriptor> descriptors = new LinkedList<OpstringServiceDescriptor>();
        for (OperationalString op : operationalStrings) {
            for (ServiceElement se : op.getServices())
                descriptors.add(new OpstringServiceDescriptor(se, policyFile));
            descriptors.addAll(createServiceDescriptors(op.getNestedOperationalStrings(), policyFile));
        }
        return descriptors;
    }
}
