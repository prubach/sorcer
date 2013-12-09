/*
 * Copyright to the original author or authors.
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

/*
 * This configuration is used to start a ProvisionMonitor, Cybernode, Webster and a Lookup Service
 */

import org.rioproject.util.ServiceDescriptorUtil
import org.rioproject.config.Component
import com.sun.jini.start.ServiceDescriptor
import org.rioproject.resolver.maven2.Repository

@Component('org.rioproject.start')
class StartAllConfig {
    String home = System.getenv("SORCER_HOME")
    String rioHome = System.getenv("RIO_HOME")

    ServiceDescriptor[] getServiceDescriptors() {
        ServiceDescriptorUtil.checkForLoopback()
        String m2Repo = Repository.getLocalRepository().absolutePath

        def websterRoots = [rioHome+'/lib-dl', ';',
                            rioHome+'/lib',    ';',
                            home+'/deploy', ';',
                            home+'/lib', ';',
                            m2Repo]

        String policyFile = home+'/configs/rio/rio.policy'
        def monitorConfigs = [home+'/configs/rio/common.groovy',
                              home+'/configs/rio/monitor.groovy']
        def reggieConfigs = [home+'/configs/rio/common.groovy',
                             home+'/configs/rio/reggie.groovy']
        def cybernodeConfigs = [home+'/configs/rio/common.groovy',
                                home+'/configs/rio/cybernode.groovy',
                                home+'/configs/rio/compute_resource.groovy']

        return [
            ServiceDescriptorUtil.getWebster(policyFile, '9010', websterRoots as String[]),
            ServiceDescriptorUtil.getLookup(policyFile, reggieConfigs as String[]),
            ServiceDescriptorUtil.getMonitor(policyFile, monitorConfigs as String[]),
            ServiceDescriptorUtil.getCybernode(policyFile, cybernodeConfigs as String[])
        ] as ServiceDescriptor[];
    }
}