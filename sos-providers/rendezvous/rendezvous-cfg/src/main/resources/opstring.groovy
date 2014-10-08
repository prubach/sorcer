/**
 * Deployment configuration for rendezvous-prv
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

deployment(name: 'rendezvous-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    def rendezvous = [
            impl: 'org.sorcersoft.sorcer:rendezvous-cfg:' + SORCER_VERSION,
            dl  : "org.sorcersoft.sorcer:default-codebase:pom:" + SORCER_VERSION
    ]

    service(name: "Rendezvous") {
        interfaces {
            classes 'sorcer.core.provider.Concatenator', 'sorcer.core.provider.Jobber', 'sorcer.core.provider.Spacer'
            artifact rendezvous.dl
        }
        implementation(class: 'sorcer.core.provider.ServiceProvider') {
            artifact rendezvous.impl
        }
        configuration file: 'classpath:rendezvous-prv.config'
        maintain 1
    }
}
