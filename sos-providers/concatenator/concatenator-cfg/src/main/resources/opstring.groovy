/**
 * Deployment configuration for concatenator-prv
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


deployment(name: 'concatenator-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    //artifact id:'concatenator-api', 'org.sorcersoft.sorcer:concatenator-api:'+getSorcerVersion()
    artifact id:'concatenator-cfg', 'org.sorcersoft.sorcer:concatenator-cfg:'+getSorcerVersion()

    service(name: "Concatenator") {
        interfaces {
            classes 'sorcer.core.provider.Concatenator'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceConcatenator') {
            artifact ref: 'concatenator-cfg'
        }
        configuration file: 'classpath:concatenator.config'
        maintain 1
    }
}
