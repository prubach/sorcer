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


deployment(name: 'fiddler-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'fiddler-dl', 'org.apache.river:fiddler-dl:2.2.1'
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
