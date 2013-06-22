/*
 * This configuration is used to start a Webster serving up Sorcer resources
 */

import org.rioproject.config.Component

import com.sun.jini.start.ServiceDescriptor
import net.jini.config.ConfigurationException
import org.rioproject.boot.ServiceDescriptorUtil

@Component('com.sun.jini.start')
class StartSorcerWebsterConfig {

    def checkAppendPathSeparator(String dir) {
        if(!dir.endsWith(File.separator))
            dir = dir+File.separator
        return dir
    }

    String[] getWebsterRoots(String sorcerHome, String rioHome, String repo) {
        sorcerHome = checkAppendPathSeparator(sorcerHome)
        rioHome = checkAppendPathSeparator(rioHome)
        def websterRoots = [
                sorcerHome + "lib", ";" ,
                rioHome + "lib-dl", ";" ,
                repo
                ]
        return websterRoots as String[]
    }

    def getValue(String propertyName) {
        String value = System.getProperty(propertyName, System.getenv(propertyName))
        if(value==null) {
            throw new ConfigurationException("${propertyName} must be set either as an environment variable or as a system property")
        }
        return value
    }

    ServiceDescriptor[] getServiceDescriptors() {
        String rioHome = getValue("RIO_HOME")
        String sorcerHome = getValue("SORCER_HOME")
        String repo = getValue("user.home") + "/.m2/repository";

        def websterRoots = getWebsterRoots(sorcerHome, rioHome, repo)

        String policyFile = sorcerHome+'/configs/sorcer.policy'

        def serviceDescriptors = [
            ServiceDescriptorUtil.getWebster(policyFile, '9010', websterRoots as String[]),
        ]

        return (ServiceDescriptor[])serviceDescriptors
    }

}
