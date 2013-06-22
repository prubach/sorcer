
/**
 * Deployment configuration for the minimum IGrid
 *
 * @author Dennis Reedy
 */
import org.rioproject.config.Constants
import org.rioproject.config.Constants

def getNameSuffix() {
    return System.getProperty("user.name").substring(0, 3).toUpperCase();
}

def appendJars(def dlJars) {
    def commonDLJars = ["sorcer-prv-dl.jar", "jsk-dl.jar", "rio-api.jar", "serviceui.jar", "jmx-lookup.jar"]
    dlJars.addAll(commonDLJars)
    return dlJars as String[]
}

def getSorcerHome() {
    String sorcerHome = System.getProperty("SORCER_HOME", System.getenv("SORCER_HOME"))
    return sorcerHome
}

/*def getActualSpaceName() {
	return SorcerEnv.getActualSpaceName();
} */

def String getCodebase() {
    return 'http://'+InetAddress.getLocalHost().getHostAddress()+":9010"
}

//deployment(name: "Sorcer iGrid") {
deployment(name: 'Sorcer') { //getActualSpaceName()) {
    groups System.getProperty("sorcer.groups", System.getProperty('user.name'))

    codebase getCodebase()

    artifact id:'mahalo', 'org.apache.river:mahalo:2.2.1'
    artifact id:'mahalo-dl', 'org.apache.river:mahalo-dl:2.2.1'

    artifact id:'blitz', "org.dancres.blitz:blitz:2.1.7"
    artifact id:'blitz-dl', "org.dancres.blitz:blitz-dl:2.1.7"
    artifact id:'blitzui', "org.dancres.blitz:blitzui:2.1.7"
    artifact id:'je', "com.sleepycat:je:4.1.21"
    artifact id:'serviceui', "net.jini.lookup:serviceui:2.2.1"
    artifact id:'jsk-platform', "net.jini:jsk-platform:2.2.1"
    artifact id:'outrigger-dl', "org.apache.river:outrigger-dl:2.2.1"

    artifact id:'commons-prv', "org.sorcersoft.sorcer:commons-prv:1.0-M2-SNAPSHOT"
    artifact id:'jobber-prv', "org.sorcersoft.sorcer:jobber-prv:1.0-M2-SNAPSHOT"

    artifact id:'sos-platform', "org.sorcersoft.sorcer:sos-platform:1.0-M2-SNAPSHOT"
    artifact id:'sos-exertlet-sui', "org.sorcersoft.sorcer:sos-exertlet-sui:1.0-M2-SNAPSHOT"




    service(name:'Mahalo') {
        interfaces {
            classes 'net.jini.core.transaction.server.TransactionManager'
            artifact ref:'mahalo-dl'
        }
        implementation(class: 'com.sun.jini.mahalo.TransientMahaloImpl') {
            artifact ref:'mahalo'
        }
        configuration file: "${getSorcerHome()}/configs/jini/configs/mahalo.config"
        maintain 1
    }

 /*   service(name: "Blitz Space") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            artifact ref:'blitz-dl', 'blitzui'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref:'blitz', 'blitzui','je','serviceui','jsk-platform','outrigger-dl'
        }
        configuration file: "${getSorcerHome()}/configs/blitz/configs/blitz.config"
        maintain 1
    }
   */
    service(name: "Jobber") {
        interfaces {
            classes 'sorcer.service.Jobber'
            artifact ref:'sos-platform', 'sos-exertlet-sui', 'serviceui'
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            artifact ref:'commons-prv', 'jobber-prv'
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/jobber.config"
        maintain 1
    }

/*    service(name: "Spacer") {
        interfaces {
            classes 'sorcer.service.Spacer'
            resources appendJars(["spacer-dl.jar"])
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            resources "spacer.jar", "sorcer-prv.jar", "monitor-api.jar"
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/spacer.config"
        maintain 1
    }

    service(name: "Cataloger") {
        interfaces {
            classes 'sorcer.core.Cataloger'
            resources appendJars(["cataloger-dl.jar", "exertlet-ui.jar",])
        }
        implementation(class: 'sorcer.core.provider.cataloger.ServiceCataloger') {
            resources "cataloger.jar", "sorcer-prv.jar"
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/cataloger.config"
        maintain 1
    }

    service(name: "Logger") {
        interfaces {
            classes 'sorcer.core.provider.logger.RemoteLogger'
            resources appendJars(["logger-dl.jar"])
        }
        implementation(class: 'sorcer.core.provider.logger.RemoteLoggerManager') {
            resources "logger.jar", "sorcer-prv.jar"
        }
        configuration file: "${getIGridHome()}/configs/sos-providers/logger.config"
        maintain 1
    }

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.core.provider.exertmonitor.MonitoringManagement'
            resources appendJars(["exertmonitor-dl.jar", "exertlet-ui.jar"])
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            resources 'exertmonitor.jar', "sorcer-prv.jar"
        }
        configuration file: "${getSorcerHome()}/configs/sos-providers/exertmonitor.config"
        maintain 1
    }*/
}
