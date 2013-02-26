
/**
 * Deployment configuration for the minimum IGrid
 *
 * @author Dennis Reedy
 */
import org.rioproject.config.Constants

def getNameSuffix() {
    //return System.getProperty("user.name").substring(0, 3).toUpperCase();
	return "MWS";
}

def appendJars(def dlJars) {
    def commonDLJars = ["sorcer-prv-dl.jar", "jsk-dl.jar", "rio-api.jar", "serviceui.jar", "jmx-lookup.jar"]
    dlJars.addAll(commonDLJars)
    return dlJars as String[]
}

def getIGridHome() {
    String iGridHome = System.getProperty("iGrid.home", System.getenv("IGRID_HOME"))
    if(iGridHome==null) {
        iGridHome = "${System.getProperty("user.home")}/workspace/iGrid-trunk"
    }
    return iGridHome
}

def String getCodebase() {
    return 'http://'+InetAddress.getLocalHost().getHostAddress()+":9010"
}

deployment(name: "Sorcer IGrid") {
    groups System.getProperty("sorcer.groupw", System.getProperty('user.name'))

    codebase getCodebase()

    service(name:'Mahalo') {
        interfaces {
            classes 'net.jini.core.transaction.server.TransactionManager'
            resources 'mahalo-dl.jar', 'jsk-dl.jar'
        }
        implementation(class: 'com.sun.jini.mahalo.TransientMahaloImpl') {
            resources "mahalo.jar", "sorcer-prv.jar"
        }
        configuration file: "${getIGridHome()}/bin/jini/configs/mahalo.config"
        maintain 1
    }

    service(name: "Exert Space") {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            resources 'blitz-dl.jar', 'blitzui.jar'
        }
        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            resources "blitz.jar", "blitzui.jar", "backport-util-concurrent60.jar",
                      "serviceui.jar", "outrigger-dl.jar", "sorcer-prv.jar"
        }
        configuration file: "${getIGridHome()}/bin/blitz/configs/blitz.config"
        maintain 1
    }

    service(name: "Jobber") {
        interfaces {
            classes 'sorcer.service.Jobber'
            resources appendJars(["jobber-dl.jar", "exertlet-ui.jar"])
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceJobber') {
            resources "jobber.jar", "sorcer-prv.jar", "monitor-api.jar"
        }
        configuration file: "${getIGridHome()}/bin/sorcer/jobber/configs/jobber-prv.config"
        maintain 1
    }

    service(name: "Spacer") {
        interfaces {
            classes 'sorcer.service.Spacer'
            resources appendJars(["spacer-dl.jar"])
        }
        implementation(class: 'sorcer.core.provider.jobber.ServiceSpacer') {
            resources "spacer.jar", "sorcer-prv.jar", "monitor-api.jar"
        }
        configuration file: "${getIGridHome()}/bin/sorcer/jobber/configs/spacer-prv.config"
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
        configuration file: "${getIGridHome()}/bin/sorcer/cataloger/configs/cataloger-prv.config"
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
        configuration file: "${getIGridHome()}/bin/sorcer/logger/configs/logger-prv.config"
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
        configuration file: "${getIGridHome()}/bin/sorcer/exertmonitor/configs/exertmonitor-prv.config"
        maintain 1
    }
}
