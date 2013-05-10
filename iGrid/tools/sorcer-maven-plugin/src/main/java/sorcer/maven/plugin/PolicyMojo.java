package sorcer.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sorcer.maven.util.PolicyFileHelper;

import java.io.File;

/**
 * Generate default policy file
 * 
 * @author Rafał Krupiński
 */
@Mojo(name="policy", defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES)
public class PolicyMojo extends AbstractMojo {

	@Parameter(property = "project.build.testOutputDirectory", readonly = true)
	protected File testOutputDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		PolicyFileHelper.preparePolicyFile(testOutputDir.getPath());
	}
}
