import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            "River",
            "2.2.1",
            "Apache River",
            "Apache.org",
            'net.jini:jsk-lib'
    )
    cap.common = true
    return cap
}
