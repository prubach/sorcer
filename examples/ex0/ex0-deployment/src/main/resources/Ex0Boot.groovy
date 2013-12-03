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

def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}

def String getCodebase() {
    return 'http://'+SorcerEnv.getLocalHost().getHostAddress()+":9010"
}


deployment(name: 'ex0-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'ex0-api', 'org.sorcersoft.sorcer:ex0-api:'+getSorcerVersion()
    artifact id:'ex0-prv', 'org.sorcersoft.sorcer:ex0-prv:'+getSorcerVersion()

    service(name:'ex0-prv') {
         interfaces {
             classes 'sorcer.ex0.HelloWorld'
             artifact ref:'ex0-api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex0-prv'
         }
         configuration file: "classpath:/HelloWorld.config"
         maintain 1
     }
}
