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

import org.rioproject.config.PlatformCapabilityConfig
import sorcer.provider.boot.SorcerCapabilityDescriptor


/**
 * Declare common platform jars
 */
class CommonPlatformConfig {

    def getPlatformCapabilityConfigs() {
        def configs = []

        configs << new SorcerCapabilityDescriptor(
                "Commons Compress",
                "1.5",
                "Apache Commons Compress",
                "Apache Software Foundation",
                ["org.apache.commons:commons-compress:1.5"]
        )
        //String libDir = System.getProperty("RIO_HOME")+File.separator+"lib"+File.separator


        //configs << new PlatformCapabilityConfig("Commons Compress",
        //                                       "1.0",
        //                                        "Apache Commons Compress",
        //                                        "Apache Software Foundation",
        //                                        libDir+"commons-compress-1.0.jar")
        //configs << new PlatformCapabilityConfig("Sigar",
        //                                        "1.6.2",
        //                                        "Hyperic SIGAR",
        //                                        "Hyperic",
        //                                        libDir+"hyperic"+File.separator+"sigar.jar")
        return configs
    }
    
}
