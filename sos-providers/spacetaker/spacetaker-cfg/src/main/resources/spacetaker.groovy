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
 * Deployment configuration for spacetaker-prv
 *
 * @author Pawel Rubach
 */
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}

deployment(name: 'spacetaker-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'spacetaker-cfg', 'org.sorcersoft.sorcer:spacetaker-cfg:' + SorcerConstants.SORCER_VERSION

    service(name: 'spacetaker-prv') {
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'spacetaker-cfg'
        }
        configuration file: 'classpath:spacetaker.config'
        maintain 1
    }
}
