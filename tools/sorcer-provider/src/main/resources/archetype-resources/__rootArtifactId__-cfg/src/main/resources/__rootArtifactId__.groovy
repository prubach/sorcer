/**
 * Deployment configuration for ${rootArtifactId}-prv
 *
 * @author Pawel Rubach
 */
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}

deployment(name: '${rootArtifactId}-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: '${rootArtifactId}-dl', '${groupId}:${rootArtifactId}-dl:pom:${version}'
    artifact id: '${rootArtifactId}-cfg', '${groupId}:${rootArtifactId}-cfg:${version}'

    service(name: '${rootArtifactId}') {
        interfaces {
            classes '${package}.${providerInterface}'
            artifact ref: '${rootArtifactId}-dl'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: '${rootArtifactId}-cfg'
        }
        configuration file: 'classpath:${rootArtifactId}.config'
        maintain 1
    }
}
