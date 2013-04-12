package sorcer.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import sorcer.boot.ServiceStarter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Boot sorcer provider
 */
@Mojo(name = "boot", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class BootMojo
		extends AbstractMojo {
	/**
	 * Location of the file.
	 */
	@Parameter(property = "servicesConfig", required = true, defaultValue = "META-INF/sorcer/services.config")
	private File servicesConfig;

	@Parameter(required = true, defaultValue = "true")
	private boolean webster;


	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", required = true, readonly = true)
	private MavenSession session;

	@Component
	private ToolchainManager toolchainManager;

	public void execute() throws MojoExecutionException {

		Log log = getLog();

		File configFile = findConfig();

		log.debug("servicesConfig: "+servicesConfig+" -> " + configFile);
		log.debug("webster: " + webster);
		log.info("starting sorcer");
		try {
			ServiceStarter.main(new String[]{configFile.getAbsolutePath()});
			log.info("started sorcer");
		} catch (IOException e) {
			throw new MojoExecutionException("Error while calling ServiceStarter", e);
		}
	}

	private File findConfig() throws MojoExecutionException {
		List<Resource> resources = project.getBuild().getResources();
		for (Resource resource : resources) {
			String path = resource.getTargetPath();
			if (path == null) {
				path = resource.getDirectory();
			}
			File candidateFile = new File(path, servicesConfig.getPath());
			if (candidateFile.exists()) {
				return candidateFile;
			}
		}
		getLog().error("Could not find services.config");
		throw new MojoExecutionException("Could not find services.config");
	}
}
