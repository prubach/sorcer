/**
 * Deployment configuration for dbp-prv
 *
 * @author Pawel Rubach
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
    return SorcerEnv.getWebsterUrl();
}


deployment(name: 'mercury-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'mercury-dl', 'com.sorcersoft.river:mercury-dl:3.0-M1'
    artifact id: 'mercury-cfg', "org.sorcersoft.sorcer:mercury-cfg:" + getSorcerVersion()

    service(name: 'Mercury') {
        interfaces {
            classes 'com.sun.jini.mercury.MailboxBackEnd'
            artifact ref: 'mercury-dl'
        }
        implementation(class: 'com.sun.jini.mercury.TransientMercuryImpl') {
            artifact ref: 'mercury-cfg'
        }
        configuration file: 'classpath:mercury.config'
        maintain 1
    }
}
