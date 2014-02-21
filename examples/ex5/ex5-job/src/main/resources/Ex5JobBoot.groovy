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
 * Deployment configuration for ex5-arithmetic-job
 *
 * @author Rafał Krupiński
 */
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}

def String getCodebase() {
    return 'http://' + SorcerEnv.getLocalHost().getHostAddress() + ":9010"
}


deployment(name: 'ex5-arithmetic-job') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'ex5-api', 'org.sorcersoft.sorcer:ex5-api:' + getSorcerVersion()
    artifact id: 'ex5-cfg', 'org.sorcersoft.sorcer:ex5-job:' + getSorcerVersion()

    service(name: 'ex5-arithmetic-job') {
        interfaces {
            classes 'sorcer.service.Evaluation'
            artifact ref: 'ex5-api'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'ex5-cfg'
        }
        configuration file: "classpath:arithmetic-ter.config"
        maintain 1
    }
}
