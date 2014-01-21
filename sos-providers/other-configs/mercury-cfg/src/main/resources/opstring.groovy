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
    return 'http://'+SorcerEnv.getLocalHost().getHostAddress()+":9010"
}


deployment(name: 'mercury-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'mercury-dl', 'org.apache.river:mercury-dl:2.2.1'
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
