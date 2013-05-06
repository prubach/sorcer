package sorcer.maven.plugin;

import java.io.File;
import java.io.IOException;
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
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.DefaultDependencyNode;

import sorcer.util.ArtifactResultTransformer;
import sorcer.util.ArtifactUtil;
import sorcer.util.EnvFileHelper;
import sorcer.util.PolicyFileHelper;

import com.google.common.collect.Lists;

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
	@Parameter(required = true, defaultValue = "${project.build.outputDirectory}/META-INF/sorcer/services.config")
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
		Log log = getLog();
		log.debug("servicesConfig: " + servicesConfig);
		// log.debug("webster: " + webster);
		log.info("starting sorcer");

		String projectOutDir = project.getBuild().getDirectory();
		String policy = PolicyFileHelper.preparePolicyFile(projectOutDir);

		// prepare sorcer.env with updated group
		String sorcerEnv = EnvFileHelper.prepareEnvFile(projectOutDir);
		String classPath = resolveClassPath("org.sorcersoft.sorcer:sos-boot:11.1");
		getLog().info("Classpath = " + classPath);

		ProcessBuilder procBld = new ProcessBuilder().command("java", _D("sorcer.env.file", sorcerEnv),
				_D("java.security.policy", policy), "-cp", classPath, mainClass, servicesConfig.getPath());
		procBld.redirectErrorStream(true);
		Process proc = null;

		try {
			proc = procBld.start();
			Thread.sleep(100);
			// if next call throws exception, then we're probably good - process
			// hasn't finished yet.
			int x = proc.exitValue();
			throw new MojoExecutionException("Process exited with value " + x);
		} catch (IllegalThreadStateException x) {
			if (proc != null) {
				// put the process object in the context, so DestroyMojo can
				// kill it.
				putProcess(getPluginContext(), proc);
			} else {
				throw new MojoFailureException("Could not start java process");
			}
		} catch (InterruptedException e) {
			throw new MojoFailureException("Could not start java process", e);
		} catch (IOException e) {
			throw new MojoFailureException("Could not start java process", e);
		}

	}

	private String resolveClassPath(String coords) throws MojoExecutionException {
		try {
			DependencyRequest request = new DependencyRequest();
			DefaultDependencyNode dependencyNode = new DefaultDependencyNode();
			Dependency rootDep = new Dependency(new DefaultArtifact(coords), null);
			dependencyNode.setDependency(rootDep);


			CollectResult result = repositorySystem.collectDependencies(repoSession, new CollectRequest(rootDep, remoteRepos));
			//return ArtifactUtil.toJavaClassPath(artifacts);
			return null;
		//} catch (DependencyResolutionException e) {
		//	throw new MojoExecutionException("Error while resolving dependencies", e);
		} catch (DependencyCollectionException e) {
			throw new MojoExecutionException("Error while resolving dependencies", e);
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
