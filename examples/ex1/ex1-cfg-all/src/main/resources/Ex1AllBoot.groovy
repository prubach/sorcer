/**
 * Deployment configuration for ex1-bean
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


deployment(name: 'ex1-all') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'ex1-rdl', 'org.sorcersoft.sorcer:ex1-dl:pom:'+getSorcerVersion()
    artifact id:'ex1-prv', 'org.sorcersoft.sorcer:ex1-cfg-all:'+getSorcerVersion()

    service(name:'XYZ') {
         interfaces {
             classes 'sorcer.ex1.WhoIsIt'
             artifact ref:'ex1-rdl'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'ex1-prv'
         }
         configuration file: "classpath:whoIsIt2-prv.config"
         maintain 1
     }
     service(name:'ABC') {
        interfaces {
            classes 'sorcer.ex1.WhoIsIt'
            artifact ref:'ex1-rdl'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref:'ex1-prv'
        }
        configuration file: "classpath:whoIsIt1-prv.config"
        maintain 1
    }
}
