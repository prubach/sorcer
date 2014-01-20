/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.rio;

import org.rioproject.impl.opstring.DSLException;
import org.rioproject.impl.opstring.GroovyDSLOpStringParser;
import org.rioproject.impl.opstring.OpString;
import org.rioproject.impl.opstring.OpStringParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class SerialisedOpStringParser implements OpStringParser {
    private final static Logger log = LoggerFactory.getLogger(SerialisedOpStringParser.class);
    public static final String EXT_SER = "ser";
    private File serDir = new File(SorcerEnv.getHomeDir(), "databases/opstring");
    private OpStringParser backend;

    {
        serDir.mkdirs();
    }

    @Override
    public List<OpString> parse(Object source, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        File serFile;
        URL srcUrl;
        if (source instanceof File) {
            File srcFile = (File) source;
            try {
                srcUrl = srcFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else if (source instanceof URL) {
            srcUrl = (URL) source;
        } else
            throw new DSLException("Unrecognized source " + source);
        serFile = getSerialisedFile(srcUrl);

        List<OpString> result = parse(srcUrl, serFile, loader, defaultExportJars, defaultGroups, loadPath);
        log.info("{} -> {} opstrings", source, result.size());
        return result;
    }

    public List<OpString> parse(URL source, File serialised, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        try {
            log.info("Parsing {}", source);
            List<OpString> result;
            File container;
            if ("file".equals(source.getProtocol())) {
                container = new File(source.toURI());
            } else if ("jar".equals(source.getProtocol())) {
                String sub = source.toURI().getSchemeSpecificPart();

                System.out.println(sub);
                System.exit(0);
                container = new File(new URI(sub));
            } else {
                return defaultParse(source, loader, defaultExportJars, defaultGroups, loadPath);
            }

            if (source.getProtocol().equals("jar")) {

            }
            if (serialised.exists()) {


                URLConnection urlConnection = source.openConnection();
                //JarURLConnection urlConnection = (JarURLConnection) source.openConnection();
                //urlConnection.getLastModified();
                //new File(urlConnection.getJarFileURL().toURI());
                long lastModified = container.lastModified();
                if (serialised.lastModified() > lastModified) {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(serialised));
                    result = (List<OpString>) in.readObject();
                    in.close();
                    return result;
                }
            }

            result = defaultParse(source, loader, defaultExportJars, defaultGroups, loadPath);
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serialised));
                out.writeObject(result);
                out.close();
                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<OpString> defaultParse(Object source, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        List<OpString> result;
        if (backend == null)
            backend = new GroovyDSLOpStringParser();
        result = backend.parse(source, loader, defaultExportJars, defaultGroups, loadPath);
        return result;
    }

    public static File getSerialisedFile(URL src) {
        String serPath = replaceExt(src.toExternalForm(), EXT_SER);
        try {
            return new File(new URL(serPath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error while converting URL to URI: " + serPath, e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while creating URL: " + serPath, e);
        }
    }

    private static String replaceExt(String path, String ext) {
        return path.substring(0, path.lastIndexOf('.') + 1) + ext;
    }
}
