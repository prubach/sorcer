import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            "Plexus-utils",
            "3.0.15",
            "Plexus utils",
            "Codehaus.org",
            'org.codehaus.plexus:plexus-utils:3.0.15'
    )
    return cap
}
