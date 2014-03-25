/**
 * Deployment configuration for exertmonitor-prv
 *
 * @author Pawel Rubach
 */

import sorcer.core.SorcerConstants
import sorcer.core.SorcerEnv;

String[] getInitialMemberGroups() {
    def groups = SorcerEnv.getLookupGroups();
    return groups as String[]
}

def String getCodebase() {
    return SorcerEnv.getWebsterUrl();
}


deployment(name: 'exertmonitor-provider') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    def monitor = [
            'impl': 'org.sorcersoft.sorcer:exertmonitor-cfg:' + SorcerConstants.SORCER_VERSION,
            'dl'  : 'org.sorcersoft.sorcer:default-codebase:' + SorcerConstants.SORCER_VERSION
    ]

    service(name: "ExertMonitor") {
        interfaces {
            classes 'sorcer.core.monitor.MonitoringManagement'
            artifact monitor.dl
        }
        implementation(class: 'sorcer.core.provider.exertmonitor.ExertMonitor') {
            artifact monitor.impl
        }
        configuration file: 'classpath:exertmonitor.config'
        maintain 1
    }
}
