/**
 * Deployment configuration for cataloger-prv
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


deployment(name: 'cataloger-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'cataloger-api', 'org.sorcersoft.sorcer:cataloger-api:'+getSorcerVersion()
    artifact id:'cataloger-cfg', 'org.sorcersoft.sorcer:cataloger-cfg:'+getSorcerVersion()

    service(name:'cataloger-prv') {
         interfaces {
             classes 'sorcer.core.provider.Cataloger'
             artifact ref:'cataloger-api'
         }
         implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
             artifact ref:'cataloger-cfg'
         }
         configuration file: 'classpath:cataloger.config'
         maintain 1
     }
}
