package config.platform

import sorcer.rio.util.SorcerCapabilityDescriptor

def getPlatformCapabilityConfig() {
    def cap = new SorcerCapabilityDescriptor(
            'CGLib',
            '2.1_3',
            "CGLib",
            "CGLib",
            'cglib:cglib'
    )
    return cap
}