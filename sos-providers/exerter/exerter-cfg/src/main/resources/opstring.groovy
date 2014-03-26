/**
 * Deployment configuration for exerter-prv
 *
 * @author Pawel Rubach
 */

import sorcer.core.SorcerConstants
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}


deployment(name: 'exerter-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    def exerter = [
            'impl': 'org.sorcersoft.sorcer:exerter-cfg:' + SorcerConstants.SORCER_VERSION,
            'dl'  : 'org.sorcersoft.sorcer:default-codebase:pom:' + SorcerConstants.SORCER_VERSION
    ]

    service(name: "Exerter") {
        interfaces {
            classes 'sorcer.core.provider.ServiceTasker'
            artifact exerter.dl
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact exerter.impl
        }
        configuration file: 'classpath:exerter.config'
        maintain 1
    }
}
