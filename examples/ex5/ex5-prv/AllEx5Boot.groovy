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
    return 'http://'+SorcerEnv.getLocalHost().getHostAddress()+":9010"
}


deployment(name: 'ex5-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'ex5-api', 'org.sorcersoft.sorcer:ex5-api:'+getSorcerVersion()
    artifact id:'ex5-prv', 'org.sorcersoft.sorcer:ex5-prv:'+getSorcerVersion()

    service(name:'ex5-prv') {
         interfaces {
             classes 'sorcer.ex5.provider.Arithmetic'
             artifact ref:'ex5-api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex5-prv'
         }
         configuration file: "${getSorcerHome()}/examples/ex5/ex5-prv/src/main/resources/config/arithmetic-all-beans.config"
         maintain 1
     }
}
