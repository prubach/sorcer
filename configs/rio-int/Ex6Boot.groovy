/**
 * Deployment configuration for ex6-prv
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

def String getCodebase() {
    return 'http://'+SorcerEnv.getLocalHost().getHostAddress()+":9010"
}


deployment(name: 'ex6-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'ex6-api', 'org.sorcersoft.sorcer:ex6-api:1.0-M3-SNAPSHOT'
    artifact id:'ex6-prv', 'org.sorcersoft.sorcer:ex6-prv:1.0-M3-SNAPSHOT'

    service(name:'ex6-prv') {
         interfaces {
             classes 'sorcer.arithmetic.provider.Arithmetic'
             artifact ref:'ex6-api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex6-prv'
         }
         configuration file: "${getSorcerHome()}/examples/ex6/ex6-prv/src/main/resources/config/beans-prv.config"
         maintain 1
     }
}
