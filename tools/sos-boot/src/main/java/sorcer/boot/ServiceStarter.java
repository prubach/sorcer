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

import com.sun.jini.start.LifeCycle;
import net.jini.config.ConfigurationException;
import sorcer.core.DestroyAdmin;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.util.JavaSystemProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter {
	public static final String CONFIG_RIVER = "config/services.config";
	private static final String SUFFIX_RIVER = ".config";
	final public static String SORCER_DEFAULT_CONFIG = new File(SorcerEnv.getHomeDir(), "configs/sorcer-boot.config").getPath();

	public static void main(String[] args) throws Exception {
		new ServiceStarter().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
		loadDefaultProperties();
		List<String> configs = new LinkedList<String>();
		for (String arg : args) {
			if (isConfigFile(args[0])) {
				configs.add(arg);
			} else {
				configs.add(findConfigUrl(arg).toExternalForm());
			}
		}
		if (configs.isEmpty()) {
			configs.add(SORCER_DEFAULT_CONFIG);
		}
		start(configs);
	}

	private void loadDefaultProperties() {
		String sorcerHome = SorcerEnv.getHomeDir().getPath();
		setDefaultProperty(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb.sos");
		setDefaultProperty(JavaSystemProperties.UTIL_LOGGING_CONFIG_FILE, sorcerHome + "/configs/sorcer.logging");
		setDefaultProperty(SorcerConstants.S_KEY_SORCER_ENV, sorcerHome + "/configs/sorcer.env");
	}

	private void setDefaultProperty(String key, String value) {
		String userValue = System.getProperty(key);
		if (userValue == null) {
			System.setProperty(key, value);
		}
	}

	private boolean isConfigFile(String path) {
		return path.endsWith(SUFFIX_RIVER);
	}

	private List<Object> rioServices = new LinkedList<Object>();

	/**
	 * Start services from the configs
	 *
	 * @param configs file path or URL of the services.config configuration
	 */
	public void start(Collection<String> configs) throws ConfigurationException {
		sorcer.com.sun.jini.start.ServiceStarter serviceStarter = new sorcer.com.sun.jini.start.ServiceStarter();
		serviceStarter.startServicesFromPaths(configs.toArray(new String[configs.size()]));
	}

	/**
	 * This unused method is left for further reference
	 */
	public void stop() {
		Class<com.sun.jini.start.ServiceStarter> c = com.sun.jini.start.ServiceStarter.class;
		try {
			Field servicesField = c.getDeclaredField("transient_service_refs");
			if (!servicesField.isAccessible()) {
				servicesField.setAccessible(true);
			}
			List riverServices = (List) servicesField.get(null);
			List<Object> allServices = new ArrayList<Object>(riverServices);
			allServices.addAll(this.rioServices);
			for (Object o : allServices) {
				if (o instanceof DestroyAdmin) {
					((DestroyAdmin) o).destroyNode();
				} else if (o instanceof LifeCycle) {
					((LifeCycle) o).unregister(o);
				}
			}
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Unsupported Apache River version (expected field is missing)", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not access river services", e);
		} catch (RemoteException e) {
			throw new RuntimeException("Unknown error", e);
		}

	}

	private URL findConfigUrl(String path) throws IOException {
		File configFile = new File(path);

		if (!configFile.exists()) {
			throw new FileNotFoundException(path);
		} else if (configFile.isDirectory()) {
			return new File(configFile, CONFIG_RIVER).toURI().toURL();
		} else if (path.endsWith(".jar")) {
			ZipEntry entry = new ZipFile(path).getEntry(CONFIG_RIVER);
			if (entry != null) {
				return new URL(String.format("jar:file:%1$s!/%2$s", path, CONFIG_RIVER));
			}
		}
		throw new FileNotFoundException(CONFIG_RIVER);
	}
}
