package sorcer.resolver;

import sorcer.util.ArtifactCoordinates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
public class HybridArtifactResolver extends AbstractArtifactResolver {

	public static final String GROUPDIR_DEFAULT = "commons";
	protected File rootDir;

	protected Map<String, String> groupDirMap = new HashMap<String, String>();

	public HybridArtifactResolver(File rootDir) {
		this.rootDir = rootDir;
	}

	{
		String resourceName = "META-INF/maven/repolayout.properties";
		URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if (resource == null) {
			throw new RuntimeException("Could not find repolayout.properties");
		}
		Properties properties = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = resource.openStream();
			properties.load(inputStream);
			// properties is a Map<Object, Object> but it contains only Strings
			@SuppressWarnings("unchecked")
			Map<String, String> propertyMap = (Map) properties;
			groupDirMap.putAll(propertyMap);
		} catch (IOException e) {
			throw new RuntimeException("Could not load repolayout.properties", e);
		} finally {
			close(inputStream);
		}
	}

	@Override
	public String resolveAbsolute(ArtifactCoordinates artifactCoordinates) {
		return new File(rootDir, resolveRelative(artifactCoordinates)).getPath();
	}

	@Override
	public String resolveRelative(ArtifactCoordinates coords) {
		String groupId = coords.getGroupId();
		String groupDir;
		if (groupDirMap.containsKey(groupId)) {
			groupDir = groupDirMap.get(groupId);
		} else {
			groupDir = GROUPDIR_DEFAULT;
		}
		return new File(groupDir, coords.getArtifactId() + (coords.getClassifier()!=null ? '-' + coords.getClassifier() : "") + '.' + coords.getPackaging())
				.getPath();
	}

    @Override
    public String getRootDir() {
        return rootDir.toString();
    }
}
