import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    return new SorcerCapabilityDescriptor(
            "River",
            "2.2.2",
            "Apache River",
            "Apache.org",
            [
                    'net.jini:jsk-lib',
                    'net.jini.lookup:serviceui',
            ]
    )
}
