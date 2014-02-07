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

/**
 * User: prubach
 * Date: 14.05.13
 * Time: 10:18
 */
public class Installer {
    final private static Logger log = LoggerFactory.getLogger(Installer.class);
    final private static Logger logger = log;

    private ArtifactResolver libResolver = new MappedFlattenedArtifactResolver(new File(SorcerEnv.getLibPath()));
    private ArtifactResolver mvnResolver = new RepositoryArtifactResolver(SorcerEnv.getRepoDir());

    protected Map<String, String> groupDirMap = new HashMap<String, String>();
    protected Map<String, String> versionsMap = new HashMap<String, String>();

    final private static String MARKER_FILENAME = "sorcer_jars_installed_user_";

    final private static String MARKER_FILENAME_EXT = ".tmp";

    final private static String COMMONS_LIBS = "commons";

    private static String VERSIONS_PROPS_FILE=SorcerEnv.getHomeDir() + File.separator + "configs" +
            File.separator + "groupversions.properties";

    private static String REPOLAYOUT_PROPS_FILE=SorcerEnv.getExtDir() + File.separator + "configs" +
            File.separator + "repolayout.properties";

    int errorCount = 0;

    private Map<String, ArtifactCoordinates> artifactsFromPoms = new HashMap<String, ArtifactCoordinates>();


    protected Installer() {
        String repoDir = SorcerEnv.getRepoDir();
        try {
            if (repoDir == null)
                throw new IOException("Repo Dir is null");
            else
                FileUtils.forceMkdir(new File(repoDir));
        } catch (IOException io) {
            logger.error("Problem installing jars to local maven repository - repository directory {} does not exist! ", repoDir, io);
            System.exit(-1);
        }
        String resourceName = "META-INF/maven/groupversions.properties";
        URL resourceVersions = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resourceVersions == null) {
            throw new RuntimeException("Could not find versions.properties");
        }

