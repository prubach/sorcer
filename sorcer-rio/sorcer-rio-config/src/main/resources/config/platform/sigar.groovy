/*
* Copyright 2013, 2014 SorcerSoft.com S.A.
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
package config.platform

import sorcer.rio.util.SorcerCapabilityDescriptor
import sorcer.resolver.Resolver;
import sorcer.util.Zip

def getPlatformCapabilityConfig() {
    def version = "1.6.4-3";
    installLibFromArtifact("org.sorcersoft.sigar:sigar-native:zip:" + version, "lib")

    return new SorcerCapabilityDescriptor(
            "Sigar",
            version,
            "Hyperic SIGAR",
            "Hyperic",
            "org.sorcersoft.sigar:sigar"
    )
}

def installLibFromArtifact(String coords, String targetDir) throws IOException {
    File artifact = Resolver.resolveAbsoluteFile(coords)
    File target = new File(artifact.parentFile, targetDir);

//if the directory exists, assume it was properly unzipped
    if (!target.exists())
        Zip.unzip(artifact, target);

    sorcer.util.LibraryPathHelper.getLibraryPath().add(target.getPath());
}
