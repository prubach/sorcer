import sorcer.provider.boot.SorcerCapabilityDescriptor
import sorcer.provider.boot.SorcerCapabilityDescriptor
import sorcer.provider.boot.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "River"
    cap.version = "2.2.1"
    cap.manufacturer = "Apache.org"
    cap.setClasspath(['net.jini:jsk-dl', 'net.jini.lookup:serviceui'])

    return cap;
}