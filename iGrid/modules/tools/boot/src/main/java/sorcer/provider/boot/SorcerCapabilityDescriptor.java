package sorcer.provider.boot;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.rioproject.config.PlatformCapabilityConfig;

import sorcer.util.ArtifactCoordinates;
import sorcer.util.LibraryPathHelper;

/**
 * @author Rafał Krupiński
 */
public class SorcerCapabilityDescriptor extends PlatformCapabilityConfig {

	private static final String COORDS_SEPARATOR = ":";

	public SorcerCapabilityDescriptor() {
	}

	public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
			String classpath) {
		super(name, version, description, manufacturer, classpath);
	}

	public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
			Collection<String> classpath, String libraryPath) {
		this(name, version, description, manufacturer, classpath);
		setLibraryPath(libraryPath);
	}

	public void setLibraryPath(String libraryPath) {
		LibraryPathHelper.updateLibraryPath(libraryPath);
	}

	public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
			Collection<String> classpath) {
		super(name, version, description, manufacturer, null);
		setClasspath(classpath);
	}

	@Override
	public void setClasspath(String classpath) {
		super.setClasspath(resolve(classpath));
	}

	public void setClasspath(Collection<String> classpath) {
		super.setClasspath(resolve(classpath));
	}

	protected String resolve(Collection<String> classpath) {
		List<String> result = new LinkedList<String>();
		for (String entry : classpath) {
			result.add(resolve(entry));
		}
		return StringUtils.join(result, File.pathSeparator);
	}

	public String resolve(String entry) {
		if (new File(entry).exists()) {
			return entry;
		}
		String[] splitted = entry.split(COORDS_SEPARATOR);
		if (splitted.length < 2 || splitted.length > 5) {
			return entry;
		}
		String coords = entry;
		if (splitted.length == 2) {
			coords += COORDS_SEPARATOR + getVersion();
		}
		try {
			return ArtifactCoordinates.coords(coords).fromLocalRepo();
		} catch (IllegalArgumentException x) {
			return entry;
		}
	}
}
