import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor()
    cap.name = "Rio"
    cap.version = "5.0-M4_sorcer1"
    cap.platformClass = "org.rioproject.system.capability.software.RioSupport"
    cap.setClasspath(['org.rioproject:rio-lib', 'org.rioproject:rio-api'])

    return cap;
}