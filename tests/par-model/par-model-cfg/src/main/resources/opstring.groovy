/**
 * Deployment configuration for par-model-prv
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


deployment(name: 'par-model-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'api', 'org.sorcersoft.sorcer:sorcer-api:'+getSorcerVersion()
    artifact id:'par-model-cfg', 'org.sorcersoft.sorcer:par-model-cfg:'+getSorcerVersion()

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.service.Invocation', 'sorcer.service.Evaluation'
            artifact ref: 'api'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'par-model-cfg'
        }
        configuration file: 'classpath:parmodel-prv.config'
        maintain 1
    }
}
