import sorcer.provider.boot.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "River"
    cap.version = "2.2.1"
    cap.manufacturer = "Apache.org"
    cap.setClasspath([
            'net.jini:jsk-dl',
             /*'net.jini.lookup:serviceui',*/
            'net.jini:jsk-lib',
            'net.jini:jsk-platform'
    ])

    return cap;
}
