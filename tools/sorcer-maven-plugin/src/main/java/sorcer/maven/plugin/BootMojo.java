/**
 *
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.graph.DefaultDependencyNode;

import sorcer.util.ArtifactUtil;
import sorcer.util.EnvFileHelper;
import sorcer.util.PolicyFileHelper;

import com.jcabi.aether.Aether;

/**
 * Boot sorcer provider
 */
@Mojo(name = "boot", aggregator = true, defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class BootMojo extends AbstractMojo {

	public static final String KEY_PROCESS = BootMojo.class.getName() + ".process";
	@Parameter(required = true, defaultValue = "${env.SORCER_HOME}/configs/sorcer.env")
	private File sorcerEnv;

	/**
	 * Location of the services file.
	 */
	@Parameter
	private File servicesConfig;

	@Parameter(required = true, defaultValue = "true")
	private boolean webster;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", required = true, readonly = true)
	private MavenSession session;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter(defaultValue = "${v.sorcer}")
	protected String sorcerVersion;

	@Component
	protected ProjectDependenciesResolver projectDependenciesResolver;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	protected List<RemoteRepository> remoteRepos;
	@Component
	protected ArtifactResolver artifactResolver;

	@Component
	protected RepositorySystem repositorySystem;

	@Parameter(required = true, defaultValue = "sorcer.boot.ServiceStarter")
	protected String mainClass;

	public void execute() throws MojoExecutionException, MojoFailureException {
		String projectOutDir = project.getBuild().getDirectory();
		if (servicesConfig == null) {
			servicesConfig = new File("first-prv/target/classes/META-INF/sorcer/services.config");
		}

		Log log = getLog();
		log.debug("servicesConfig: " + servicesConfig);
		// log.debug("webster: " + webster);
		log.info("starting sorcer");

		String policy = PolicyFileHelper.preparePolicyFile(projectOutDir);

		// prepare sorcer.env with updated group
		String sorcerEnv = EnvFileHelper.prepareEnvFile(projectOutDir);
		String classPath = resolveClassPath("org.sorcersoft.sorcer:sos-boot:1.0-SNAPSHOT");
		getLog().info("Classpath = " + classPath);

		Map<String, Object> _d = new HashMap<String, Object>();
		_d.put("java.net.preferIPv4Stack", true);
		_d.put("java.rmi.server.useCodebaseOnly", false);
		_d.put("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb.sos");
		String sorcerHome = System.getenv("SORCER_HOME");
		_d.put("sorcer.home", sorcerHome);
		_d.put("rio.home", System.getenv("RIO_HOME"));
		_d.put("webster.tmp.dir", new File(sorcerHome, "data"));
		_d.put("sorcer.env.file", sorcerEnv);
		_d.put("java.security.policy", policy);

		ProcessBuilder procBld = new ProcessBuilder().command("java");
		procBld.command().addAll(_D(_d));
		procBld.command().addAll(Arrays.asList("-classpath", classPath, mainClass, servicesConfig.getPath()));

		Process proc = null;
		try {
			try {
				proc = procBld.start();
				// give it a second to exit on error
				Thread.sleep(1000);
				// if next call throws exception, then we're probably good -
				// process
				// hasn't finished yet.
				int x = proc.exitValue();
				throw new MojoExecutionException("Process exited with value " + x);
			} catch (IllegalThreadStateException x) {
				if (proc != null) {
					// put the process object in the context, so DestroyMojo can
					// kill it.
					Thread.sleep(10000);
					putProcess(getPluginContext(), proc);
				} else {
					throw new MojoFailureException("Could not start java process");
				}
			} catch (IOException e) {
				throw new MojoFailureException("Could not start java process", e);
			}
		} catch (InterruptedException e) {
			throw new MojoFailureException("Could not start java process", e);
		}
	}

	private List<String> _D(Map<String, Object> d) {
		List<String> result = new ArrayList<String>(d.size());
		for (Map.Entry<String, Object> e : d.entrySet()) {
			result.add(_D(e.getKey(), e.getValue().toString()));
		}
		return result;
	}

	private String resolveClassPath(String coords) throws MojoExecutionException {
		try {
			DependencyRequest request = new DependencyRequest();
			DefaultDependencyNode dependencyNode = new DefaultDependencyNode();
			DefaultArtifact artifact = new DefaultArtifact(coords);
			Dependency rootDep = new Dependency(artifact, JavaScopes.RUNTIME);
			dependencyNode.setDependency(rootDep);
			request.setRoot(dependencyNode);
			request.setCollectRequest(new CollectRequest(rootDep, remoteRepos));
			List<Artifact> artifacts = new Aether(project, repoSession.getLocalRepository().getBasedir()).resolve(
					artifact, JavaScopes.RUNTIME);

			// DependencyResult result =
			// repositorySystem.resolveDependencies(repoSession, request);

			return ArtifactUtil.toJavaClassPath(artifacts);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException("Error while resolving dependencies", e);
			/*
			 * } catch (DependencyCollectionException e) { throw new
			 * MojoExecutionException("Error while resolving dependencies", e);
			 */
		}
	}

	@SuppressWarnings("unchecked")
	public static void putProcess(Map pluginContext, Process process) {
		pluginContext.put(KEY_PROCESS, process);
	}

	public static Process getProcess(Map pluginContext) {
		return (Process) pluginContext.get(KEY_PROCESS);
	}

	private static String _D(String key, String value) {
		return "-D" + key + '=' + value;
	}

}
