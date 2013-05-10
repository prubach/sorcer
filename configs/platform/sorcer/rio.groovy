import sorcer.provider.boot.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "Rio"
    cap.version = "5.0-M2"
    cap.platformClass = "org.rioproject.system.capability.software.RioSupport"
    cap.setClasspath([/*'org.rioproject:rio-platform',*/ 'org.rioproject:rio-api'])

    return cap;
}