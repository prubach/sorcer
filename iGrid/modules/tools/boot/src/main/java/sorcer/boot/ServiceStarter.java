package sorcer.boot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter {
    private static final String CONFIG = "META-INF/sorcer/services.config";

    public static void main(String[] args) throws IOException {
        String[] myArgs;
        if (args.length != 0 && !args[0].endsWith(".config")) {
            myArgs = findConfig(args[0]);
        } else {
            myArgs = args;
        }
        com.sun.jini.start.ServiceStarter.main(myArgs);
    }


    private static String[] findConfig(String path) throws IOException {
        return new String[]{findConfigUrl(path).toExternalForm()};
    }

    private static URL findConfigUrl(String path) throws IOException {
        File configFile = new File(path);

        if (!configFile.exists()) {
            throw new FileNotFoundException(path);
        } else if (configFile.isDirectory()) {
            return new File(configFile, CONFIG).toURI().toURL();
        } else if (path.endsWith(".jar")) {
            ZipEntry entry = new ZipFile(path).getEntry(CONFIG);
            if (entry != null) {
                return new URL(String.format("jar:file:%1$s!/%2$s", path, CONFIG));
            }
        }
        throw new FileNotFoundException(CONFIG);
    }
}
