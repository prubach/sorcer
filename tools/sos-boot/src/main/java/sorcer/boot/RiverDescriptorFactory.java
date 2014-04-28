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

import java.io.File;
import java.util.*;

import static sorcer.core.SorcerConstants.START_PACKAGE;

/**
 * @author Rafał Krupiński
 */
public class RiverDescriptorFactory implements IServiceDescriptorFactory {
    @Override
    public Collection<String> handledExtensions() {
        return Arrays.asList("config");
    }

    @Override
    public List<? extends ServiceDescriptor> create(File file) throws ConfigurationException {
        Configuration config = ConfigurationProvider.getInstance(new String[]{file.getPath()});
        ServiceDescriptor[] descs = (ServiceDescriptor[])
                config.getEntry(START_PACKAGE, "serviceDescriptors",
                        ServiceDescriptor[].class, null);

        List<ServiceDescriptor> result = new LinkedList<ServiceDescriptor>();

        if (descs != null)
            Collections.addAll(result, descs);
        return result;
    }
}
