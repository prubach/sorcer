/**
 * Deployment configuration for the minimum Sorcer
 *
 * @author Pawel Rubach based on Dennis Reedy's example
 */
import sorcer.core.SorcerEnv;

def getSorcerHome() {
    return SorcerEnv.homeDir.path;
}

def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}

deployment(name: 'Sorcer') {
    groups SorcerEnv.getLookupGroups() as String[];

    artifact id: 'reggie', 'org.apache.river:reggie:2.2.2'
    artifact id: 'reggie-dl', 'org.apache.river:reggie-dl:2.2.2'

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
