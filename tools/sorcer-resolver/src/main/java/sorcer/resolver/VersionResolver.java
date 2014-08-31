package sorcer.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.PropertiesLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VersionResolver {
    final private static Logger log = LoggerFactory.getLogger(VersionResolver.class);

    // groupId_artifactId -> version
    protected Map<String, String> versions = new HashMap<String, String>();
    static PropertiesLoader propertiesLoader = new PropertiesLoader();
    static File VERSIONS_PROPS_FILE = new File(SorcerEnv.getHomeDir(), "configs/groupversions.properties");

    {
        versions = propertiesLoader.loadAsMap("META-INF/maven/groupversions.properties", Thread.currentThread()
                .getContextClassLoader());
        try {
            versions.putAll(propertiesLoader.loadAsMap(VERSIONS_PROPS_FILE));
        } catch (IOException e) {
            log.warn("Could not load versions from {}", VERSIONS_PROPS_FILE, e);
        }
    }

    static public final VersionResolver instance = new VersionResolver();

    /**
     * Resolve version of artifact using groupversions.properties or pom.properties
     * from individual artifact jar the jar must be already in the classpath of
     * current thread context class loader in order to load its pom.version
     *
     * @param coords maven coordinates groupId and artifactId, optionally type
     * @return artifacts version
     * @throws IllegalArgumentException if version could not be found
     */
    public String resolveVersion(ArtifactCoordinates coords) {
        String version = resolveCachedVersion(coords.getGroupId(), coords.getArtifactId());
        if (version != null)
            if (checkFileExists(coords.getGroupId(), coords.getArtifactId(), version, coords.getPackaging())) {
                return version;
            } else
                log.warn("Version of {}:{} resolved to {} but file not found", coords.getGroupId(), coords.getArtifactId(), version);

        version = loadVersionFromPomProperties(coords.getGroupId(), coords.getArtifactId());
        if (version == null) {
            throw new IllegalArgumentException("Could not load version of " + coords.getGroupId() + ':' + coords.getArtifactId());
        }
        versions.put(key(coords.getGroupId(), coords.getArtifactId()), version);
        return version;
    }

    /**
     * Resolve version of artifact using groupversions.properties or pom.properties
     * from individual artifact jar the jar must be already in the classpath of
     * current thread context class loader in order to load its pom.version
     *
     * @param groupId    maven artifacts groupId
     * @param artifactId maven artifacts artifactId
     * @return artifacts version
     * @throws IllegalArgumentException if version could not be found
     */
    public String resolveVersion(String groupId, String artifactId) {
        return resolveVersion(new ArtifactCoordinates(groupId, artifactId, null));
    }


    private String loadVersionFromPomProperties(String groupId, String artifactId) {
        String resourceName = String.format("META-INF/maven/%1$s/%2$s/pom.properties", groupId, artifactId);
        Properties properties;
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            properties = propertiesLoader.loadAsProperties(resourceName, contextClassLoader);
        } catch (IllegalArgumentException x) {
            log.debug("Could not find {} in classpath", resourceName);
            return null;
        }

        String version = properties.getProperty("version");
        if (version == null) {
            throw new IllegalArgumentException("Could not load version of " + groupId + ':' + artifactId
                    + " from artifacts pom.properties");
        }
        return version;
    }

    /**
     * @return cached version, may be null
     */
    String resolveCachedVersion(String groupId, String artifactId) {
        String key = key(groupId, artifactId);
        if (versions.containsKey(key)) {
            return versions.get(key);
        }
        // may be null
        return versions.get(groupId);
    }

    private static String key(String groupId, String artifactId) {
        return groupId + "_" + artifactId;
    }

    private boolean checkFileExists(String groupId, String artifactId, String version, String packaging) {
        String path = Resolver.resolveAbsolute(ArtifactCoordinates.coords(groupId, artifactId, version, packaging));
        return new File(path).exists();
    }
}