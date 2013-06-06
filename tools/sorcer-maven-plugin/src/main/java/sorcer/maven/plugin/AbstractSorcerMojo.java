/*
 * Copyright 2013 Rafał Krupiński.
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

import java.io.File;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.filter.DependencyFilterUtils;

import sorcer.maven.util.Process2;
import sorcer.maven.util.TestCycleHelper;
import sorcer.org.rioproject.net.HostUtil;

/**
 * @author Rafał Krupiński
 */
public abstract class AbstractSorcerMojo extends AbstractMojo {
	public static final String KEY_PROVIDER_NAME = "sorcer.providerName";
	public static final String KEY_CLASSPATH = "sorcer.classpath";
	public static final String KEY_CODEBASE = "sorcer.codebase";
	public static final String KEY_REQUESTOR = "sorcer.requestor";
	public static final String KEY_PROVIDER = "sorcer.provider";

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession repositorySystemSession;

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter(property = "project.remoteProjectRepositories", readonly = true)
	protected List<RemoteRepository> remoteRepositories;

	@Component
	protected RepositorySystem repositorySystem;

	@Parameter(property = "project.build.testOutputDirectory", readonly = true)
	protected File testOutputDir;

    protected static String getInetAddress() throws MojoExecutionException {
        try {
            return HostUtil.getInetAddress().getHostAddress();
        } catch (UnknownHostException e) {
            throw new MojoExecutionException("Could not obtain local address", e);
        }
    }

    @Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPluginContext() {
		return super.getPluginContext();
	}

	public void putProcess(Process2 process) {
		TestCycleHelper.getInstance().setProcess(process);
	}

	public Process2 getProcess() {
		return TestCycleHelper.getInstance().getProcess();
	}

	protected List<String> createClasspath(Artifact artifact, String scope) throws MojoExecutionException {
		// Request a collection of all dependencies for the artifact.
		DependencyRequest dependencyRequest = new DependencyRequest();
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, ""));
		for (RemoteRepository remoteRepository : remoteRepositories) {
			collectRequest.addRepository(remoteRepository);
		}
		CollectResult result;
		try {
			result = repositorySystem.collectDependencies(repositorySystemSession, collectRequest);
		} catch (DependencyCollectionException e) {
			throw new MojoExecutionException("Unable to resolve runner dependencies.", e);
		}

		final List<String> resultList = new LinkedList<String>();

		// Walk the tree of all dependencies to add to the classpath.
		result.getRoot().accept(new DependencyVisitor() {
			@Override
			public boolean visitEnter(DependencyNode node) {
				getLog().debug("Visiting: " + node);

				// Resolve the dependency node artifact into a real, local
				// artifact.
				Artifact resolvedArtifact;
				try {
					Artifact nodeArtifact = node.getDependency().getArtifact();
					resolvedArtifact = resolveArtifact(nodeArtifact.getGroupId(), nodeArtifact.getArtifactId(), "jar",
							nodeArtifact.getVersion());
				} catch (MojoExecutionException e) {
					throw new RuntimeException(e);
				}

				// Add the artifact's path to our classpath.
				resultList.add(resolvedArtifact.getFile().getAbsolutePath());

				return true;
			}

			@Override
			public boolean visitLeave(DependencyNode node) {
				return true;
			}
		});

		return resultList;
	}

	protected Artifact resolveArtifact(String groupId, String artifactId, String extension, String version)
			throws MojoExecutionException {
		return resolveArtifact(new DefaultArtifact(groupId, artifactId, extension, version));
	}

	protected Artifact resolveArtifact(Artifact artifact) throws MojoExecutionException {
		ArtifactRequest request = new ArtifactRequest();
		request.setArtifact(artifact);
		request.setRepositories(remoteRepositories);

		try {
			ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, request);
			return artifactResult.getArtifact();
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException("Unable to resolve runner from repository.", e);
		}
	}

	// from jcabi Aether

	public List<Artifact> resolveDependencies(@NotNull Artifact root, @NotNull String scope)
			throws DependencyResolutionException {
		DependencyFilter filter = DependencyFilterUtils.classpathFilter(scope);
		if (filter == null) {
			throw new IllegalStateException(String.format("failed to create a filter for '%s'", scope));
		}
		return resolveDependencies(root, scope, filter);
	}

	/**
	 * List of transitive deps of the artifact.
	 * 
	 * @param root
	 *            The artifact to work with
	 * @param scope
	 *            The scope to work with ("runtime", "test", etc.)
	 * @param filter
	 *            The dependency filter to work with
	 * @return The list of dependencies
	 * @throws DependencyResolutionException
	 *             If can't fetch it
	 */
	public List<Artifact> resolveDependencies(@NotNull Artifact root, @NotNull String scope, @NotNull DependencyFilter filter)
			throws DependencyResolutionException {
		CollectRequest crq = request(new Dependency(root, scope));
		DependencyRequest dreq = new DependencyRequest(crq, filter);
		return fetch(repositorySystemSession, dreq);
	}

	/**
	 * Fetch dependencies.
	 * 
	 * @param session
	 *            The session
	 * @param dreq
	 *            Dependency request
	 * @return The list of dependencies
	 * @throws DependencyResolutionException
	 *             If can't fetch it
	 * todo jcabi-#51 This catch of NPE is a temprorary measure. I don't know why
	 *       Aether throws NPE in case of unresolveable artifact. This is the
	 *       best I can do at the moment in order to protect clients of the
	 *       class.
	 */
	private List<Artifact> fetch(final RepositorySystemSession session,
								 final DependencyRequest dreq) throws DependencyResolutionException {
		final List<Artifact> deps = new LinkedList<Artifact>();
		try {
			Collection<ArtifactResult> results;
			synchronized (session.getLocalRepository().getBasedir()) {
				results = this.repositorySystem
						.resolveDependencies(session, dreq)
						.getArtifactResults();
			}
			for (ArtifactResult res : results) {
				deps.add(res.getArtifact());
			}
		} catch (Exception ex) {
			throw new DependencyResolutionException(new DependencyResult(dreq), new IllegalArgumentException(
					String.format("failed to load '%s' from repositories into %s", dreq.getCollectRequest().getRoot(),
							session.getLocalRepositoryManager().getRepository().getBasedir()), ex));
		}
		return deps;
	}

	/**
	 * Create collect request.
	 * 
	 * @param root
	 *            The root to start with
	 * @return The request
	 */
	private CollectRequest request(Dependency root) {
		CollectRequest request = new CollectRequest();
		request.setRoot(root);
		for (RemoteRepository repo : remoteRepositories) {
			request.addRepository(repo);
		}
		return request;
	}

}
