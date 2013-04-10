package sorcer.util;

import sorcer.core.SorcerConstants;

/**
 * @author Rafał Krupiński
 */
public class ArtifactCoordinates {
    private String groupId;
    private String artifactId;
    private String version;

    public static ArtifactCoordinates coords(String coords) {
        String[] coordSplit = coords.split(":");
        if (coordSplit.length < 2 || coordSplit.length > 3) {
            throw new IllegalArgumentException("Artifact coordinates must be in a form of groupId:artifactId:version or groupId:artifactId");
        }
        String groupId = coordSplit[0];
        String artifactId = coordSplit[1];
        String version = coordSplit.length == 3 ? coordSplit[2] : SorcerConstants.SORCER_VERSION;
        return new ArtifactCoordinates(groupId, artifactId, version);
    }

    public static ArtifactCoordinates coords(String groupId, String artifactId, String version) {
        return new ArtifactCoordinates(groupId, artifactId, version);
    }

    public static ArtifactCoordinates coords(String groupId, String artifactId) {
        return new ArtifactCoordinates(groupId, artifactId, SorcerConstants.SORCER_VERSION);
    }

    public ArtifactCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    public ArtifactCoordinates(String groupId, String artifactId) {
        this(groupId, artifactId, SorcerConstants.SORCER_VERSION);
    }
    
    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getRelativePath() {
        StringBuilder result = new StringBuilder(groupId.replace('.', '/'));
        result.append('/').append(artifactId).append('/').append(version).append('/').append(artifactId).append('-').append(version).append(".jar");
        return result.toString();
    }

    @Override
    public String toString() {
        return getRelativePath();
    }
    
    public static String getSorcerApi() { 
    	return new ArtifactCoordinates("org.sorcersoft.sorcer", "sorcer-api").toString();
    }

    public static String getDbpService() { 
    	return new ArtifactCoordinates("org.sorcersoft.sorcer", "dbp-service").toString();
    }
    
    public static String getJobberService() { 
    	return new ArtifactCoordinates("org.sorcersoft.sorcer", "jobber-service").toString();
    }
}
