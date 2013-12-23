/**
 * Deployment configuration for the minimum Sorcer
 *
 * @author Pawel Rubach based on Dennis Reedy's example
 */
import sorcer.core.SorcerEnv;


String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def getSorcerHome() {
    return SorcerEnv.getHomeDir().path;
}

def fs() {
    return File.separator;
}


def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}


def String getCodebase() {
    return 'http://' + SorcerEnv.getLocalHost().getHostAddress() + ":9010"
}

deployment(name: 'Sorcer') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'webster-srv', 'org.sorcersoft.sorcer:sos-webster:' + getSorcerVersion()

    artifact id: 'reggie', 'org.apache.river:reggie:2.2.1'
    artifact id: 'reggie-dl', 'org.apache.river:reggie-dl:2.2.1'


    service(name: 'Webster') {
        implementation(class: 'sorcer.tools.webster.Webster') {
            artifact ref: 'webster-srv'
        }
        configuration file: getSorcerHome() + "/configs/webster/configs/webster-prv.config"
        maintain 1
    }

    service(name: 'Reggie') {
        interfaces {
            classes 'com.sun.jini.reggie.Registrar'
            artifact ref: 'reggie-dl'
        }
        implementation(class: 'com.sun.jini.reggie.TransientRegistrarImpl') {
            artifact ref: 'reggie'
        }
        configuration file: getSorcerHome() + "/configs/jini/configs/reggie.config"
        maintain 1
    }

}


include 'SorcerRioBoot.groovy'
