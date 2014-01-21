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

import org.apache.commons.io.IOUtils;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class SerialisedOpStringParser implements OpStringParser {
    private final static Logger log = LoggerFactory.getLogger(SerialisedOpStringParser.class);
    public static final String EXT_SER = "ser";
    public static final String PREFIX_FILE = "file";
    private File serDir = new File(SorcerEnv.getHomeDir(), "databases/opstring");
    private OpStringParser backend;

    {
        serDir.mkdirs();
    }

    @Override
    public List<OpString> parse(Object source, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        URL srcUrl;
        File container;
        String path;
        if (source instanceof File) {
            File srcFile = (File) source;
            try {
                srcUrl = srcFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            container = srcFile;
            path = srcFile.getPath();
        } else if (source instanceof URL) {
            srcUrl = (URL) source;
            try {
                if (PREFIX_FILE.equals(srcUrl.getProtocol())) {
                    container = new File(srcUrl.toURI());
                    path = container.getPath();
                } else if ("jar".equals(srcUrl.getProtocol())) {
                    // cache only for local files
                    if (!srcUrl.toExternalForm().startsWith("jar:file:"))
                        return defaultParse(source, loader, defaultExportJars, defaultGroups, loadPath);

                    JarURLConnection jarUrl = (JarURLConnection) srcUrl.openConnection();
                    container = new File(jarUrl.getJarFileURL().toURI());
                    path = jarUrl.getEntryName();
                } else {
                    return defaultParse(source, loader, defaultExportJars, defaultGroups, loadPath);
                }
            } catch (IOException e) {
                //JarUrlConnection.openConnection() on local file should never happen
                throw new DSLException(e);
            } catch (URISyntaxException e) {
                throw new DSLException(e);
            }
        } else
            throw new DSLException("Unrecognized source " + source);

        File serFile = getSerialisedFile(container, path);

        return parse(container, srcUrl, serFile, loader, defaultExportJars, defaultGroups, loadPath);
    }

    public List<OpString> parse(File container, URL source, File serialised, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        List<OpString> result;

        if (serialised.exists() && serialised.lastModified() > container.lastModified()) {
            log.debug("Reading opstrings from cache {}", serialised);
            List<OpString> opStrings = readFile(serialised);
            if (opStrings != null)
                return opStrings;
        }

        result = defaultParse(source, loader, defaultExportJars, defaultGroups, loadPath);

        try {
            writeFile(serialised, result);
        } catch (IOException e) {
            log.warn("Could not write {}", serialised, e);
        }

        return result;
    }

    private void writeFile(File serialised, List<OpString> result) throws IOException {
        log.debug("Writing cached opstring to {}", serialised);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serialised));
        out.writeObject(result);
        out.close();
    }

    @SuppressWarnings("unchecked")
    private List<OpString> readFile(File serialised) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(serialised));
            Object o = in.readObject();
            in.close();
            return o instanceof List ? (List<OpString>) o : null;
        } catch (IOException e) {
            log.warn("Could not read {}", serialised, e);
            return null;
        } catch (ClassNotFoundException e) {
            log.warn("Error while reading serialised file {}", serialised, e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private List<OpString> defaultParse(Object source, ClassLoader loader, String[] defaultExportJars, String[] defaultGroups, Object loadPath) {
        log.debug("Parsing opstring from {}", source);
        List<OpString> result;
        if (backend == null)
            backend = new GroovyDSLOpStringParser();
        result = backend.parse(source, loader, defaultExportJars, defaultGroups, loadPath);
        return result;
    }

    public File getSerialisedFile(File container, String path) {
        File file = new File(replaceExt(path, EXT_SER));
        if (container.getPath().equals(path))
            return new File(serDir, file.getName());
        else
            return new File(serDir, container.getName() + "_" + file.getName());
    }

    private static String replaceExt(String path, String ext) {
        return path.substring(0, path.lastIndexOf('.') + 1) + ext;
    }
}
