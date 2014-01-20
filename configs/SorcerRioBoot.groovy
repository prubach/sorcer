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

    artifact id: 'mahalo', 'org.apache.river:mahalo:2.2.1'
    artifact id: 'mahalo-dl', 'org.apache.river:mahalo-dl:2.2.1'
    artifact id: 'fiddler', 'org.apache.river:fiddler:2.2.1'
    artifact id: 'fiddler-dl', 'org.apache.river:fiddler-dl:2.2.1'
    artifact id: 'norm', 'org.apache.river:norm:2.2.1'
    artifact id: 'norm-dl', 'org.apache.river:norm-dl:2.2.1'
    artifact id: 'mercury', 'org.apache.river:mercury:2.2.1'
    artifact id: 'mercury-dl', 'org.apache.river:mercury-dl:2.2.1'
    artifact id: 'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:" + getSorcerVersion()
    artifact id: 'cataloger-cfg', "org.sorcersoft.sorcer:cataloger-cfg:" + getSorcerVersion()
    artifact id: 'cataloger-sui', "org.sorcersoft.sorcer:cataloger-sui:" + getSorcerVersion()
    artifact id: 'jobber-cfg', "org.sorcersoft.sorcer:jobber-cfg:" + getSorcerVersion()
    artifact id: 'spacer-cfg', "org.sorcersoft.sorcer:spacer-cfg:" + getSorcerVersion()
    artifact id: 'logger-cfg', "org.sorcersoft.sorcer:logger-cfg:" + getSorcerVersion()
    artifact id: 'logger-sui', "org.sorcersoft.sorcer:logger-sui:" + getSorcerVersion()
    artifact id: 'dbp-cfg', "org.sorcersoft.sorcer:dbp-cfg:" + getSorcerVersion()
    artifact id: 'exertmonitor-cfg', "org.sorcersoft.sorcer:exertmonitor-cfg:" + getSorcerVersion()
    artifact id: 'commons-prv', "org.sorcersoft.sorcer:commons-prv:" + getSorcerVersion()
    artifact id: 'exerter-cfg', "org.sorcersoft.sorcer:exerter-cfg:" + getSorcerVersion()

    artifact id: 'blitz-cfg', "org.sorcersoft.sorcer:blitz-cfg:" + getSorcerVersion()
    artifact id: 'blitz-dl', 'org.sorcersoft.blitz:blitz-proxy:2.2.0'
    //artifact id: 'blitz-impl', 'org.sorcersoft.blitz:blitz-service:2.2.0'


    service(name: 'Mahalo') {
        interfaces {
            classes 'com.sun.jini.mahalo.TxnManager'
            artifact ref: 'mahalo-dl'
        }
        implementation(class: 'com.sun.jini.mahalo.TransientMahaloImpl') {
            artifact ref: 'mahalo'
        }
        configuration file: getSorcerHome() + "/configs/jini/configs/mahalo.config"
        maintain 1
    }
    service(name: 'Fiddler') {
        interfaces {
            classes 'com.sun.jini.fiddler.Fiddler'
            artifact ref: 'fiddler-dl'
        }
        implementation(class: 'com.sun.jini.fiddler.TransientFiddlerImpl') {
            artifact ref: 'fiddler'
        }
        configuration file: getSorcerHome() + "/configs/jini/configs/fiddler.config"
        maintain 1
    }
    service(name: 'Norm') {
        interfaces {
            classes 'com.sun.jini.norm.NormServer'
            artifact ref: 'norm-dl'
        }
        implementation(class: 'com.sun.jini.norm.TransientNormServerImpl') {
            artifact ref: 'norm'
        }
        configuration file: getSorcerHome() + "/configs/jini/configs/norm.config"
        maintain 1
    }

    service(name: 'Mercury') {
        interfaces {
            classes 'com.sun.jini.mercury.MailboxBackEnd'
            artifact ref: 'mercury-dl'
        }
        implementation(class: 'com.sun.jini.mercury.TransientMercuryImpl') {
            artifact ref: 'mercury'
        }
        configuration file: getSorcerHome() + "/configs/jini/configs/mercury.config"
        maintain 1
    }

    service(name: "BlitzSpace") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            artifact ref: 'blitz-dl'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref: 'blitz-cfg'
        }
        configuration file: 'classpath:blitz.config'
        maintain 1
    }

    service(name: "Jobber") {
        interfaces {
            classes 'sorcer.core.provider.Jobber'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            artifact ref: 'jobber-cfg'
        }
        configuration file: 'classpath:jobber.config'
        maintain 1
    }

    service(name: "Spacer") {
        interfaces {
            classes 'sorcer.core.provider.Spacer'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            artifact ref: 'spacer-cfg'
        }
        configuration file: 'classpath:spacer.config'
        maintain 1
    }

    service(name: "Cataloger") {
        interfaces {
            classes 'sorcer.core.provider.Cataloger'
            artifact ref: 'cataloger-sui'
        }
        implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
            artifact ref: 'cataloger-cfg'
        }
        configuration file: 'classpath:cataloger.config'
        maintain 1
    }

    service(name: "Logger") {
        interfaces {
            classes 'sorcer.core.RemoteLogger'
            artifact ref: 'logger-sui'
        }
        implementation(class: 'sorcer.core.provider.logger.RemoteLoggerManager') {
            artifact ref: 'logger-cfg'
        }
        configuration file: 'classpath:logger.config'
        maintain 1
    }

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.core.monitor.MonitoringManagement'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact ref: 'exertmonitor-cfg'
        }
        configuration file: 'classpath:exertmonitor.config'
        maintain 1
    }

    service(name: "DatabaseStorer") {
        interfaces {
            classes 'sorcer.service.DatabaseStorer'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.ServiceProvider') {
            artifact ref: 'dbp-cfg'
        }
        configuration file: 'classpath:dbp.config'
        maintain 1
    }

    service(name: "Exerter") {
        interfaces {
            classes 'sorcer.core.provider.ServiceTasker'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'exerter-cfg'
        }
        configuration file: 'classpath:exerter.config'
        maintain 1
    }
}
