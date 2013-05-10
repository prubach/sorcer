package sorcer.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import sorcer.maven.util.Process2;

/**
 * @author Rafał Krupiński
 */
@Mojo(name = "destroy", aggregator = true, defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class DestroyMojo extends AbstractSorcerMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Process2 process = getProcess();
		if (process != null) {
			if (process.running()) {
				getLog().info("KILL KILL KILL");
				process.destroy();
			} else {
				getLog().warn("Process dead");
			}
		}
	}
}
