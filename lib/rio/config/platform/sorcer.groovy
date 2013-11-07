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
                    'org.sorcersoft.sorcer:logger-api',
                    'org.sorcersoft.sorcer:commons-prv',
                    'org.sorcersoft.sorcer:sorcer-api'
            ],
            false
    )
    cap.common = true
    return cap
}
