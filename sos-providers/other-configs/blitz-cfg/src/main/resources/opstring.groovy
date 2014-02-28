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


deployment(name: 'blitz-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'blitz-dl', 'org.sorcersoft.blitz:blitz-proxy:2.2.1-1'
    artifact id: 'blitz-cfg', "org.sorcersoft.sorcer:blitz-cfg:" + getSorcerVersion()

    service(name: "BlitzSpace") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            artifact ref: 'blitz-dl'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref: 'blitz-cfg'
        }
        configuration file: 'classpath:blitz.config'
        maintain 1
    }
}
