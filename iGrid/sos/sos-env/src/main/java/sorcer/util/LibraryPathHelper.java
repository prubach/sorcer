package sorcer.util;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class LibraryPathHelper {
	private static final String KEY_LIB_PATH = "java.library.path";
	private static final Logger log = LoggerFactory.getLogger(LibraryPathHelper.class);

	static Field fieldSysPath;
	static {
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}

	public static synchronized void updateLibraryPath(String libraryPath) {
		File libraryDir = new File(libraryPath);
		if (!libraryDir.exists() || !libraryDir.isDirectory() || !libraryDir.canRead()) {
			throw new IllegalArgumentException("Could not access directory " + libraryPath);
		}

		String systemLibPath = System.getProperty(KEY_LIB_PATH);
		if (StringUtils.isEmpty(systemLibPath)) {
			systemLibPath = libraryPath;
		} else {
			systemLibPath += File.pathSeparator + libraryPath;
		}
		log.info("New {} = {}", KEY_LIB_PATH, systemLibPath);
		System.setProperty(KEY_LIB_PATH, systemLibPath);

		try {
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException("Could not update java.library.path system property", e);
		}
	}
}
