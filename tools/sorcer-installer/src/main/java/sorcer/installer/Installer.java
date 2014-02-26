/**
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
package sorcer.installer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import sorcer.core.SorcerEnv;
import sorcer.resolver.ArtifactResolver;
import sorcer.resolver.MappedFlattenedArtifactResolver;
import sorcer.resolver.RepositoryArtifactResolver;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.IOUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static sorcer.util.JavaSystemProperties.USER_NAME;

/**
 * User: prubach
 * Date: 14.05.13
 * Time: 10:18
 */
public class Installer {
    final private static Logger log = LoggerFactory.getLogger(Installer.class);

    private ArtifactResolver libResolver;
    private ArtifactResolver mvnResolver = new RepositoryArtifactResolver(SorcerEnv.getRepoDir());

    protected Map<String, String> groupDirMap;

    final private static String MARKER_FILENAME = "sorcer_jars_installed_user_";

    final private static String MARKER_FILENAME_EXT = ".tmp";

    final private static String COMMONS_LIBS = "commons";
    protected static File configDir = new File(SorcerEnv.getHomeDir(), "configs");

    private static File REPOLAYOUT_PROPS_FILE = new File(configDir, "repolayout.properties");

    int errorCount = 0;

    // artifactId -> coordinates (non-pom only)
    private Map<String, ArtifactCoordinates> artifactsFromPoms = new HashMap<String, ArtifactCoordinates>();

    public static void main(String[] args) throws IOException {
        new Installer().install();
    }

    public boolean isInstallRequired(File logDir) {
        assert logDir != null;
        File sorcerApi = new File(libResolver.resolveAbsolute("org.sorcersoft.sorcer:sorcer-api"));
        File markerFile = new File(logDir, "sorcer_jars_installed_user_" + System.getProperty(USER_NAME) + ".tmp");
        return sorcerApi.exists() && !markerFile.exists();
    }

    public void install() throws IOException {
        installPoms();
        installJars();
        createMarker();
    }

    @SuppressWarnings("unchecked")
    public Installer() throws IOException {
        String repoDir = SorcerEnv.getRepoDir();
        try {
            if (repoDir == null)
                throw new IOException("Maven repository root directory is undefined");
            else
                FileUtils.forceMkdir(new File(repoDir));
        } catch (IOException io) {
            log.error("Problem installing jars to local maven repository - repository directory {} does not exist! ", repoDir, io);
            System.exit(-1);
        }

        groupDirMap = (Map) readProperties("META-INF/maven/repolayout.properties", REPOLAYOUT_PROPS_FILE);

        libResolver = new MappedFlattenedArtifactResolver(new File(SorcerEnv.getLibPath()), groupDirMap);
    }

    /**
     * loadProperties(map, File) version is used to load additional data in commercial distribution
     */
    protected Properties readProperties(String resourceName, File versions_props_file) throws IOException {
        Properties versions = new Properties();
        loadProperties(versions, resourceName);
        loadProperties(versions, versions_props_file);
        return versions;
    }

