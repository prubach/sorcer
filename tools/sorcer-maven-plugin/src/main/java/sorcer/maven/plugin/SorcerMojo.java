/**
 *
 * Copyright 2013 Rafał Krupiński
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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import sorcer.maven.util.ArtifactUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

		artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(),
				project.getVersion());
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
}
