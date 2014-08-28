/**
 * Deployment configuration for ex5-prv
 *
 * @author Pawel Rubach
 */
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def getSorcerHome() {
    return sorcerHome = SorcerEnv.getHomeDir();
}

def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}


deployment(name: 'ju-arithmetic-context') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'api', 'org.sorcersoft.sorcer:ju-arithmetic-dl:pom:'+getSorcerVersion()
    artifact id: 'cfg', 'org.sorcersoft.sorcer:ju-arithmetic-cfg-ctx:'+getSorcerVersion()

    service(name:'Arithmetic') {
         interfaces {
             classes 'sorcer.service.Contexter'
             artifact ref:'api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'cfg'
         }
         configuration file: "classpath:contexter-prv.config"
         maintain 1
     }
}
