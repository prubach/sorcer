package sorcer.util;

import java.io.File;

/**
 * @author Rafał Krupiński
 */
public class FileUtils {
    /**
     * Same as new File(parent, child), but if child is absolute, return new File(child)
     */
    public static File getFile(File parent, String child) {
        File result = new File(child);
        if (result.isAbsolute())
            return result;
        else
            return new File(parent, child);
    }
}
