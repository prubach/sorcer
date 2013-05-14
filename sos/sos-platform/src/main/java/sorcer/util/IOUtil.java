package sorcer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Rafał Krupiński
 */
public class IOUtil {
	/**
	 * Deletes a direcory and all its files.
	 *
	 * @param dir
	 *            to be deleted
	 * @return true if the directory is deleted
	 * @throws Exception
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	/**
	 * Copy in to out stream. Do not allow other threads to read from the input
	 * or write to the output while copying is taking place.
	 */
	synchronized public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[256];
		int bytesRead;
		while (true) {
			bytesRead = in.read(buffer);
			if (bytesRead == -1)
				break;
			out.write(buffer, 0, bytesRead);
		}
	}
}
