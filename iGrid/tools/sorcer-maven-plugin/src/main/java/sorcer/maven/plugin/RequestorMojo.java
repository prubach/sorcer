package sorcer.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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

import sorcer.core.SorcerEnv;
import sorcer.maven.util.ArtifactUtil;
import sorcer.maven.util.SorcerProcessBuilder;

/**
 * @author Rafał Krupiński
 */
@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "run-requestor", aggregator = true, defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class RequestorMojo extends AbstractSorcerMojo {

	@Parameter(property = "project.build.outputDirectory", readonly = true)
	protected File outputDir;

	@Parameter(property = "project.build.directory", readonly = true)
	protected File targetDir;

	@Parameter(required = true, property = "sorcer.requestor.mainClass")
	protected String mainClass;

	@Parameter(property = "basedir", readonly = true)
	protected File baseDir;

	/**
	 * Milliseconds to wait before starting the requestor
	 */
	@Parameter(defaultValue = "3000")
	protected int waitBeforeRun;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		String sorcerHome = SorcerEnv.getHomeDir().getPath();

		Map<String, String> sysProps = new HashMap<String, String>();
		sysProps.put("java.util.logging.config.file", new File(sorcerHome, "configs/sorcer.logging").getPath());
		sysProps.put("java.security.policy", new File(baseDir,"first-prv/target/sorcer.policy").getPath());
		sysProps.put("sorcer.env.file", new File(targetDir, "sorcer.env").getPath());
		sysProps.put("java.rmi.server.codebase", buildCodeBase());
		sysProps.put("java.rmi.server.useCodebaseOnly", "false");

		SorcerProcessBuilder builder = new SorcerProcessBuilder(getLog());
		builder.setProperties(sysProps);
		builder.setMainClass(mainClass);
		builder.setClassPath(buildClasspath());

		try {
			if (waitBeforeRun > 0) {
				Thread.sleep(waitBeforeRun);
			}
			getLog().info("Starting requestor process");
			Process process = builder.startProcess();
			process.waitFor();
			getLog().info("Requestor process has finished");
		} catch (InterruptedException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private Collection<String> buildClasspath() throws MojoExecutionException {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		List<String> cp = Arrays.asList( (String)project.getProperties().get(KEY_REQUESTOR));
		try {
			for (String coords : cp) {
				artifacts.addAll(resolveDependencies(new DefaultArtifact(coords), JavaScopes.RUNTIME));
			}
			return ArtifactUtil.toString(artifacts);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private List<String> websterRoots = new LinkedList<String>();

	private String buildCodeBase() throws MojoExecutionException {
		List<String> cb = (List<String>) project.getProperties().get(KEY_CODEBASE_REQUESTOR);
		Set<Artifact> artifacts = new HashSet<Artifact>();
		try {
			String repositoryPath = repositorySystemSession.getLocalRepository().getBasedir().getCanonicalPath();
			for (String s : cb) {
				artifacts.addAll(resolveDependencies(new DefaultArtifact(s), JavaScopes.RUNTIME));
			}
			List<String> codeBaseList = new LinkedList<String>();
			for (Artifact artifact : artifacts) {
				File artifactFile = artifact.getFile();
				String artifactPath = artifactFile.getCanonicalPath();
				if (artifactPath.startsWith(repositoryPath)) {
					// add jar from repository
					codeBaseList.add(artifactPath.substring(repositoryPath.length()));
				} else {
					// add jar from target
					codeBaseList.add(artifactFile.getName());
					websterRoots.add(artifactFile.getParent());
				}
			}
			return StringUtils.join(codeBaseList, " ");
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}
}
