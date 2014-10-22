/**
 * Deployment configuration for caller-prv
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

deployment(name: 'Caller') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'caller-dl', 'org.sorcersoft.sorcer:caller-dl:pom:1.1-SNAPSHOT'
    artifact id: 'caller-cfg', 'org.sorcersoft.sorcer:caller-cfg:1.1-SNAPSHOT'

    service(name: 'Caller') {
        interfaces {
            classes 'sorcer.caller.Caller'
            artifact ref: 'caller-dl'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'caller-cfg'
        }
        configuration file: 'classpath:caller.config'
        maintain 1
    }
}
