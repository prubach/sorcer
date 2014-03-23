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

import sorcer.rio.util.SorcerCapabilityDescriptor


/**
 * Declare common platform jars
 */
class CommonPlatformConfig {
    def getPlatformCapabilityConfigs() {
        return [
                new SorcerCapabilityDescriptor(
                        "Commons Compress",
                        "1.0",
                        "Apache Commons Compress",
                        "Apache Software Foundation",
                        ["org.apache.commons:commons-compress:1.0"]
                ),
                new SorcerCapabilityDescriptor(
                        "plexus-utils",
                        '3.0.15',
                        'Plexus utils - graph traverse',
                        'codehaus.org',
                        'org.codehaus.plexus:plexus-utils'
                )
        ]
    }
}
