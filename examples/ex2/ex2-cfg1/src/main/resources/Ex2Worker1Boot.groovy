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


deployment(name: 'ex2-worker-1') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'ex2-rdl', 'org.sorcersoft.sorcer:ex2-dl:pom:' + getSorcerVersion()
    artifact id:'ex2-cfg', 'org.sorcersoft.sorcer:ex2-cfg1:'+getSorcerVersion()

    service(name:'Worker1') {
         interfaces {
             classes 'sorcer.ex2.provider.Worker'
             artifact ref:'ex2-rdl'
         }
         implementation(class: 'sorcer.ex2.provider.WorkerProvider') {
             artifact ref:'ex2-cfg'
         }
         configuration file: "classpath:worker1-prv.config"
         maintain 1
     }
}
