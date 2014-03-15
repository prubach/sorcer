/**
 * Deployment configuration for spacer-prv
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


deployment(name: 'spacer-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'spacer-api', 'org.sorcersoft.sorcer:spacer-api:'+getSorcerVersion()
    artifact id:'spacer-cfg', 'org.sorcersoft.sorcer:spacer-cfg:'+getSorcerVersion()
    artifact id: 'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:" + getSorcerVersion()

    service(name: "Spacer") {
        interfaces {
            classes 'sorcer.core.provider.Spacer'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            artifact ref: 'spacer-cfg'
        }
        configuration file: 'classpath:spacer.config'
        maintain 1
    }
}
