import sorcer.provider.boot.SorcerCapabilityDescriptor
import sorcer.provider.boot.SorcerCapabilityDescriptor
import sorcer.provider.boot.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "Berkeley DB JE"
    cap.version = "4.1.21"
    cap.manufacturer = "Oracle"
    cap.setClasspath('com.sleepycat:je')

    return cap;
}