        resourceName = "META-INF/maven/repolayout.properties";
        URL resourceRepo = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resourceRepo == null) {
            throw new RuntimeException("Could not find repolayout.properties");
        }
        Properties propertiesRepo = new Properties();
        Properties propertiesVer = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = resourceRepo.openStream();
            propertiesRepo.load(inputStream);
            // properties is a Map<Object, Object> but it contains only Strings
            @SuppressWarnings("unchecked")
            Map<String, String> propertyMap = (Map) propertiesRepo;
            groupDirMap.putAll(propertyMap);

            File repoFile = new File(REPOLAYOUT_PROPS_FILE);
            if (repoFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(repoFile));
                propertyMap = (Map) props;
                 groupDirMap.putAll(propertyMap);
            }

            inputStream = resourceVersions.openStream();
            propertiesVer.load(inputStream);
            // properties is a Map<Object, Object> but it contains only Strings
            @SuppressWarnings("unchecked")
            Map<String, String> propertyMapVer = (Map) propertiesVer;
            versionsMap.putAll(propertyMapVer);

            File versionsFile = new File(VERSIONS_PROPS_FILE);
            if (versionsFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(versionsFile));
                propertyMapVer = (Map) props;
                versionsMap.putAll(propertyMapVer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load repolayout.properties", e);
        } finally {
            close(inputStream);
        }
    }

    public void install() throws IOException {
        installSigar();

        for (String group : groupDirMap.keySet()) {
            String dir = groupDirMap.get(group);
            String version = versionsMap.get(group);

            File libDir = new File(SorcerEnv.getLibPath(), dir);
            if (dir == null || version == null || !libDir.exists()) {
                logger.error("Problem installing jars for groupId: {} directory or version not specified: {} {}", group, dir, version);
                errorCount++;
                continue;
            }

            File[] jars = libDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });

            for (File jar : jars) {
                String fileNoExt = jar.getName().replace(".jar", "");
                ArtifactCoordinates ac = new ArtifactCoordinates(group, fileNoExt, version);
                File destJarFile = new File(mvnResolver.resolveAbsolute(ac));
                File artifactDir = destJarFile.getParentFile();
                try {
                    FileUtils.forceMkdir(artifactDir);
                    extractZipFile(jar, getInternalPomPath(ac), replaceExt(destJarFile, "pom"));
                    if (!destJarFile.exists() || ac.isVersionSnapshot())
                        FileUtils.copyFile(jar, destJarFile);
                } catch (IOException io) {
                    errorCount++;
                    logger.error("Problem installing jar: {} to: {}", fileNoExt, artifactDir);
                }
            }
        }
        // install commons
        File[] jars = new File(SorcerEnv.getLibPath(), COMMONS_LIBS).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("jar");
            }
        });

        for (File jar : jars) {
            ArtifactCoordinates ac = getArtifactCoordinatesForFile(jar);
            if (ac!=null && ac.getGroupId()!=null && ac.getArtifactId()!=null && ac.getVersion()!=null) {
                File destJarFile = new File(mvnResolver.resolveAbsolute(ac));
                File artifactDir = destJarFile.getParentFile();
                try {
                    FileUtils.forceMkdir(artifactDir);
                    extractZipFile(jar, getInternalPomPath(ac), replaceExt(destJarFile, "pom"));
                    if (!destJarFile.exists() || ac.isVersionSnapshot())
                        FileUtils.copyFile(jar, destJarFile);
                    logger.info("Installed jar and pom file: {}", destJarFile);
                } catch (IOException io) {
                    errorCount++;
                    logger.error("Problem installing jar: {} to: {}", ac.getArtifactId(), artifactDir, io);
                }
            }
        }
    }

    private void installSigar() {
        try {
            String group = "org.sorcersoft.sigar";
            String version = versionsMap.get(group);
            ArtifactCoordinates ac = new ArtifactCoordinates(group, "sigar-native", "zip", version, null);
            File destFile = new File(mvnResolver.resolveAbsolute(ac));
            File zipFile = new File(libResolver.resolveAbsolute(ac));
            if (!destFile.exists()) {
                logger.info("Installing zip file: " + zipFile + " to " + destFile);
                FileUtils.copyFile(zipFile, destFile);
            } else
                logger.info("File already exists " + destFile);
        } catch (IOException io) {
            ++errorCount;
            throw new IllegalStateException("Problem while copying sigar-native.zip to repo: ", io);
        }
    }

    public ArtifactCoordinates getArtifactCoordinatesForFile(File jar) throws IOException {
        String artifactIdFromFileName = jar.getName().replace(".jar", "");
        if (artifactsFromPoms.containsKey(artifactIdFromFileName)) {
            return artifactsFromPoms.get(artifactIdFromFileName);
        } else
            return getArtifactCoordinatesFromJar(jar);
    }

    public ArtifactCoordinates getArtifactCoordinatesFromJar(File jar) throws IOException {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(jar);
            ZipEntry pomProps = get(zipFile, new ZipEntryFilter() {
                @Override
                public boolean accept(ZipEntry entry) {
                    return (!entry.isDirectory() && entry.getName().contains("META-INF/maven/") && entry.getName().contains("pom.properties"));
                }
            });
            Properties properties = new Properties();
            properties.load(zipFile.getInputStream(pomProps));
            String fileName = jar.getName();
            String packaging = fileName.substring(fileName.lastIndexOf('.') + 1);

            return new ArtifactCoordinates(properties.getProperty("groupId"), properties.getProperty("artifactId"), packaging, properties.getProperty("version"), null);
        } finally {
            //also closes input streams
            IOUtils.closeQuietly(zipFile);
        }
    }


    public void installPoms() {
        String pomDir = SorcerEnv.getHomeDir() + "/configs/poms/";

        File[] poms = new File(pomDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("pom");
            }
        });
        for (File pom : poms) {
                ArtifactCoordinates ac = getArtifactCoordsFromPom(pom.getAbsolutePath());
                if (ac!=null) {
                    artifactsFromPoms.put(ac.getArtifactId(), ac);
                    File destFile = new File(mvnResolver.resolveAbsolute(ac));
                    File artifactDir = destFile.getParentFile();
                    try {
                        FileUtils.forceMkdir(artifactDir);
                        if (!destFile.exists() || ac.isVersionSnapshot())
                            FileUtils.copyFile(pom, destFile);
                        logger.info("Installed pom file: " + destFile);
                    } catch (IOException io) {
                        errorCount++;
                        logger.error("Problem installing pom file: " + pom.getAbsolutePath() + " to: " + artifactDir);
                    }
                } else
                    errorCount++;
        }
    }

    public static void main(String[] args) throws IOException {
        Installer installer = new Installer();
        installer.installPoms();
        installer.install();
        installer.createMarker();
    }

    protected void install(ArtifactCoordinates coordinates, File pom){}

    protected static ArtifactCoordinates getArtifactCoordsFromPom(String fileName) {
        DocumentBuilderFactory domFactory =
                DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String packaging = null;
        try {
            builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(fileName);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
            xpath.setNamespaceContext(
                    new NamespaceContextImpl("http://maven.apache.org/POM/4.0.0",
                            namespaces));
            XPathExpression expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'groupId']");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            if (nodes.getLength()>0)
                groupId = nodes.item(0).getTextContent();
            else {
                expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'parent']/*[local-name() = 'groupId']");
                result = expr.evaluate(doc, XPathConstants.NODESET);
                nodes = (NodeList) result;
                if (nodes.getLength()>0)
                    groupId = nodes.item(0).getTextContent();
            }

            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'artifactId']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength()==0) {
                logger.error("Problem installing file: " + fileName + "\n" + " could not read artifactId");
                return null;
            }
            artifactId = nodes.item(0).getTextContent();

            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'version']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength()>0)
                version = nodes.item(0).getTextContent();
            else {
                expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'parent']/*[local-name() = 'version']");
                result = expr.evaluate(doc, XPathConstants.NODESET);
                nodes = (NodeList) result;
                if (nodes.getLength()>0)
                    version = nodes.item(0).getTextContent();
            }
            expr = xpath.compile("/*[local-name() = 'project']/*[local-name() = 'packaging']");
            result = expr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
            if (nodes.getLength()>0)
                packaging = nodes.item(0).getTextContent();
            else
                packaging = ArtifactCoordinates.DEFAULT_PACKAGING;

        } catch (Exception e) {
            logger.error("Problem installing file: " + fileName + "\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return new ArtifactCoordinates(groupId, artifactId, packaging, version, null);
    }

    private void createMarker() {
        if (errorCount==0) {
            String userName = System.getProperty("user.name");
            String markerFile = SorcerEnv.getHomeDir() + "/logs/" + MARKER_FILENAME + userName + MARKER_FILENAME_EXT;
            File f = new File(markerFile);
            try {
                f.createNewFile();
            } catch (IOException e) {
            }
        }
        logger.info("Installer finished with " + errorCount + " errors");
    }


    protected void close(Closeable inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void extractZipFile(File zipFileSrc, String relativeFilePath, File targetFilePath) {
        try {
            // Do not overwrite
            if (!targetFilePath.exists()) {
                ZipFile zipFile = new ZipFile(zipFileSrc);
                Enumeration<? extends ZipEntry> e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    // if the entry is not directory and matches relative file then extract it
                    if (!entry.isDirectory() && entry.getName().equals(relativeFilePath)) {
                        InputStream bis = new BufferedInputStream(
                                zipFile.getInputStream(entry));

                        // write the inputStream to a FileOutputStream
                        OutputStream outputStream =
                                new FileOutputStream(targetFilePath);

                        int read = 0;
                        byte[] bytes = new byte[1024];

                        while ((read = bis.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        bis.close();
                        outputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IOError :", e);
            e.printStackTrace();
        }
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

    private static String getInternalPomPath(ArtifactCoordinates ac) {
        return "META-INF/maven/" + ac.getGroupId() + "/" + ac.getArtifactId() + "/pom.xml";
    }

    private static File replaceExt(File file, String ext) {
        String path = file.getPath();
        return new File(path.substring(0, path.lastIndexOf('.') + 1) + ext);
    }

    private static ArtifactCoordinates coordsForPom(ArtifactCoordinates ac){
        return new ArtifactCoordinates(ac.getGroupId(),ac.getArtifactId(),"pom",ac.getVersion(),ac.getClassifier());
    }

}

interface ZipEntryFilter {
    boolean accept(ZipEntry entry);
}
