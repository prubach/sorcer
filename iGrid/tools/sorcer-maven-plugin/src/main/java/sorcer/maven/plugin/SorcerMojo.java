package sorcer.maven.plugin;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
@Mojo(name = "sorcer", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON, defaultPhase = LifecyclePhase.INITIALIZE)
public class SorcerMojo extends AbstractMojo {
	private static final String SERVICE_SUFFIX = "-prv";

	@Component
	protected ArtifactResolver artifactResolver;

	@Component
	protected RepositorySystem repoSystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	protected List<RemoteRepository> remoteRepos;

	@Parameter(defaultValue = "${session}")
	protected MavenSession mavenSession;

	/**
	 * Provider name, by default it's artifactId without '-server' part
	 */
	@Parameter
	protected String providerName;

	/**
	 * API artifact, by default it's ${groupId}:${providerName}-api if such
	 * artifact is declared in the dependencies section. The artifact with its dependencies create providers classpath
	 */
	@Parameter
	protected org.apache.maven.model.Dependency api;

	/**
	 * proxy artifact, by default it's ${groupId}:${providerName}-proxy if such
	 * artifact is declared in the dependencies section. This and 'ui' artifacts
	 * (if present) create providers codebase
	 */
	@Parameter
	protected org.apache.maven.model.Dependency proxy;

	/**
	 * UI artifact, by default it's ${groupId}:${providerName}-ui if such
	 * artifact is declared in the dependencies section
	 */
	@Parameter
	protected org.apache.maven.model.Dependency ui;

	/**
	 * Extra entries to be put in the codebase. Proxy and UI are added automatically Each artifact must be mentioned in the dependencies section.
	 */
	@Parameter
	protected org.apache.maven.model.Dependency[] codebase;

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	protected Artifact artifact;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		artifact = getArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(), project.getVersion());
		resolveProviderName();
		try {
			resolveProviderDependencies();
		} catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (RepositoryException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void resolveProviderName() throws MojoFailureException {
		if (StringUtils.isNotBlank(providerName)) {
			return;
		}
		String artifactId = project.getArtifactId();
		if (!artifactId.endsWith(SERVICE_SUFFIX)) {
			throw new MojoFailureException("Undefined 'providerName'");
		}
		providerName = artifactId.substring(0, artifactId.length() - SERVICE_SUFFIX.length());
		getLog().debug("providerName = " + providerName);
	}

	private void resolveProviderDependencies() throws MojoFailureException, MojoExecutionException, RepositoryException, AbstractArtifactResolutionException {
		Artifact proxyArtifact = resolveProviderDependency(proxy, "proxy");
		Artifact uiArtifact = resolveProviderDependency(ui, "ui");

		List<Artifact> codeBaseArtifacts = new LinkedList<Artifact>();
		if (proxyArtifact != null) codeBaseArtifacts.add(proxyArtifact);
		if (uiArtifact != null) codeBaseArtifacts.add(uiArtifact);

		if (this.codebase != null) {
			for (org.apache.maven.model.Dependency codeBaseDependency : codebase) {
				Artifact dependencyArtifact = resolveProviderDependency(codeBaseDependency, project.getGroupId(), null);
				if (dependencyArtifact != null) {
					codeBaseArtifacts.add(dependencyArtifact);
				}
			}
		}

		String codebase = createClasspath(codeBaseArtifacts);
		getLog().info("CodeBase = " + codebase);
		mavenSession.getCurrentProject().getProperties().setProperty("provider.codebase", codebase);
	}

	protected Artifact resolveProviderDependency(org.apache.maven.model.Dependency userValue, String type) throws AbstractArtifactResolutionException, RepositoryException, MojoExecutionException, MojoFailureException {

		String groupId = project.getGroupId();
		String artifactId = providerName + "-" + type;
		return resolveProviderDependency(userValue, groupId, artifactId);

	}

	private Artifact resolveProviderDependency(org.apache.maven.model.Dependency dependency, String defaultGroupId, String defaultArtifactId) throws MojoExecutionException, MojoFailureException {
		if (dependency != null) {
			String userGroupId = dependency.getGroupId();
			if (userGroupId != null) {
				defaultGroupId = userGroupId;
			}
			String userArtifactId = dependency.getArtifactId();
			if (userArtifactId != null) {
				defaultArtifactId = userArtifactId;
			}
		}

		for (org.apache.maven.model.Dependency dep : project.getDependencies()) {
			if (defaultGroupId.equals(dep.getGroupId()) && defaultArtifactId.equals(dep.getArtifactId()))
				return getArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getType(), dep.getVersion());
		}

		if (dependency == null) {
			return null;
		} else {
			throw new MojoFailureException(dependency + "not found");
		}
	}

	private Artifact getArtifact(String groupId, String artifactId, String extension, String version) throws MojoExecutionException {
		try {
			return artifactResolver.resolveArtifact(repoSession, new ArtifactRequest(new DefaultArtifact(groupId, artifactId, extension, version), remoteRepos, null)).getArtifact();
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

	private List<Dependency> toDependencies(Collection<Artifact> artifacts, String scope) {
		List<Dependency> result = new ArrayList<Dependency>(artifacts.size());
		for (Artifact a : artifacts) {
			result.add(new Dependency(a, scope));
		}
		return result;
	}

	private String createClasspath(Collection<Artifact> artifacts)
			throws MojoExecutionException, RepositoryException {


		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setDependencies(toDependencies(artifacts, "provided"));
		collectRequest.setRepositories(remoteRepos);

		DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, new DependencyRequest(collectRequest, null));

		Iterable<String> paths = Iterables.transform(dependencyResult.getArtifactResults(), new Function<ArtifactResult, String>() {
			@Nullable
			@Override
			public String apply(@Nullable ArtifactResult input) {
				return input == null ? null : relativePath(input.getArtifact().getFile());
			}
		});

		return StringUtils.join(paths.iterator(), " ");

	}

	private String relativePath(File input) {
		String absolutePath = input.getAbsolutePath();
		return absolutePath.substring(mavenSession.getLocalRepository().getBasedir().length(), absolutePath.length());
	}
}
