import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    return new SorcerCapabilityDescriptor(
            "River",
            "3.0-M1",
            "Apache River",
            "Apache.org",
            [
                    'com.sorcersoft.river:jsk-lib',
                    'com.sorcersoft.river:serviceui',
            ]
    )
}
