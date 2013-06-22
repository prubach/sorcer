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
 * This configuration is used to start a service that will exec a single service bean
 */

import org.rioproject.start.RioServiceDescriptor
import org.rioproject.config.Component
import com.sun.jini.start.ServiceDescriptor
import org.rioproject.util.FileHelper
import sorcer.provider.boot.RioServiceDescriptorUtil

@Component('org.rioproject.start')
class StartServiceBeanExecConfig {

    String[] getConfigArgs(String rioHome) {
        RioServiceDescriptorUtil.checkForLoopback()
        File common = new File(rioHome + '/../../configs/rio/compiled/common')
        File cybernode = new File(rioHome + '/../../configs/rio/compiled/cybernode')
        File computeResource = new File(rioHome + '/../../configs/rio/compiled/compute_resource')

        def configArgs = []
        configArgs.addAll(FileHelper.getIfExists(common, rioHome + '/../../configs/rio/common.groovy'))
        configArgs.addAll(FileHelper.getIfExists(cybernode, rioHome + '/../../configs/rio/forked_service.groovy'))
        configArgs.addAll(FileHelper.getIfExists(computeResource, rioHome + '/../../configs/rio/compute_resource.groovy'))
        return configArgs as String[]
    }

    ServiceDescriptor[] getServiceDescriptors() {
        String rioHome = System.getProperty('RIO_HOME')
        String codebase = RioServiceDescriptorUtil.getCybernodeCodebase()
        String classpath = RioServiceDescriptorUtil.getCybernodeClasspath()
        
        String policyFile = rioHome + '/../../configs/rio/rio.policy'
        def configArgs = getConfigArgs(rioHome)

        def serviceDescriptors = [
            new RioServiceDescriptor(codebase,
                                     policyFile,
                                     classpath,
                                     'org.rioproject.cybernode.ServiceBeanExecutorImpl',
                                     (String[]) configArgs)
        ]

        return (ServiceDescriptor[]) serviceDescriptors
    }
}
