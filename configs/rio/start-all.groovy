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

import sorcer.provider.boot.RioServiceDescriptorUtil
import org.rioproject.config.Component
import com.sun.jini.start.ServiceDescriptor
import org.rioproject.resolver.maven2.Repository

@Component('org.rioproject.start')
class StartAllConfig {
    ServiceDescriptor[] getServiceDescriptors() {
        RioServiceDescriptorUtil.checkForLoopback()
        String m2Repo = Repository.getLocalRepository().absolutePath
        String rioHome = System.getProperty('RIO_HOME')

        def websterRoots = [rioHome+'/lib/rio/lib-dl', ';',
                            rioHome+'/lib/rio/lib',    ';',
                            rioHome+'/deploy', ';',
                            m2Repo]

        String policyFile = rioHome+'/configs/rio/rio.policy'
        def monitorConfigs = [rioHome+'/configs/rio/common.groovy',
                              rioHome+'/configs/rio/monitor.groovy']
        def reggieConfigs = [rioHome+'/configs/rio/common.groovy',
                             rioHome+'/configs/rio/reggie.groovy']
        def cybernodeConfigs = [rioHome+'/configs/rio/common.groovy',
                                rioHome+'/configs/rio/cybernode.groovy',
                                rioHome+'/configs/rio/compute_resource.groovy']

        def serviceDescriptors = [
            RioServiceDescriptorUtil.getWebster(policyFile, '9010', websterRoots as String[]),
            RioServiceDescriptorUtil.getLookup(policyFile, reggieConfigs as String[]),
            RioServiceDescriptorUtil.getMonitor(policyFile, monitorConfigs as String[]),
            RioServiceDescriptorUtil.getCybernode(policyFile, cybernodeConfigs as String[])
        ]

        return (ServiceDescriptor[])serviceDescriptors
    }
}