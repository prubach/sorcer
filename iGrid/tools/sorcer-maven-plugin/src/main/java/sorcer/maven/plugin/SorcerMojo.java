package sorcer.maven.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

import sorcer.maven.util.ArtifactResultTransformer;
import sorcer.maven.util.ArtifactUtil;

import com.google.common.collect.Collections2;
import com.jcabi.aether.Aether;

/**
 * @author Rafał Krupiński
 */
//@Mojo(name = "provider", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON, defaultPhase = LifecyclePhase.INITIALIZE)
public class SorcerMojo extends AbstractSorcerMojo {
	@Component
	protected RepositorySystem repoSystem;

	@Component
	protected ProjectDependenciesResolver projectDependenciesResolver;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	protected List<RemoteRepository> remoteRepos;

	@Parameter(defaultValue = "${session}")
	protected MavenSession mavenSession;

	/**
	 * Extra entries to be put in the codebase. Proxy and UI are added
	 * automatically Each artifact must be mentioned in the dependencies
	 * section.
	 */
	@Parameter
	protected org.apache.maven.model.Dependency[] codebase;

	protected Artifact artifact;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Aether aether = createAether();

		artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(),
				project.getVersion());
		try {
			resolveProviderDependencies(aether);
		} catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (RepositoryException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void resolveProviderDependencies(Aether aether) throws MojoFailureException, MojoExecutionException,
			RepositoryException, AbstractArtifactResolutionException, DependencyResolutionException {
		String codebase = resolveCodeBase(aether);
		getLog().info(codebase);
		mavenSession.getCurrentProject().getProperties().setProperty("provider.codebase", codebase);

		String classpath = resolveClassPath(aether);
		getLog().info(classpath);
		mavenSession.getCurrentProject().getProperties().setProperty("provider.classpath", classpath);
	}

	protected String resolveCodeBase(Aether aether) throws AbstractArtifactResolutionException, RepositoryException,
			MojoExecutionException, MojoFailureException {

		Set<Artifact> codeBaseArtifacts = new HashSet<Artifact>();

		for (String coords : (List<String>) project.getParent().getProperties().get(KEY_CODEBASE)) {
			List<Artifact> resolve = aether.resolve(new DefaultArtifact(coords), JavaScopes.RUNTIME);
			codeBaseArtifacts.addAll(resolve);
		}

		if (this.codebase != null) {
			for (org.apache.maven.model.Dependency codeBaseDependency : codebase) {
				Artifact dependencyArtifact = resolveProjectDependency(codeBaseDependency, project.getGroupId(), null);
				if (dependencyArtifact != null) {
					codeBaseArtifacts.add(dependencyArtifact);
				}
			}
		}
		return buildClassPathString(getDependencies(codeBaseArtifacts, JavaScopes.RUNTIME), "resolveCodeBase");
	}

	protected String resolveClassPath(Aether aether)
			throws org.sonatype.aether.resolution.DependencyResolutionException {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		for (String coords : (List<String>) project.getParent().getProperties().get(KEY_CLASSPATH)) {
			artifacts.addAll(aether.resolve(new DefaultArtifact(coords), JavaScopes.COMPILE));
		}
		return buildClassPathString(artifacts, "resolveClassPath");
	}

	private String buildClassPathString(Collection<Artifact> dependencies, String resolveMethod) {
		StringBuilder result = new StringBuilder("sorcer.resolver.Resolver.").append(resolveMethod).append(
				"( new sorcer.util.ArtifactCoordinates[]{");

		boolean coma = false;
		for (Artifact dependency : dependencies) {
			if (coma) {
				result.append(',');
			} else {
				coma = true;
			}
			result.append("\n\t\t\t\tsorcer.util.ArtifactCoordinates.coords(\"").append(ArtifactUtil.key(dependency))
					.append("\")");
		}
		result.append("\n\t\t\t})");
		return result.toString();
	}

	private Artifact resolveProjectDependency(org.apache.maven.model.Dependency dependency, String defaultGroupId,
			String defaultArtifactId) throws MojoExecutionException, MojoFailureException {
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
				return new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getType(), dep.getVersion());
		}

		if (dependency == null) {
			return null;
		} else {
			throw new MojoFailureException(dependency + " not found");
		}
	}

	private List<Dependency> toDependencies(Collection<Artifact> artifacts, String scope) {
		List<Dependency> result = new ArrayList<Dependency>(artifacts.size());
		for (Artifact a : artifacts) {
			result.add(new Dependency(a, scope));
		}
		return result;
	}

	private Collection<Artifact> getDependencies(Collection<Artifact> artifacts, String scope)
			throws MojoExecutionException, RepositoryException {
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setDependencies(toDependencies(artifacts, scope));
		collectRequest.setRepositories(remoteRepos);

		DependencyResult dependencyResult = repoSystem.resolveDependencies(repositorySystemSession, new DependencyRequest(
				collectRequest, DependencyFilterUtils.classpathFilter(scope)));

		return Collections2.transform(dependencyResult.getArtifactResults(), new ArtifactResultTransformer());
	}

}
