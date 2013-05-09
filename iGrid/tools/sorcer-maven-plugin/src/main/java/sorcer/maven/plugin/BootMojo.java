package sorcer.maven.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
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
import sorcer.maven.util.PolicyFileHelper;
import sorcer.maven.util.SorcerProcessBuilder;

/**
 * Boot sorcer provider
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "provider", aggregator = true, defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class BootMojo extends AbstractSorcerMojo {
	/**
	 * Location of the services file.
	 */
	//@Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/sorcer/services.config")
	@Parameter(defaultValue = "${basedir}/first-prv/target/classes/META-INF/sorcer/services.config")
	private File servicesConfig;

	@Parameter(defaultValue = "${v.sorcer}")
	protected String sorcerVersion;

	@Parameter(required = true, defaultValue = "sorcer.boot.ServiceStarter")
	protected String mainClass;

	public void execute() throws MojoExecutionException, MojoFailureException {
		String key = getProviderProjectKey();
		if (System.getProperty(key) != null) {
			return;
		}
		String providerProject = (String) project.getParent().getProperties().get(KEY_PROVIDER);
		System.setProperty(key, providerProject);

		String projectOutDir = project.getBuild().getDirectory();
		if (servicesConfig == null) {
			servicesConfig = new File("target/classes/META-INF/sorcer/services.config");
		}

		Log log = getLog();
		log.debug("servicesConfig: " + servicesConfig);
		log.info("starting sorcer");

		String policy = PolicyFileHelper.preparePolicyFile(projectOutDir);

		// prepare sorcer.env with updated group
		String sorcerEnv = EnvFileHelper.prepareEnvFile(projectOutDir);
		List<Artifact> artifacts;

		try {
			artifacts = createAether().resolve(new DefaultArtifact("org.sorcersoft.sorcer:sos-boot:" + sorcerVersion),
					JavaScopes.RUNTIME);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException("Error while resolving dependencies", e);
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
		properties.put("java.security.policy", policy);

		SorcerProcessBuilder builder = new SorcerProcessBuilder(getLog());
		builder.setMainClass(mainClass);
		builder.setProperties(properties);
		builder.setParameters(Arrays.asList(servicesConfig.getPath()));
		builder.setClassPath(ArtifactUtil.toString(artifacts));

		putProcess(builder.startProcess());
	}

}
