/**
 * Deployment configuration for __rootArtifactId__-prv
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


deployment(name: '${rootArtifactId}-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'${rootArtifactId}-api', '${groupId}:${rootArtifactId}-api:${version}'
    artifact id:'${rootArtifactId}-prv', '${groupId}:${rootArtifactId}-prv:${version}'

    service(name:'${rootArtifactId}-prv') {
         interfaces {
             classes '${package}.${providerInterface}'
             artifact ref:'${rootArtifactId}-api'
         }
         implementation(class: 'sorcer.core.provider.ServiceTasker') {
             artifact ref:'${rootArtifactId}-prv'
         }
         configuration file: getSorcerHome()+"/examples/${rootArtifactId}/${rootArtifactId}-prv/src/main/resources/config/${providerInterface}.config"
         maintain 1
     }
}
