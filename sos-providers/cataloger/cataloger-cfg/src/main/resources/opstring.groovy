/**
 * Deployment configuration for cataloger-prv
 *
 * @author Pawel Rubach
 */

import sorcer.core.SorcerEnv;
import static sorcer.core.SorcerConstants.SORCER_VERSION;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}

deployment(name: 'cataloger-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    def cataloger = [
            impl: 'org.sorcersoft.sorcer:cataloger-cfg:' + SORCER_VERSION,
            api : 'org.sorcersoft.sorcer:default-codebase:pom:' + SORCER_VERSION
    ]

    service(name: 'cataloger-prv') {
        interfaces {
            classes 'sorcer.core.provider.Cataloger'
            artifact cataloger.api
        }
        implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
            artifact cataloger.impl
        }
        configuration file: 'classpath:cataloger.config'
        maintain 1
    }
}
