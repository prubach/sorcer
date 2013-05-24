package sorcer.boot.util;

import sorcer.util.Zip;
import sorcer.resolver.Resolver;
import sorcer.util.LibraryPathHelper;

import java.io.File;
import java.io.IOException;

/**
 * Add contents of maven artifact to java.library.path
 *
 * @author Rafał Krupiński
 */
public class MavenLibraryHelper {
	/**
	 * Unzip an artifact and add a directory to java.library.path
	 *
	 * @param coords Maven artifact coordinates of zip file containing libs
	 * @throws IOException
	 */
	public static void installLibFromArtifact(String coords) throws IOException {
		installLibFromArtifact(coords, null);
	}

	/**
	 * Unzip an artifact and add a directory to java.library.path
	 *
	 * @param coords            artifact coordinates to use as a
	 * @param internalDirectory root directory in the zip file, may be null
	 */
	public static void installLibFromArtifact(String coords, String internalDirectory) throws IOException {
		File artifactFile = new File(Resolver.resolveAbsolute(coords));
		File target = artifactFile.getParentFile();
		String child = internalDirectory != null ? internalDirectory : ".";
		File libraryDir = new File(target, child);

		//if the directory exists, assume it was properly unzipped
		if (!libraryDir.exists()) {
			Zip.unzip(artifactFile, target);
		}

		LibraryPathHelper.updateLibraryPath(libraryDir.getPath());
	}
}
