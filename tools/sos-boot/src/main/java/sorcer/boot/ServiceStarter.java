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
package sorcer.boot;

import net.jini.config.EmptyConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.ServiceElement;
import org.rioproject.resolver.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;
import sorcer.util.IOUtils;
import sorcer.util.JavaSystemProperties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static sorcer.provider.boot.AbstractServiceDescriptor.Created;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter {
    final private static Logger log = LoggerFactory.getLogger(ServiceStarter.class);
	public static final String CONFIG_RIVER = "config/services.config";
    private static final String SUFFIX_RIVER = "config";
    final public static File SORCER_DEFAULT_CONFIG = new File(SorcerEnv.getHomeDir(), "configs/sorcer-boot.config");
    private sorcer.com.sun.jini.start.ServiceStarter riverServiceStarter = new sorcer.com.sun.jini.start.ServiceStarter();

	public static void main(String[] args) throws Exception {
		new ServiceStarter().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
		loadDefaultProperties();
        List<String> configs = new LinkedList<String>(Arrays.asList(args));
		if (configs.isEmpty()) {
            configs.add(SORCER_DEFAULT_CONFIG.getPath());
		}
		start(configs);
	}

	private void loadDefaultProperties() {
		String sorcerHome = SorcerEnv.getHomeDir().getPath();
		setDefaultProperty(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
		setDefaultProperty(JavaSystemProperties.UTIL_LOGGING_CONFIG_FILE, sorcerHome + "/configs/sorcer.logging");
		setDefaultProperty(SorcerConstants.S_KEY_SORCER_ENV, sorcerHome + "/configs/sorcer.env");
	}

	private void setDefaultProperty(String key, String value) {
		String userValue = System.getProperty(key);
		if (userValue == null) {
			System.setProperty(key, value);
		}
	}

	/**
	 * Start services from the configs
	 *
	 * @param configs file path or URL of the services.config configuration
	 */
    public void start(Collection<String> configs) throws Exception {
        List<String> riverServices = new LinkedList<String>();
        List<File> cfgJars = new LinkedList<File>();
        System.setSecurityManager(new RMISecurityManager());
        for (String path : configs) {
            File file = null;
            if (path.startsWith(":")) {
                file = findArtifact(path.substring(1));
            } else if (Artifact.isArtifact(path))
                file = new File(Resolver.resolveAbsolute(path));
            if (file == null) file = new File(path);

            IOUtils.ensureFile(file, IOUtils.FileCheck.readable);
            path = file.getPath();
            String ext = path.substring(path.lastIndexOf('.') + 1);

            if (file.isDirectory())
                riverServices.add(findConfigUrl(path).toExternalForm());
            else if (SUFFIX_RIVER.equals(ext))
                riverServices.add(path);
            else if ("oar".equals(ext) || "jar".equals(ext))
                cfgJars.add(file);
        }
        if (!riverServices.isEmpty())
            riverServiceStarter.startServicesFromPaths(riverServices.toArray(new String[configs.size()]));
        if (!cfgJars.isEmpty())
            startConfigJars(cfgJars);
    }

    private File findArtifact(String artifactId) {
        Collection<File> files = FileUtils.listFiles(new File(System.getProperty("user.dir")), new ArtifactIdFileFilter(artifactId), DirectoryFileFilter.INSTANCE);
        if (files.size() == 0) {
            log.error("Artifact file {} not found", artifactId);
            return null;
        }
        if (files.size() > 1) {
            log.warn("Found {} files possibly matching artifactId, using the first", files.size());
        }
        return files.iterator().next();
    }

    protected void startConfigJars(Collection<File> files) throws Exception {
        for (File file : files) {
            createServices(file);
        }
    }

    private List<Created> createServices(File file) throws Exception {
        SorcerOAR oar = new SorcerOAR(file);
        OperationalString[] operationalStrings = oar.loadOperationalStrings();
        URL policyFile = oar.getPolicyFile();
        URL oarUrl = file.toURI().toURL();
        List<Created> result = new LinkedList<Created>();

        for (OperationalString op : operationalStrings) {
            for (ServiceElement se : op.getServices()) {
                result.add(new OpstringServiceDescriptor(se, oarUrl, policyFile).create(EmptyConfiguration.INSTANCE));
            }
        }
        return result;
    }

    private URL findConfigUrl(String path) throws IOException {
        File configFile = new File(path);
        if (configFile.isDirectory()) {
            return new File(configFile, CONFIG_RIVER).toURI().toURL();
        } else if (path.endsWith(".jar")) {
            ZipEntry entry = new ZipFile(path).getEntry(CONFIG_RIVER);
            if (entry != null) {
                return new URL(String.format("jar:file:%1$s!/%2$s", path, CONFIG_RIVER));
            }
        }
        return new File(path).toURI().toURL();
    }

    private static class ArtifactIdFileFilter extends AbstractFileFilter {
        private String prefix;

        public ArtifactIdFileFilter(String prefix) {
            this.prefix = prefix + "-";
        }

        @Override
        public boolean accept(File dir, String name) {
            return "target".equals(dir.getName()) && name.startsWith(prefix) && name.endsWith(".jar");
        }
	}
}
