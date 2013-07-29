/**
 * Deployment configuration for ex0-prv
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


deployment(name: 'ex0-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'ex0-api', 'org.sorcersoft.sorcer:ex0-api:1.0-M3-SNAPSHOT'
    artifact id:'ex0-prv', 'org.sorcersoft.sorcer:ex0-prv:1.0-M3-SNAPSHOT'

    service(name:'ex0-prv') {
         interfaces {
             classes 'sorcer.ex0.HelloWorld'
             artifact ref:'ex0-api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex0-prv'
         }
         configuration file: "${getSorcerHome()}/examples/ex0/ex0-prv/src/main/resources/config/HelloWorld.config"
         maintain 1
     }
}
