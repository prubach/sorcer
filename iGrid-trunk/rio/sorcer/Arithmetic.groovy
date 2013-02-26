
/**
 * Deployment configuration for the minimum IGrid
 *
 * @author Dennis Reedy
 */
import org.rioproject.config.Constants

def getNameSuffix() {
    return System.getProperty("user.name").substring(0, 3).toUpperCase();
}

def appendJars(def dlJars) {
    def commonDLJars = ["sorcer-prv-dl.jar", "jsk-dl.jar", "rio-api.jar", "serviceui.jar", "jmx-lookup.jar"]
    dlJars.addAll(commonDLJars)
    return dlJars as String[]
}

def getIGridHome() {
    String iGridHome = System.getProperty("IGRID_HOME", System.getenv("IGRID_HOME"))
    if(iGridHome==null) {
        iGridHome = "${System.getProperty("user.home")}/projects/sorcer/workspace/iGrid-Rio"
    }
    return iGridHome
}

def String getCodebase() {
    return 'http://'+InetAddress.getLocalHost().getHostAddress()+":9010"
}

deployment(name: "Arithmetic") {
    groups System.getProperty(Constants.GROUPS_PROPERTY_NAME, System.getProperty('user.name'))

    codebase getCodebase()

    include 'MinimalIGridBoot.groovy'

    ['Adder', 'Multiplier', 'Subtractor', 'Divider'].each { s ->
        service(name: s) {
            interfaces {
                classes "sorcer.arithmetic.provider.$s"
                resources appendJars(["arithmetic-dl.jar", "provider-ui.jar", "exertlet-ui.jar"])
            }
            implementation(class: 'sorcer.core.provider.ServiceProvider') {
                resources "arithmetic-beans.jar", "sorcer-prv.jar", "sorcer-modeling-lib.jar"
            }
            configuration file: "${getIGridHome()}/modules/examples/ex6/configs/${s.toLowerCase()}-prv.config"
            maintain 1
        }
    }
}
