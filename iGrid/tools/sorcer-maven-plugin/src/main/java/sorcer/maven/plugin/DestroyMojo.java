package sorcer.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Rafał Krupiński
 */
@Mojo(name = "destroy", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class DestroyMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Process process = BootMojo.getProcess(getPluginContext());
		if (process != null) {
			getLog().error("KILL KILL KILL");
			process.destroy();
		}
	}
}
