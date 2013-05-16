/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: prubach
 * Date: 14.05.13
 * Time: 10:18
 */
public class Installer {
    protected Map<String, String> groupDirMap = new HashMap<String, String>();
    protected Map<String, String> versionsMap = new HashMap<String, String>();

    private static String repoDir;

    protected static final Logger logger = Logger.getLogger(Installer.class.getName());

    {
        try {
            repoDir = Resolver.getRepoDir();
            if (repoDir == null)                          {
                throw new IOException("Repo Dir is null");
            }
            else
                FileUtils.forceMkdir(new File(repoDir));
        } catch (IOException io) {
            logger.severe("Problem installing jars to local maven repository - repository directory does not exist! " + io.getMessage());
            System.exit(-1);
        }
        String resourceName = "META-INF/maven/versions.properties";
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

            inputStream = resourceVersions.openStream();
            propertiesVer.load(inputStream);
            // properties is a Map<Object, Object> but it contains only Strings
            @SuppressWarnings("unchecked")
            Map<String, String> propertyMapVer = (Map) propertiesVer;
            versionsMap.putAll(propertyMapVer);
        } catch (IOException e) {
            throw new RuntimeException("Could not load repolayout.properties", e);
        } finally {
            close(inputStream);
        }
    }

    public void install() {
        for (String group : groupDirMap.keySet()) {
            // Ignore Sigar
            if (group.equals("org.sorcersoft.sigar")) continue;

            String dir = groupDirMap.get(group);
            String version = versionsMap.get(group);

            if (dir == null || version == null || !new File(Resolver.getRootDir() + "/" + dir).exists()) {
                logger.severe("Problem installing jars for groupId: " + group + " directory or version not specified: " + dir + " " + version);
                continue;
            }
            File[] jars = new File(Resolver.getRootDir() + "/" + dir).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith("jar"))
                        return true;
                    return false;
                }
            });

            for (File jar : jars) {
                String fileNoExt = jar.getName().replace(".jar", "");
                String artifactDir = Resolver.getRepoDir() + "/" + group.replace(".", "/") + "/" + fileNoExt + "/" + version;
                try {
                    FileUtils.forceMkdir(new File(artifactDir));
                    extractZipFile(jar, "META-INF/maven/" + group + "/" + fileNoExt + "/pom.xml",
                            artifactDir + "/" + fileNoExt + "-" + version + ".pom");
                    FileUtils.copyFile(jar, new File(artifactDir, fileNoExt + "-" + version + ".jar"));
                } catch (IOException io) {
                    logger.severe("Problem installing jar: " + fileNoExt + " to: " + artifactDir);
                }
            }
        }
    }


    public void installPoms() {

        String pomDir = SorcerEnv.getHomeDir() + "/configs/poms/";

        File[] jars = new File(pomDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith("pom"))
                    return true;
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        String group = "org.sorcersoft.sorcer";
        for (File jar : jars) {
                String fileNoExt = jar.getName().replace("-" + SorcerEnv.getSorcerVersion() + ".pom", "");
                String artifactDir = Resolver.getRepoDir() + "/" + group.replace(".", "/") + "/" + fileNoExt + "/" + SorcerEnv.getSorcerVersion();
                try {
                    FileUtils.forceMkdir(new File(artifactDir));
                    FileUtils.copyFile(jar, new File(artifactDir, jar.getName()));
                } catch (IOException io) {
                    logger.severe("Problem installing jar: " + fileNoExt + " to: " + artifactDir);
                }
        }
    }

    public static void main(String[] args) {
        Installer installer = new Installer();
        installer.install();
        installer.installPoms();
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

    public static void extractZipFile(File zipFileSrc, String relativeFilePath, String targetFilePath) {
        try {
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
                            new FileOutputStream(new File(targetFilePath));

                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = bis.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    bis.close();
                    outputStream.close();
                } else {
                    continue;
                }
            }
        } catch (IOException e) {
            logger.severe("IOError :" + e);
            e.printStackTrace();
        }
    }
}
