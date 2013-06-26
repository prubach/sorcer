import org.rioproject.config.Constants
//import sorcer.core.SorcerEnv;
import java.util.logging.Level

/*String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
} */
/** Most basic example of deploying a default Blitz instance.
 * See Blitz documentation for configuration options.
 */
deployment(name:'BlitzSorcer') {

    /* Configuration for the discovery group that the services should join */
    groups "pol";//getInitialMemberGroups();

    // Blitz
    artifact id: 'blitz-dl', 'org.dancres.blitz:blitz-proxy:2.1.7'
    artifact id: 'blitz-impl', 'org.dancres.blitz:blitz-service:2.1.7'

    logging {
        logger 'com.travellinck', Level.INFO
    }


    /** Infrastructure service: Space */
    service(name: 'BlitzSpace') {
        interfaces {
            classes 'net.jini.space.JavaSpace05'
            artifact ref: 'blitz-dl'
        }

        implementation(class: 'org.dancres.blitz.remote.BlitzServiceImpl') {
            artifact ref: 'blitz-impl'
        }


        // Sample configuration for a basic, untuned, default instance. See Blitz
        // sample configs for details
        //configuration "file:src/test/conf/blitz.config"
        //configuration file: "${SorcerEnv.getHomeDir()}/configs/blitz/configs/blitz.config"
        configuration file: "/pol/sos/configs/blitz/configs/blitz.groovy"
        //configuration "file:src/test/conf/BlitzConfig-Persistent.groovy"

        maintain 1
    }
}
