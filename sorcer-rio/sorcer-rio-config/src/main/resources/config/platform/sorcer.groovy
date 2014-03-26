import sorcer.rio.util.SorcerCapabilityDescriptor
import static sorcer.core.SorcerConstants.SORCER_VERSION

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            'SORCER',
            SORCER_VERSION,
            "SORCER OS",
            "SorcerSoft.com",
            [
                    'org.sorcersoft.lockmgr:lockmgr-api:0.2-3',
//                    'org.sorcersoft.sorcer:sos-platform',
                    'org.sorcersoft.sorcer:logger-api',
//                    'org.sorcersoft.sorcer:commons-prv',
            ],
            false
    )
    return cap
}
