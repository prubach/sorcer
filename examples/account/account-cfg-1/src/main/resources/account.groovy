/**
 * Deployment configuration for account-prv
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

deployment(name: 'account1-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'account-api', 'org.sorcersoft.sorcer:account-api:' + SorcerEnv.getSorcerVersion()
    artifact id: 'account-cfg', 'org.sorcersoft.sorcer:account-cfg-1:' + SorcerEnv.getSorcerVersion()

    service(name: 'account1-prv') {
        interfaces {
            classes 'sorcer.account.provider.Account', 'sorcer.account.provider.ServiceAccount'
            artifact ref: 'account-api'
        }
        implementation(class: 'sorcer.account.provider.AccountProvider') {
            artifact ref: 'account-cfg'
        }
        //configuration file: 'classpath:config1.groovy'
        configuration file: 'classpath:account.config'
        maintain 1
    }
}
