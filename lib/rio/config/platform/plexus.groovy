import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "Plexus-utils"
    cap.version = "3.0.15"
    cap.manufacturer = "Codehaus.org"
    cap.setClasspath([
            'org.codehaus.plexus:plexus-utils:3.0.15'
    ])

    return cap;
}
