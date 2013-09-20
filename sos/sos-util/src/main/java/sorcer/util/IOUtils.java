package sorcer.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

public class IOUtils {

	public static void closeQuietly(ZipFile closeable) {
		//stupid java 1.6 doesn't know ZipFile is Closeable
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	protected static void checkFileExists(File file) throws IOException {
		if (!file.exists()) {
			throw new IOException("***error: the file does not exist: "
					+ file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IOException("***error: the file is not readable: "
					+ file.getAbsolutePath());
		}
	}

    // this method exits the jvm if the file or directory is not readable; the exit is
    // necessary for boot strapping providers since exceptions in provider constructors
    // are simply caught and ignored...exit brings the provider down, which is good.
    public static void checkFileExistsAndIsReadable(File file) {

        try {

            if(!file.exists()) {
                System.out.println("***error: file does not exist = "
                        + file.getAbsolutePath());
                //if (sp != null) sp.destroy();
                throw new IOException("***error: file does not exist = "
                        + file.getAbsolutePath());

            }

            if (!file.canRead()){
                System.out.println("***error: file does not have read permission = "
                        + file.getAbsolutePath());
                //if (sp != null) sp.destroy();
                throw new IOException("***error: file does not have read permission = "
                        + file.getAbsolutePath());
            }

        } catch (IOException e) {
            System.out.println("***error: " + e.toString()
                    + "; problem with file = " + file.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

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


	// this is copied from apache commons-io
	/**
	 * The default buffer size to use for
	 * {@link #copyLarge(java.io.InputStream, java.io.OutputStream)}
	 * and
	 * {@link #copyLarge(java.io.Reader, java.io.Writer)}
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * Copy bytes from an <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p/>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p/>
	 * Large streams (over 2GB) will return a bytes copied value of
	 * <code>-1</code> after the copy has completed since the correct
	 * number of bytes cannot be returned as an int. For large streams
	 * use the <code>copyLarge(InputStream, OutputStream)</code> method.
	 *
	 * @param input  the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws java.io.IOException  if an I/O error occurs
	 * @since Commons IO 1.1
	 */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p/>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input  the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws java.io.IOException  if an I/O error occurs
	 * @since Commons IO 1.3
	 */
	public static long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Unconditionally close a <code>Closeable</code>.
	 * <p/>
	 * Equivalent to {@link java.io.Closeable#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   Closeable closeable = null;
	 *   try {
	 *       closeable = new FileReader("foo.txt");
	 *       // process closeable
	 *       closeable.close();
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(closeable);
	 *   }
	 * </pre>
	 *
	 * @param closeable the object to close, may be null or already closed
	 * @since Commons IO 2.0
	 */
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
}
