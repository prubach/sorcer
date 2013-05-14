package sorcer.maven.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import sorcer.maven.util.ArtifactUtil;
import sorcer.maven.util.EnvFileHelper;
import sorcer.maven.util.JavaProcessBuilder;

/**
 * Boot sorcer provider
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "provider", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class BootMojo extends AbstractSorcerMojo {
	/**
	 * Location of the services file.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/sorcer/services.config")
	private File servicesConfig;

	@Parameter(required = true, defaultValue = "sorcer.boot.ServiceStarter")
	protected String mainClass;

	@Parameter(property = "project.build.directory", readonly = true)
	protected File projectOutputDir;

	@Parameter(defaultValue = "${v.sorcer}")
	protected String sorcerVersion;

	@Parameter(defaultValue = "org.sorcersoft.sorcer:sos-boot")
	protected String booter;

	@Parameter(defaultValue = "${providerName}-prv")
	protected String provider;

	@Parameter(property = "sorcer.provider.debug")
	protected boolean debug;

	@Parameter(defaultValue = "${project.build.directory}/provider.log")
	protected File logFile;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("servicesConfig: " + servicesConfig);

		// prepare sorcer.env with updated group
		String sorcerEnv = EnvFileHelper.prepareEnvFile(projectOutputDir.getPath());
		List<Artifact> artifacts;

		try {
			artifacts = resolveDependencies(new DefaultArtifact(booter + ":" + sorcerVersion), JavaScopes.RUNTIME);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException("Error while resolving booter dependencies", e);
		}

		Map<String, String> properties = new HashMap<String, String>();
		String sorcerHome = System.getenv("SORCER_HOME");
		properties.put("java.net.preferIPv4Stack", "true");
		properties.put("java.rmi.server.useCodebaseOnly", "false");
		properties.put("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb.sos");
		properties.put("sorcer.home", sorcerHome);
		properties.put("rio.home", System.getenv("RIO_HOME"));
		properties.put("webster.tmp.dir", new File(sorcerHome, "data").getPath());
		properties.put("sorcer.env.file", sorcerEnv);
		properties.put("java.security.policy", new File(testOutputDir, "sorcer.policy").getPath());

		JavaProcessBuilder builder = new JavaProcessBuilder(getLog());
		builder.setMainClass(mainClass);
		builder.setProperties(properties);
		builder.setParameters(Arrays.asList(servicesConfig.getPath()));
		builder.setClassPath(ArtifactUtil.toString(artifacts));
		builder.setDebugger(debug);
		builder.setOutput(logFile);

		getLog().info("starting sorcer");
		putProcess(builder.startProcess());
	}
}
