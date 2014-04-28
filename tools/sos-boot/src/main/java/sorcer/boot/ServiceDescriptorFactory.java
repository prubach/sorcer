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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.destroy.ServiceDestroyerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ServiceDescriptorFactory {
    private static final Logger log = LoggerFactory.getLogger(ServiceDestroyerFactory.class);

    public List<? extends ServiceDescriptor> create(File path) throws ConfigurationException {
        String ext = FilenameUtils.getExtension(path.getPath());
        IServiceDescriptorFactory descriptorFactory = descriptorFactories.get(ext);
        if (descriptorFactory == null)
            throw new IllegalArgumentException("Unknown file extension " + path);
        return descriptorFactory.create(path);
    }

    private Map<String, IServiceDescriptorFactory> descriptorFactories = new HashMap<String, IServiceDescriptorFactory>();

    @Inject
    public void setDescriptorFactories(Set<IServiceDescriptorFactory> set) {
        for (IServiceDescriptorFactory descriptorFactory : set) {
            for (String ext : descriptorFactory.handledExtensions()) {
                if (descriptorFactories.containsKey(ext)) {
                    log.warn("Not overriding descriptor factory for {}", ext);
                    break;
                } else
                    descriptorFactories.put(ext, descriptorFactory);
            }
        }
    }

}
