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

/**
 * Deployment configuration for the minimum Sorcer
 *
 * @author Pawel Rubach based on Dennis Reedy's example
 */
import sorcer.core.SorcerEnv;


String[] getInitialMemberGroups() {
    return SorcerEnv.lookupGroups as String[];
}

def getSorcerHome() {
    return SorcerEnv.homeDir.path;
}

deployment(name: 'Sorcer') {
    groups getInitialMemberGroups();

    codebase 'http://' + SorcerEnv.localHost.hostAddress + ":9010/"

    artifact id: 'monitor-service', 'org.rioproject.monitor:monitor-service:5.0-M4-S4'
    artifact id: 'monitor-proxy',   'org.rioproject.monitor:monitor-proxy:5.0-M4-S4'

    artifact id: 'cybernode-service', 'org.rioproject.cybernode:cybernode-service:5.0-M4-S4'
    artifact id: 'cybernode-proxy',   'org.rioproject.cybernode:cybernode-proxy:5.0-M4-S4'
    artifact id: 'cybernode-api',     'org.rioproject.cybernode:cybernode-api:5.0-M4-S4'


    service(name: 'Cybernode') {
        interfaces {
            classes 'org.rioproject.cybernode.Cybernode'
            artifact ref: 'cybernode-proxy'
        }
        implementation(class: 'org.rioproject.cybernode.service.CybernodeImpl') {
            artifact ref: 'cybernode-service'
        }
        configuration file: getSorcerHome() + "/configs/rio/cybernode.groovy"
        maintain 1
    }

    service(name: 'Monitor') {
        interfaces {
            classes 'org.rioproject.monitor.ProvisionMonitor'
            artifact ref: 'monitor-proxy'
        }
        implementation(class: 'org.rioproject.monitor.service.ProvisionMonitorImpl') {
            artifact ref: 'monitor-service'
        }
        configuration file: getSorcerHome() + "/configs/rio/monitor.groovy"
        maintain 1
    }

}
