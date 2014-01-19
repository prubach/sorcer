/**
 * Deployment configuration for exerter-prv
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


deployment(name: 'exerter-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'exerter-api', 'org.sorcersoft.sorcer:exerter-api:'+getSorcerVersion()
    artifact id:'exerter-cfg', 'org.sorcersoft.sorcer:exerter-cfg:'+getSorcerVersion()

    service(name: "Exerter") {
        interfaces {
            classes 'sorcer.core.provider.ServiceTasker'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'exerter-cfg'
        }
        configuration file: 'classpath:exerter.config'
        maintain 1
    }
}