    protected static void loadProperties(Properties properties, String resourceName) throws IOException {
        URL resourceVersions = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resourceVersions == null) {
            throw new IOException("Could not find versions.properties");
        }
        InputStream inputStream = null;
        try {
            inputStream = resourceVersions.openStream();
            properties.load(inputStream);
        } finally {
            closeQuietly(inputStream);
        }
    }

    protected static void loadProperties(Properties properties, File file) throws IOException {
        if (!file.exists()) return;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } finally {
            closeQuietly(inputStream);
        }
    }

    public void installPoms() throws IOException {
        File pomDir = new File(SorcerEnv.getHomeDir(), "configs/poms");

        File[] poms = pomDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("pom");
            }
        });
        for (File pom : poms) {
            ArtifactCoordinates ac = getArtifactCoordsFromPom(pom);
            if (ac == null) {
                log.error("Problem while reading pom", pom);
                errorCount++;
                return;
            }

            if (!"pom".equals(ac.getPackaging())) {
                String artifactId = ac.getArtifactId();
                artifactsFromPoms.put(artifactId, ac);
            }

            try {
                install(pom, toPomCoordinates(ac));
            } catch (IOException io) {
                errorCount++;
                log.error("Problem installing pom file: {}", pom.getAbsolutePath(), io);
            }
        }
    }

    public void installJars() throws IOException {
        File libDir = new File(SorcerEnv.getLibPath());
        //Collection<File> files = FileUtils.listFiles(libDir, new String[]{"jar", "zip"}, true);

        Collection<String> paths = new ArrayList<String>(groupDirMap.size() + 1);
        paths.addAll(groupDirMap.values());
        paths.add(COMMONS_LIBS);

        for (String dir : paths) {
            File dir1 = new File(libDir, dir);
            log.info("Installing files from {}", dir1);
            for (File file : dir1.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String name = file.getName();
                    return !file.isDirectory() && (name.endsWith("jar") || name.endsWith("zip"));
                }
            })) {
                try {
                    install(file);
                } catch (IOException x) {
                    ++errorCount;
                    log.error("Erro while installing {}", file, x);
                }
            }
        }
    }

    protected ArtifactCoordinates installPomFromJar(File jarFile) throws IOException {
        ZipFile jarZip = null;
        try {
            jarZip = new ZipFile(jarFile);
            ZipEntry pomEntry = get(jarZip, new ZipEntryFilter() {
                @Override
                public boolean accept(ZipEntry entry) {
                    String name = entry.getName();
                    return (!entry.isDirectory() && name.startsWith("META-INF/maven/") && name.endsWith("pom.xml"));
                }
            });
            if (pomEntry == null)
                throw new FileNotFoundException("pom.xml entry not found in " + jarFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) pomEntry.getSize());
            org.apache.commons.io.IOUtils.copy(jarZip.getInputStream(pomEntry), outputStream);

            byte[] pomBytes = outputStream.toByteArray();
            ArtifactCoordinates jarCoords = getArtifactCoordsFromPom(new ByteArrayInputStream(pomBytes));
            install(new ByteArrayInputStream(pomBytes), toPomCoordinates(jarCoords));
            return jarCoords;
        } finally {
            IOUtils.closeQuietly(jarZip);
        }
    }

    public void install(File file) throws IOException {
        File fileNoExt = replaceExt(file, null);
        String artifactId = fileNoExt.getName();

        ArtifactCoordinates ac;

        if (artifactsFromPoms.containsKey(artifactId)) {
            ac = artifactsFromPoms.get(artifactId);
        } else {
            ac = installPomFromJar(file);
        }

        install(ac);
    }

    protected void install(ArtifactCoordinates coordinates) throws IOException {
        File srcJarFile = new File(libResolver.resolveAbsolute(coordinates));
        install(srcJarFile, coordinates);
    }

    private void install(File srcJarFile, ArtifactCoordinates destCoords) throws IOException {
        File destJarFile = new File(mvnResolver.resolveAbsolute(destCoords));
        if (destJarFile.exists() && !destCoords.isVersionSnapshot())
            return;
        FileUtils.forceMkdir(destJarFile.getParentFile());
        FileUtils.copyFile(srcJarFile, destJarFile);
    }

    protected void install(InputStream inputStream, ArtifactCoordinates coordinates) throws IOException {
        File destFile = new File(mvnResolver.resolveAbsolute(coordinates));
        if (destFile.exists() && !coordinates.isVersionSnapshot())
            return;
        FileUtils.copyInputStreamToFile(inputStream, destFile);
    }

    private static ArtifactCoordinates toPomCoordinates(ArtifactCoordinates ac) {
        return new ArtifactCoordinates(ac.getGroupId(), ac.getArtifactId(), "pom", ac.getVersion(), ac.getClassifier());
    }

    protected static ArtifactCoordinates getArtifactCoordsFromPom(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return getArtifactCoordsFromPom(inputStream);
        } finally {
            closeQuietly(inputStream);
        }
    }

    protected static ArtifactCoordinates getArtifactCoordsFromPom(InputStream file) {
        DocumentBuilderFactory domFactory =
                DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        String groupId = null;
        String artifactId;
        String version = null;
        String packaging;
        try {
            builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
            xpath.setNamespaceContext(
                    new NamespaceContextImpl("http://maven.apache.org/POM/4.0.0",
                            namespaces));
            XPathExpression expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'groupId']");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            if (nodes.getLength() > 0)
                groupId = nodes.item(0).getTextContent();
            else {
                expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'parent']/*[local-name() = 'groupId']");
                result = expr.evaluate(doc, XPathConstants.NODESET);
                nodes = (NodeList) result;
                if (nodes.getLength() > 0)
                    groupId = nodes.item(0).getTextContent();
            }

            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'artifactId']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength() == 0) {
                log.error("Problem installing file: " + file + "\n" + " could not read artifactId");
                return null;
            }
            artifactId = nodes.item(0).getTextContent();

            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'version']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength() > 0)
                version = nodes.item(0).getTextContent();
            else {
                expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'parent']/*[local-name() = 'version']");
                result = expr.evaluate(doc, XPathConstants.NODESET);
                nodes = (NodeList) result;
                if (nodes.getLength() > 0)
                    version = nodes.item(0).getTextContent();
            }
            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'packaging']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength() > 0) {
                //special case: bundle is really a jar
                packaging = nodes.item(0).getTextContent();
                //TODO RKR move to ArtifactCoordinates
                if ("bundle".equals(packaging) || "maven-archetype".equals(packaging) || "maven-plugin".equals(packaging) || "eclipse-plugin".equals(packaging))
                    packaging = "jar";
            } else
                packaging = ArtifactCoordinates.DEFAULT_PACKAGING;

        } catch (Exception e) {
            log.error("Problem reading file: {}", file, e);
            return null;
        }

        return new ArtifactCoordinates(groupId, artifactId, packaging, version, null);
    }

    private void createMarker() {
        if (errorCount == 0) {
            String userName = System.getProperty("user.name");
            String markerFile = SorcerEnv.getHomeDir() + "/logs/" + MARKER_FILENAME + userName + MARKER_FILENAME_EXT;
            File f = new File(markerFile);
            try {
                if (!f.createNewFile())
                    log.warn("Couldn't create marker file {}", f);
            } catch (IOException e) {
                log.warn("Error while creating marker file {}", f, e);
            }
        }
        log.info("Installer finished with " + errorCount + " errors");
    }

    public static ZipEntry get(ZipFile zipFile, ZipEntryFilter filter) {
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            // if the entry is not directory and matches relative file then extract it
            if (filter.accept(entry))
                return entry;
        }
        return null;
    }

    /**
     * replaces filename extension to a provided one. Special case: if ext is null, remove '.' as well
     */
    private static File replaceExt(File file, String ext) {
        String path = file.getPath();
        int cut = path.lastIndexOf('.');
        if (ext != null)
            ++cut;
        String result = path.substring(0, cut);
        if (ext != null)
            result += ext;
        return new File(result);
    }
}

interface ZipEntryFilter {
    boolean accept(ZipEntry entry);
}
