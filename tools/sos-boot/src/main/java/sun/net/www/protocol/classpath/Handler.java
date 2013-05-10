package sun.net.www.protocol.classpath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Rafał Krupiński
 */
public class Handler extends URLStreamHandler {


    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        String path = u.getPath();
        if(path.startsWith("/")){
            path=path.substring(1);
        }
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource == null) {
            throw new FileNotFoundException(u.toString());
        }
        return resource.openConnection();
    }
}
