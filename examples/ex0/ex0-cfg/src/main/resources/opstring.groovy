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
    return SorcerEnv.getWebsterUrl();
}


deployment(name: 'ex0-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'ex0-dl', 'org.sorcersoft.sorcer:ex0-dl:pom:' + getSorcerVersion()
    artifact id:'ex0-cfg', 'org.sorcersoft.sorcer:ex0-cfg:'+getSorcerVersion()

    service(name:'HelloWorld') {
         interfaces {
             classes 'sorcer.ex0.HelloWorld'
             artifact ref:'ex0-dl'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex0-cfg'
         }
         //configuration file: 'classpath:config.groovy'
         configuration file: 'classpath:service.config'
         maintain 1
     }
}
