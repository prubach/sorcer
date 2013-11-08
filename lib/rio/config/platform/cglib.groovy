import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            'CGLib',
            '2.1_3',
            "Hyperic SIGAR",
            "Hyperic",
            'cglib:cglib'
    )
    cap.common = true
    return cap
}