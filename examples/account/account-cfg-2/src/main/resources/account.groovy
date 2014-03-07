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

deployment(name: 'account2-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'account-api', 'org.sorcersoft.sorcer:account-api:' + SorcerEnv.getSorcerVersion()
    artifact id: 'account-cfg', 'org.sorcersoft.sorcer:account-cfg-2:' + SorcerEnv.getSorcerVersion()

    service(name: 'account2-prv') {
        interfaces {
            classes 'sorcer.account.provider.Account', 'sorcer.account.provider.ServiceAccount'
            artifact ref: 'account-api'
        }
        implementation(class: 'sorcer.account.provider.AccountProvider') {
            artifact ref: 'account-cfg'
        }
        configuration file: 'classpath:account.config'
        maintain 1
    }
}
