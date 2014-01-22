package sorcer.rio.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.FileUtils;
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
    final private static Logger log = LoggerFactory.getLogger(MavenLibraryHelper.class);

	/**
	 * Unzip an artifact and add a directory to java.library.path
	 *
	 * @param coords Maven artifact coordinates of zip file containing libs
	 * @throws java.io.IOException
	 */
	public static void installLibFromArtifact(String coords) throws IOException {
		installLibFromArtifact(coords, null);
	}

	/**
	 * Unzip an artifact and add a directory to java.library.path
	 *
	 * @param coords            artifact coordinates to use as a
     * @param targetDir root directory in the zip file, may be null
	 */
    public static void installLibFromArtifact(String coords, String targetDir) throws IOException {
		File artifactFile = new File(Resolver.resolveAbsolute(coords));
        File parent = artifactFile.getParentFile();

        File target = FileUtils.getFile(parent, targetDir);
		//if the directory exists, assume it was properly unzipped
        if (!target.exists()) {
            log.debug("Unzip {} to {}", coords, target);
            Zip.unzip(artifactFile, target);
        } else
            log.debug("For {} using existing {}", coords, target);

        LibraryPathHelper.getLibraryPath().add(target.getPath());
	}
}
