def getSorcerVersion() {
    return sorcerVersion = SorcerEnv.getSorcerVersion();
}

def getRiverVersion() {
    return "3.0-M1";
}

def getBlitzVersion() {
    return "2.2.1-1";
}

def sorcer(String artifactId) {
    return 'org.sorcersoft.sorcer:' + artifactId + ':' + getSorcerVersion();
}

deployment(name: 'SorcerCommon') {
    groups getInitialMemberGroups();

    codebase getCodebase()

    artifact id: 'cataloger-prv', sorcer('cataloger-prv')
    artifact id: 'cataloger-api', sorcer('cataloger-api')
    artifact id: 'jobber-prv', sorcer('jobber-prv')
    artifact id: 'spacer-prv', sorcer('spacer-prv')
    artifact id: 'logger-prv', sorcer('logger-prv')
    artifact id: 'logger-sui', sorcer('logger-sui')
    artifact id: 'dbp-prv', sorcer('dbp-prv')
    artifact id: 'exertmonitor-prv', sorcer('exertmonitor-prv')
    artifact id: 'commons-prv', sorcer('commons-prv')

    artifact id: 'mahalo', 'com.sorcersoft.river:mahalo:' + getRiverVersion()
    artifact id: 'mahalo-dl', 'com.sorcersoft.river:mahalo-dl:' + getRiverVersion()

    artifact id: 'blitz-dl', 'org.sorcersoft.blitz:blitz-proxy:' + getBlitzVersion()
    artifact id: 'blitz-impl', 'org.sorcersoft.blitz:blitz-service:' + getBlitzVersion()
}
