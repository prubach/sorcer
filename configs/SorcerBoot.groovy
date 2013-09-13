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
    return sorcerHome = SorcerEnv.getHomeDir();
}

def fs() {
    return File.separator;
}


def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}


def String getCodebase() {
    return 'http://'+SorcerEnv.getLocalHost().getHostAddress()+":9010"
}

deployment(name: 'Sorcer') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'mahalo', 'org.apache.river:mahalo:2.2.1'
    artifact id:'mahalo-dl', 'org.apache.river:mahalo-dl:2.2.1'
    artifact id:'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:"+getSorcerVersion()
    artifact id:'cataloger-prv', "org.sorcersoft.sorcer:cataloger-prv:"+getSorcerVersion()
    artifact id:'cataloger-sui', "org.sorcersoft.sorcer:cataloger-sui:"+getSorcerVersion()
    artifact id:'jobber-prv', "org.sorcersoft.sorcer:jobber-prv:"+getSorcerVersion()
    artifact id:'spacer-prv', "org.sorcersoft.sorcer:spacer-prv:"+getSorcerVersion()
    artifact id:'logger-prv', "org.sorcersoft.sorcer:logger-prv:"+getSorcerVersion()
    artifact id:'logger-sui', "org.sorcersoft.sorcer:logger-sui:"+getSorcerVersion()
    artifact id:'dbp-prv', "org.sorcersoft.sorcer:dbp-prv:"+getSorcerVersion()
    artifact id:'exertmonitor-prv', "org.sorcersoft.sorcer:exertmonitor-prv:"+getSorcerVersion()
    artifact id:'commons-prv', "org.sorcersoft.sorcer:commons-prv:"+getSorcerVersion()

    artifact id: 'blitz-dl', 'org.sorcersoft.blitz:blitz-proxy:2.2.0'
    artifact id: 'blitz-impl', 'org.sorcersoft.blitz:blitz-service:2.2.0'

    service(name:'Mahalo') {
         interfaces {
             classes 'net.jini.core.transaction.server.TransactionManager'
             artifact ref:'mahalo-dl'
         }
         implementation(class: 'com.sun.jini.mahalo.TransientMahaloImpl') {
             artifact ref:'mahalo'
         }
         configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}jini${fs()}configs${fs()}mahalo.config"
         maintain 1
     }

    service(name: "BlitzSpace") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            artifact ref:'blitz-dl'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref:'blitz-impl'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}blitz${fs()}configs${fs()}blitz.config"
        maintain 1
    }

    service(name: "Jobber") {
        interfaces {
            classes 'sorcer.core.provider.Jobber'
            artifact ref:'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            artifact ref: 'jobber-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}jobber.config"
        maintain 1
    }

    service(name: "Spacer") {
        interfaces {
            classes 'sorcer.core.provider.Spacer'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            artifact ref: 'spacer-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}spacer.config"
        maintain 1
    }

      service(name: "Cataloger") {
        interfaces {
            classes 'sorcer.core.Cataloger'
            artifact ref: 'cataloger-sui'
        }
        implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
            artifact ref: 'cataloger-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}cataloger.config"
        maintain 1
    }

    service(name: "Logger") {
        interfaces {
            classes 'sorcer.core.RemoteLogger'
            artifact ref: 'logger-sui'
        }
        implementation(class: 'sorcer.core.provider.logger.RemoteLoggerManager') {
            artifact ref: 'logger-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}logger.config"
        maintain 1
    }

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.core.monitor.MonitoringManagement'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact ref: 'exertmonitor-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}exertmonitor.config"
        maintain 1
    }

    service(name: "DatabaseStorer") {
        interfaces {
            classes 'sorcer.core.provider.DatabaseStorer'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.dbp.DatabaseProvider') {
            artifact ref: 'dbp-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}dbp.config"
        maintain 1
    }

    service(name: "Exerter") {
        interfaces {
            classes 'sorcer.core.provider.ServiceTasker'
            artifact ref: 'sos-exertlet-sui'
        }
        implementation(class: 'sorcer.core.provider.ServiceTasker') {
            artifact ref: 'commons-prv'
        }
        configuration file: ""+getSorcerHome()+"${fs()}configs${fs()}sos-providers${fs()}exerter.config"
        maintain 1
    }
}
