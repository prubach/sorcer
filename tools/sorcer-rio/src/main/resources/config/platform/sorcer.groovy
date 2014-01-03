package config.platform

import sorcer.rio.util.SorcerCapabilityDescriptor
import static sorcer.core.SorcerConstants.SORCER_VERSION

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            'SORCER',
            SORCER_VERSION,
            "SORCER OS",
            "SorcerSoft.com",
            [
                    'org.sorcersoft.sorcer:sos-api',
                    'org.sorcersoft.sorcer:sos-platform',
                    'org.codehaus.plexus:plexus-utils:3.0.15',
                    'org.sorcersoft.sorcer:logger-api',
                    'org.sorcersoft.sorcer:commons-prv'
            ],
            false
    )
    return cap
}
