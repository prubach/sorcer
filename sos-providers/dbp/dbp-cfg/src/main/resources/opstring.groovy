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


deployment(name: 'dbp-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'dbp-api', 'org.sorcersoft.sorcer:dbp-api:'+getSorcerVersion()
    artifact id:'dbp-cfg', 'org.sorcersoft.sorcer:dbp-cfg:'+getSorcerVersion()
//    artifact id:'sos-exertlet-sui', 'org.sorcersoft.sorcer:sos-exertlet-sui:'+getSorcerVersion()

    service(name: "DatabaseStorer") {
        interfaces {
            classes 'sorcer.core.provider.dbp.IDatabaseProvider'
            artifact ref: 'dbp-api'
        }
        implementation(class: 'sorcer.core.provider.ServiceProvider') {
            artifact ref: 'dbp-cfg'
        }
        configuration file: 'classpath:dbp.config'
        maintain 1
    }
}
