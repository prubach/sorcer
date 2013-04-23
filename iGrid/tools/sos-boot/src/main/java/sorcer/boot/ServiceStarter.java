package sorcer.boot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.rioproject.start.ServiceStarter.ServiceReference;
import sorcer.core.Destroyer;
import sorcer.core.SorcerConstants;

import com.sun.jini.start.LifeCycle;

/**
 * @author Rafał Krupiński
 */
public class ServiceStarter {
	public static final String CONFIG_RIVER = "META-INF/sorcer/services.config";
	public static final String CONFIG_RIO = "META-INF/sorcer/services.groovy";
	private static final String SUFFIX_RIVER = ".config";
	private static final String SUFFIX_RIO = ".groovy";

	public static void main(String[] args) throws IOException {
		new ServiceStarter().doMain(args);
	}

	private void doMain(String[] args) throws IOException {
		loadDefaultProperties();
		String config;
		if (args.length != 0) {
			if (isConfigFile(args[0])) {
				config = args[0];
			} else {
				config = findConfigUrl(args[0]).toExternalForm();
			}
		} else {
			config = CONFIG_RIO;
		}
		start(config);
	}

	private void loadDefaultProperties() {
		String sorcerHome = System.getProperty(SorcerConstants.SORCER_HOME);
		setDefaultProperty("org.rioproject.resolver.prune.platform", "false");
		setDefaultProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb.sos");
		setDefaultProperty("java.util.logging.config.file",  sorcerHome + "/configs/sorcer.logging");
		setDefaultProperty("sorcer.env.file",  sorcerHome + "/configs/sorcer.env");
	}

	private void setDefaultProperty(String key, String value) {
		String userValue = System.getProperty(key);
		if (userValue == null) {
			System.setProperty(key, value);
		}
	}

	private boolean isConfigFile(String path) {
		return path.endsWith(SUFFIX_RIVER) || path.endsWith(SUFFIX_RIO);
	}

	private List<Object> rioServices = new LinkedList<Object>();

	/**
	 * Start services from the config
	 *
	 * @param config
	 *            file path or URL of the services.config configuration
	 */
	public void start(String config) {
		if (config.endsWith(CONFIG_RIO)) {
			startRio(config);
		} else {
			com.sun.jini.start.ServiceStarter.main(new String[] { config });
		}
	}

	private void startRio(String config) {
		List<ServiceReference> rioServices = null;
		try {
			rioServices = org.rioproject.start.ServiceStarter.start(config);
		} catch (Exception e) {
			throw new RuntimeException("Problem starting Rio", e);
		}
		for (ServiceReference rioService : rioServices) {
			this.rioServices.add(rioService.impl);
		}
	}

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
				if (o instanceof Destroyer) {
					((Destroyer) o).destroyNode();
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
