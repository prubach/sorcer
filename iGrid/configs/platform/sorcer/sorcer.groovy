import sorcer.provider.boot.SorcerCapabilityDescriptor
import static sorcer.core.SorcerConstants.SORCER_VERSION

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = 'SORCER'
    cap.version = SORCER_VERSION
    cap.manufacturer = "SorcerSoft.com"
    cap.setClasspath(['org.sorcersoft.sorcer:sorcer-api', 'org.sorcersoft.sorcer:provider-common'])

    return cap;
}