/**
 * Deployment configuration for jobber-prv
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


deployment(name: 'jobber-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'jobber-api', 'org.sorcersoft.sorcer:jobber-api:'+getSorcerVersion()
    artifact id:'jobber-cfg', 'org.sorcersoft.sorcer:jobber-cfg:'+getSorcerVersion()
    artifact id: 'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:" + getSorcerVersion()

    service(name: "Jobber") {
        interfaces {
            classes 'sorcer.core.provider.Jobber'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            artifact ref: 'jobber-cfg'
        }
        configuration file: 'classpath:jobber.config'
        maintain 1
    }
}
