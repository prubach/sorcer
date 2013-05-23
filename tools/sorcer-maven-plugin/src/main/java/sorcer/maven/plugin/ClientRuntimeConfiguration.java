package sorcer.maven.plugin;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import sorcer.core.SorcerConstants;
import sorcer.maven.util.JavaProcessBuilder;
import sorcer.util.JavaSystemProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal runtime configuration of a client (requestor) process
 *
 * @author Rafał Krupiński
 */
public class ClientRuntimeConfiguration extends ClientConfiguration {
	private List<String> websterRoots;

	public ClientRuntimeConfiguration(String mainClass, Collection<String> classpath) {
		this(mainClass, classpath.toArray(new String[classpath.size()]));
	}

	public ClientRuntimeConfiguration(String mainClass, String[] classpath) {
		super(mainClass, null, classpath);
	}

	public JavaProcessBuilder preconfigureProcess() {
		JavaProcessBuilder builder = new JavaProcessBuilder();

		builder.setMainClass(mainClass);
		builder.setClassPath(Arrays.asList(classpath));
		return builder;
	}

	public List<String> getWebsterRoots() {
		return websterRoots;
	}

	public void setWebsterRoots(List<String> websterRoots) {
		this.websterRoots = websterRoots;
	}

	public Map<String, String> getSystemProperties() {
		Map<String, String> result = new HashMap<String, String>();
		if (!ArrayUtils.isEmpty(codebase)) {
			result.put(JavaSystemProperties.JAVA_RMI_SERVER_CODEBASE, StringUtils.join(codebase, " "));
		}
		if (websterRoots != null && !websterRoots.isEmpty()) {
			result.put(SorcerConstants.WEBSTER_ROOTS, StringUtils.join(websterRoots, ";"));
		}
		return result;
	}
}
