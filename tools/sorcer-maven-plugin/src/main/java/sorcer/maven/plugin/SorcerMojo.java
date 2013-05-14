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
package sorcer.maven.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

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
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.DependencyFilterUtils;
import org.sonatype.aether.util.filter.ExclusionsDependencyFilter;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import sorcer.util.ArtifactResultTransformer;
import sorcer.util.ArtifactUtil;

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

	@Component
	protected ProjectDependenciesResolver projectDependenciesResolver;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	protected List<RemoteRepository> remoteRepos;

	@Parameter(defaultValue = "${session}")
	protected MavenSession mavenSession;

	/**
	 * Provider name, by default it's artifactId without '-prv' part
	 */
	@Parameter
	protected String providerName;

	/**
	 * API artifact, by default it's ${groupId}:${providerName}-api if such
	 * artifact is declared in the dependencies section. The artifact with its
	 * dependencies create providers classpath
	 * 
	 * currently unused
	 */
	@SuppressWarnings("unused")
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
	 * Extra entries to be put in the codebase. Proxy and UI are added
	 * automatically Each artifact must be mentioned in the dependencies
	 * section.
	 */
	@Parameter
	protected org.apache.maven.model.Dependency[] codebase;

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	protected Artifact artifact;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(),
				project.getVersion());
		resolveProviderName();
		try {
			resolveProviderDependencies();
		} catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (RepositoryException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void resolveProviderName() throws MojoFailureException {
		if (StringUtils.isBlank(providerName)) {
			String artifactId = project.getArtifactId();
			if (!artifactId.endsWith(SERVICE_SUFFIX)) {
				throw new MojoFailureException("Undefined 'providerName'");
			}
			providerName = artifactId.substring(0, artifactId.length() - SERVICE_SUFFIX.length());
		}
		getLog().info("providerName = " + providerName);
	}

	private void resolveProviderDependencies() throws MojoFailureException, MojoExecutionException,
			RepositoryException, AbstractArtifactResolutionException, DependencyResolutionException {
		String codebase = resolveCodeBase();
		getLog().info(codebase);
		mavenSession.getCurrentProject().getProperties().setProperty("provider.codebase", codebase);

		String classpath = resolveClassPath();
		getLog().info(classpath);
		mavenSession.getCurrentProject().getProperties().setProperty("provider.classpath", classpath);
	}

	protected String resolveCodeBase() throws AbstractArtifactResolutionException, RepositoryException,
			MojoExecutionException, MojoFailureException {
		Artifact proxyArtifact = resolveModuleDependency(proxy, "proxy");
		Artifact uiArtifact = resolveModuleDependency(ui, "ui");

		List<Artifact> codeBaseArtifacts = new LinkedList<Artifact>();
		if (proxyArtifact != null)
			codeBaseArtifacts.add(proxyArtifact);
		if (uiArtifact != null)
			codeBaseArtifacts.add(uiArtifact);

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
			result.append("\n\t\t\t\tsorcer.util.ArtifactCoordinates.coords(\"").append(ArtifactUtil.key(dependency)).append("\")");
		}
		result.append("\n\t\t\t})");
		return result.toString();
	}

	protected String resolveClassPath() throws DependencyResolutionException {
		DefaultDependencyResolutionRequest request = new DefaultDependencyResolutionRequest(project, repoSession);

		DependencyFilter runtimeFilter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);

		// booter is required as a dependency of the provider project to start
		// the service, but shouldn't be part of provider classpath
		ExclusionsDependencyFilter booterExclusion = new ExclusionsDependencyFilter(
				Arrays.asList("org.sorcersoft.sorcer:sos-boot"));

		request.setResolutionFilter(DependencyFilterUtils.andFilter(runtimeFilter, booterExclusion));
		DependencyResolutionResult resolutionResult = projectDependenciesResolver.resolve(request);

		Collection<Artifact> dependencies = Collections2.transform(resolutionResult.getDependencies(),
				new Function<Dependency, Artifact>() {
					@Nullable
					@Override
					public Artifact apply(@Nullable Dependency input) {
						return input == null ? null : input.getArtifact();
					}
				});

		Collection<Artifact> result = new ArrayList<Artifact>(dependencies.size() + 1);
		result.add(new DefaultArtifact(ArtifactUtil.key(project.getArtifact())));
		result.addAll(dependencies);

		return buildClassPathString(result, "resolveClassPath");
	}

	protected Artifact resolveModuleDependency(org.apache.maven.model.Dependency userValue, String type)
			throws AbstractArtifactResolutionException, RepositoryException, MojoExecutionException,
			MojoFailureException {
		return resolveProjectDependency(userValue, project.getGroupId(), providerName + "-" + type);
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

		DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, new DependencyRequest(
				collectRequest, DependencyFilterUtils.classpathFilter(scope)));

		return Collections2.transform(dependencyResult.getArtifactResults(), new ArtifactResultTransformer());
	}

}
