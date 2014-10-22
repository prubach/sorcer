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
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import org.rioproject.opstring.ServiceElement;
import sorcer.core.SorcerEnv;
import sorcer.core.deploy.ServiceDeployment;
import sorcer.core.deploy.ServiceElementFactory;
import static sorcer.core.SorcerConstants.DEPLOYMENT_PACKAGE;

import java.io.File;
import java.util.*;

/**
 * @author Paweł Rubach
 */
public class ConfigDeploymentDescriptorFactory extends RiverDescriptorFactory implements IServiceDescriptorFactory {
    @Override
    public Collection<String> handledExtensions() {
        return Arrays.asList("config");
    }

    @Override
    public List<? extends ServiceDescriptor> create(File file) throws ConfigurationException {
        Configuration config = ConfigurationProvider.getInstance(new String[]{file.getPath()});
        String[] interfaces = (String[])
                config.getEntry(DEPLOYMENT_PACKAGE, "interfaces",
                        String[].class, null);

        List<ServiceDescriptor> sDescs = new ArrayList<ServiceDescriptor>();
        if (interfaces!=null && interfaces.length>0) {

            ServiceDeployment sd = new ServiceDeployment();
            sd.setConfig(file.getAbsolutePath());
            try {
                ServiceElement sel = ServiceElementFactory.create(sd);
                OpstringServiceDescriptor osd = new OpstringServiceDescriptor(sel, new File(SorcerEnv.getHomeDir(), "configs/sorcer.policy"));
                sDescs.add(osd);
                return sDescs;
            } catch (Exception e) {
                throw new ConfigurationException("Cannot interpret the Deployment Config file", e);
            }
        } else {
            return super.create(file);
        }
    }
}
