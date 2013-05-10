package sorcer.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import sorcer.maven.util.JavaProcessBuilder;
import sorcer.maven.util.Process2;

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

	@Parameter(property = "sorcer.requestor.mainClass", required = true)
	protected String mainClass;

	@Parameter(property = "basedir", readonly = true)
	protected File baseDir;

	@Parameter(defaultValue = "${basedir}/../first-prv/target/test-classes/sorcer.env")
	protected File sorcerEnvFile;

	@Parameter(defaultValue = "runtime")
	protected String scope;

	@Parameter
	protected boolean debugger;

	@Parameter
	protected List<String> codebase = new ArrayList<String>();

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
		sysProps.put("java.security.policy", new File(testOutputDir, "sorcer.policy").getPath());
		sysProps.put("sorcer.env.file", sorcerEnvFile.getPath());
		sysProps.put("java.rmi.server.codebase", buildCodeBase());
		sysProps.put("java.rmi.server.useCodebaseOnly", "false");
		if (!websterRoots.isEmpty()) {
			websterRoots.add(repositorySystemSession.getLocalRepository().getBasedir().getPath());
			sysProps.put("webster.roots", StringUtils.join(websterRoots, ";"));
		}

		JavaProcessBuilder builder = new JavaProcessBuilder(getLog());
		builder.setProperties(sysProps);
		builder.setMainClass(mainClass);
		builder.setClassPath(buildClasspath());
		builder.setDebugger(debugger);

		try {
			if (waitBeforeRun > 0) {
				Thread.sleep(waitBeforeRun);
			}
			getLog().info("Starting requestor process");
			Process2 process = builder.startProcess();
			process.waitFor(10000);
			getLog().info("Requestor process has finished");
		} catch (InterruptedException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private Collection<String> buildClasspath() throws MojoExecutionException {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		List<String> cp = Arrays.asList((String) project.getProperties().get(KEY_REQUESTOR));
		try {
			for (String coords : cp) {
				artifacts.addAll(resolveDependencies(new DefaultArtifact(coords), JavaScopes.TEST));
			}
			return ArtifactUtil.toString(artifacts);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private List<String> websterRoots = new LinkedList<String>();

	private String buildCodeBase() throws MojoExecutionException {
		List<String> cb = new LinkedList<String>(Arrays.asList(project.getProperties().getProperty(KEY_REQUESTOR)));
		cb.addAll(codebase);

		Set<Artifact> artifacts = new HashSet<Artifact>();
		try {
			String repositoryPath = repositorySystemSession.getLocalRepository().getBasedir().getCanonicalPath();
			for (String s : cb) {
				artifacts.addAll(resolveDependencies(new DefaultArtifact(s), JavaScopes.TEST));
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
