/**
 * Deployment configuration for concatenator-prv
 *
 * @author Pawel Rubach
 */
import sorcer.core.SorcerEnv;

import static sorcer.core.SorcerConstants.SORCER_VERSION

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}

deployment(name: 'concatenator-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    def concatenator = [
            impl: 'org.sorcersoft.sorcer:concatenator-cfg:' + SORCER_VERSION,
            dl  : "org.sorcersoft.sorcer:default-codebase:" + SORCER_VERSION
    ]

    service(name: "Concatenator") {
        interfaces {
            classes 'sorcer.core.provider.Concatenator'
            artifact concatenator.dl
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceConcatenator') {
            artifact concatenator.impl
        }
        configuration file: 'classpath:concatenator.config'
        maintain 1
    }
}
