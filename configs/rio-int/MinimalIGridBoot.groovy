/**
 * Deployment configuration for the minimum IGrid
 *
 * @author Dennis Reedy
 */
import org.rioproject.config.Constants
import org.rioproject.config.Constants
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;


String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def getSorcerHome() {
    return sorcerHome = SorcerEnv.getHomeDir();
}

def getActualSpaceName() {
	return SorcerEnv.getActualSpaceName();
}

def String getCodebase() {
    return 'http://'+InetAddress.getLocalHost().getHostAddress()+":9010"
}

//deployment(name: "Sorcer iGrid") {
deployment(name: 'Sorcer') { //getActualSpaceName()) {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id:'mahalo', 'org.apache.river:mahalo:2.2.1'
    artifact id:'mahalo-dl', 'org.apache.river:mahalo-dl:2.2.1'

    artifact id:'blitz', "org.dancres.blitz:blitz:2.1.7"
    artifact id:'blitz-dl', "org.dancres.blitz:blitz-dl:2.1.7"
    artifact id:'blitzui', "org.dancres.blitz:blitzui:2.1.7"
    artifact id:'sleepycat', "com.sleepycat:je:4.1.21"
    artifact id:'serviceui', "net.jini.lookup:serviceui:2.2.1"
    artifact id:'jsk-platform', "net.jini:jsk-platform:2.2.1"
    artifact id:'jsk-lib', "net.jini:jsk-lib:2.2.1"
    artifact id:'outrigger-dl', "org.apache.river:outrigger-dl:2.2.1"

    artifact id:'sos-platform', "org.sorcersoft.sorcer:sos-platform:1.0-M2-SNAPSHOT"
    artifact id:'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:1.0-M2-SNAPSHOT"

    artifact id:'commons-prv', "org.sorcersoft.sorcer:commons-prv:1.0-M2-SNAPSHOT"
    artifact id:'jobber-prv', "org.sorcersoft.sorcer:jobber-prv:1.0-M2-SNAPSHOT"
    artifact id:'spacer-prv', "org.sorcersoft.sorcer:jobber-prv:1.0-M2-SNAPSHOT"
    artifact id:'logger-prv', "org.sorcersoft.sorcer:logger-prv:1.0-M2-SNAPSHOT"
    artifact id:'logger-sui', "org.sorcersoft.sorcer:logger-sui:1.0-M2-SNAPSHOT"
    artifact id:'dbp-prv', "org.sorcersoft.sorcer:dbp-prv:1.0-M2-SNAPSHOT"

    resources id:'blitz-cdb', Resolver.resolveRelative("org.dancres.blitz:blitz-dl"), Resolver.resolveRelative("org.dancres.blitz:blitzui")


    /*service(name:'Mahalo') {
         interfaces {
             classes 'net.jini.core.transaction.server.TransactionManager'
             artifact ref:'mahalo-dl'
         }
         implementation(class: 'com.sun.jini.mahalo.TransientMahaloImpl') {
             artifact ref:'mahalo'
         }
         configuration file: "${getSorcerHome()}/configs/jini/configs/mahalo.config"
         maintain 1
     }*/

    service(name: "BlitzSpace") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            resources ref:'blitz-dl'
            artifact ref:'blitzui'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref:'blitz'
            artifact ref:'blitzui'
            artifact ref:'serviceui'
            artifact ref:'jsk-platform'
            artifact ref:'outrigger-dl'
            artifact ref:'sleepycat'
        }
        configuration file: "${getSorcerHome()}/configs/blitz/configs/blitz.config"
        maintain 1
    }

    /*service(name: "Jobber") {
        interfaces {
            classes 'sorcer.service.Jobber'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            artifact ref:'commons-prv', 'jobber-prv'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/jobber.config"
        maintain 1
    } */

  /*  service(name: "Spacer") {
        interfaces {
            classes 'sorcer.service.Spacer'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            artifact ref:'commons-prv', 'spacer-prv'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/spacer.config"
        maintain 1
    }

    service(name: "Cataloger") {
        interfaces {
            classes 'sorcer.core.Cataloger'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
            artifact ref:'commons-prv', 'cataloger-prv'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/cataloger.config"
        maintain 1
    }
    */
    /*service(name: "Logger") {
        interfaces {
            classes 'sorcer.core.provider.logger.RemoteLogger'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui', 'logger-sui'
        }
        implementation(class: 'sorcer.core.provider.logger.RemoteLoggerManager') {
            artifact ref:'commons-prv', 'logger-prv', 'logger-sui'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/logger.config"
        maintain 1
    }

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.core.provider.exertmonitor.MonitoringManagement'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact ref:'commons-prv', 'exertmonitor-prv', 'je'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/exertmonitor.config"
        maintain 1
    }
      */
/*    service(name: "DatabaseStorer") {
        interfaces {
            classes 'sorcer.core.provider.dbp.DatabaseProvider'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact ref:'commons-prv', 'dbp-prv', 'je'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/dbp.config"
        maintain 1
    }
 */
  /*  service(name: "Exerter") {
        interfaces {
            classes 'sorcer.core.provider.ServiceTasker'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact ref:'commons-prv'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/exerter.config"
        maintain 1
    }*/
}
