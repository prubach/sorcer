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


deployment(name: 'ex5-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'api', 'org.sorcersoft.sorcer:ju-arithmetic-api:'+getSorcerVersion()
    artifact id: 'cfg', 'org.sorcersoft.sorcer:ju-arithmetic-cfg-all:'+getSorcerVersion()

    service(name:'Arithmetic') {
         interfaces {
             classes 'junit.sorcer.core.provider.Arithmetic'
             artifact ref:'api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'cfg'
         }
         configuration file: "classpath:arithmetic-prv.config"
         maintain 1
     }
}
