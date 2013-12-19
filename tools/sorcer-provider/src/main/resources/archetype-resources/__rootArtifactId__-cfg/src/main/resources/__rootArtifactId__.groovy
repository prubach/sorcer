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
    return 'http://' + SorcerEnv.getLocalHost().getHostAddress() + ":9010"
}

deployment(name: '${rootArtifactId}-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: '${rootArtifactId}-api', '${groupId}:${rootArtifactId}-api:${version}'
    artifact id: '${rootArtifactId}-cfg', '${groupId}:${rootArtifactId}-cfg:${version}'

    service(name: '${rootArtifactId}-prv') {
        interfaces {
            classes '${package}.${providerInterface}'
            artifact ref: '${rootArtifactId}-api'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: '${rootArtifactId}-cfg'
        }
        configuration file: 'classpath:${rootArtifactId}.config'
        maintain 1
    }
}
