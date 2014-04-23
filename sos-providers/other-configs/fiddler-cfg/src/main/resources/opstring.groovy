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


deployment(name: 'fiddler-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'fiddler-dl', 'com.sorcersoft.river:fiddler-dl:3.0-M1'
    artifact id: 'fiddler-cfg', "org.sorcersoft.sorcer:fiddler-cfg:" + getSorcerVersion()

    service(name: 'Fiddler') {
        interfaces {
            classes 'com.sun.jini.fiddler.Fiddler'
            artifact ref: 'fiddler-dl'
        }
        implementation(class: 'com.sun.jini.fiddler.TransientFiddlerImpl') {
            artifact ref: 'fiddler-cfg'
        }
        configuration file: 'classpath:fiddler.config'
        maintain 1
    }
}
