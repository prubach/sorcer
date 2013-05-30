import sorcer.provider.boot.SorcerCapabilityDescriptor
import static sorcer.core.SorcerConstants.SORCER_VERSION

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = 'SORCER'
    cap.version = SORCER_VERSION
    cap.manufacturer = "SorcerSoft.com"
    cap.setClasspath([
            'org.sorcersoft.sorcer:sos-platform',
            'org.sorcersoft.sorcer:commons-prv',
            'org.sorcersoft.sorcer:sos-util'
    ])

    return cap;
}