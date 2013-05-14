package sorcer.maven.plugin;

import java.io.File;
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
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;

import sorcer.maven.util.ArtifactUtil;

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

	@Parameter
	protected File providerPath;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Properties props = project.getProperties();
		if (!isPomProject()) {
			props.putAll(project.getParent().getProperties());
			providerName = props.getProperty(KEY_PROVIDER_NAME);
		} else {
			props.setProperty(KEY_PROVIDER_NAME, providerName);
			props.setProperty(KEY_REQUESTOR, requestor);
			props.setProperty(KEY_PROVIDER, provider);
			// props.setProperty(KEY_PROVIDER_PATH, providerPath.getPath());
			props.put(KEY_CLASSPATH, Arrays.asList(api, provider, proxy));
			props.put(KEY_CODEBASE, Arrays.asList(api, proxy, sui));
		}
		assertNotEmpty("providerName", providerName);
	}

	// if we're run on a pom project, assume it's a provider root
	// otherwise, asume we're in a prv module
	private void configure() throws MojoExecutionException {

		MavenProject mainProject = isPomProject() ? project : project.getParent();

		if (providerName == null) {
			providerName = mainProject.getArtifactId();
		}

		api = resolveModule(mainProject, api, "api");

	}

	private String resolveModule(MavenProject project, String userData, String role) {
		String name = userData != null ? userData : project.getArtifactId() + "-" + role;
		if (project.getModules().contains(name)) {

			return name;
		}
		return null;
	}

	private Artifact readParent() throws MojoExecutionException {
		ArtifactDescriptorResult artifactDescriptorResult;
		try {
			artifactDescriptorResult = repositorySystem.readArtifactDescriptor(repositorySystemSession,
					new ArtifactDescriptorRequest(ArtifactUtil.toAetherArtifact(project.getParentArtifact()),
							remoteRepositories, "initialize"));
		} catch (ArtifactDescriptorException e) {
			getLog().info("could not read parent project");
			return null;
		}
		return resolveArtifact(artifactDescriptorResult.getArtifact());
	}

	private void assertNotEmpty(String name, String value) throws MojoExecutionException {
		if (StringUtils.isNotBlank(value)) {
			getLog().info(name + " = " + providerName);
		} else {
			getLog().warn("Empty " + name);
		}
	}

	public boolean isPomProject() {
		return project.getPackaging().equals("pom");
	}
}
