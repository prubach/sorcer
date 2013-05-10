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

import sorcer.provider.boot.SorcerCapabilityDescriptor
import sorcer.resolver.Resolver

def getPlatformCapabilityConfig() {
    return new SorcerCapabilityDescriptor("Sigar",
            "1.6.4",
            "Hyperic SIGAR",
            "Hyperic",
            ["org.sorcersoft.sigar:sigar:1.6.4"],
            getLib("org.sorcersoft.sigar:sigar:zip:native:1.6.4")
    )
}

String getLib(String coords) {
    return new File(new File(Resolver.resolveAbsolute(coords)).getParent(), "lib").getPath();
}
