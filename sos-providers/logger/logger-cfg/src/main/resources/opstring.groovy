/**
 * Deployment configuration for logger-prv
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


deployment(name: 'logger-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'logger-sui', 'org.sorcersoft.sorcer:logger-sui:'+getSorcerVersion()
    artifact id:'logger-cfg', 'org.sorcersoft.sorcer:logger-cfg:'+getSorcerVersion()

    service(name: "Logger") {
        interfaces {
            classes 'sorcer.core.RemoteLogger'
            artifact ref: 'logger-sui'
        }
        implementation(class: 'sorcer.core.provider.logger.RemoteLoggerManager') {
            artifact ref: 'logger-cfg'
        }
        configuration file: 'classpath:logger.config'
        maintain 1
    }
}
