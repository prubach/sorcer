package sorcer.maven.plugin;

import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Rafał Krupiński
 */
@Mojo(name = "initialize", aggregator = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON, defaultPhase = LifecyclePhase.INITIALIZE)
public class InitializeMojo extends AbstractSorcerMojo {
	/**
	 * Provider name, by default it's artifactId
	 */
	@Parameter(defaultValue = "${project.artifactId}", required = true)
	protected String providerName;

	/**
	 * API artifact, by default it's ${groupId}:${artifactId}-api if such
	 * artifact is declared in the dependencies section. The artifact with its
	 * dependencies create providers classpath
	 */
	@Parameter(defaultValue = "${project.groupId}:${project.artifactId}-api:${project.version}")
	protected String api;

	/**
	 * proxy artifact, by default it's ${groupId}:${providerName}-proxy if such
	 * artifact is declared in the dependencies section. This and 'ui' artifacts
	 * (if present) create providers codebase
	 */
	@Parameter(defaultValue = "${project.groupId}:${project.artifactId}-proxy:${project.version}")
	protected String proxy;

	/**
	 * Service UI artifact, by default it's ${groupId}:${providerName}-sui if
	 * such artifact is declared in the providers dependencies section
	 */
	@Parameter(defaultValue = "${project.groupId}:${project.artifactId}-sui:${project.version}")
	protected String sui;

	@Parameter(defaultValue = "${project.groupId}:${project.artifactId}-prv:${project.version}")
	protected String provider;

	@Parameter(defaultValue = "${project.groupId}:${project.artifactId}-req:${project.version}")
	protected String requestor;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MavenProject parent = project.getParent();
		if (parent != null && parent.getProperties().containsKey(KEY_PROVIDER_NAME)) {
			getLog().warn("providerName already set; skipping mojo");
			return;
		}
		assertNotEmpty("providerName", providerName);

		Properties props = project.getProperties();
		props.put(KEY_PROVIDER_NAME, providerName);
		props.put(KEY_CLASSPATH, Arrays.asList(api, provider, proxy));
		props.put(KEY_CODEBASE, Arrays.asList(api, proxy, sui));
		props.put(KEY_CODEBASE_REQUESTOR, Arrays.asList(api));
		props.put(KEY_REQUESTOR, requestor);
		props.put(KEY_PROVIDER, provider);
	}

	private void assertNotEmpty(String name, String value) throws MojoExecutionException {
		if (StringUtils.isBlank(value)) {
			throw new MojoExecutionException("Undefined '" + name + "'");
		}
		getLog().info("providerName = " + providerName);
	}

}
