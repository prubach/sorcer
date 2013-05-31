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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.slf4j.impl.StaticLoggerBinder;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import sorcer.boot.ServiceStarter;
import sorcer.core.SorcerConstants;
import sorcer.maven.util.ArtifactUtil;
import sorcer.maven.util.EnvFileHelper;
import sorcer.maven.util.JavaProcessBuilder;
import sorcer.maven.util.PolicyFileHelper;
import sorcer.maven.util.Process2;
import sorcer.maven.util.TestCycleHelper;
import sorcer.tools.webster.Webster;
import sorcer.util.JavaSystemProperties;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Boot sorcer provider
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "provider", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class BootMojo extends AbstractSorcerMojo {
	/**
	 * Location of the services file.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}/config/services.config")
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

	@Parameter(defaultValue = "true")
	protected boolean cleanBlitz;

	/**
	 * Log file to redirect standard and error output to. This only works for
	 * java 1.7+
	 */
	@Parameter(defaultValue = "${project.build.directory}/provider.log")
	protected File logFile;

	@Parameter(property = "project.build.outputDirectory")
	protected File workingDir;

	@Parameter(defaultValue = "${project.build.directory}/blitz")
	protected File blitzHome;

	public void execute() throws MojoExecutionException, MojoFailureException {
		//allow others to use maven logger
		StaticLoggerBinder.getSingleton().setLog(getLog());

		getLog().debug("servicesConfig: " + servicesConfig);

		cleanBlitz();

		// prepare sorcer.env with updated group
		String sorcerEnv = EnvFileHelper.prepareEnvFile(projectOutputDir.getPath());

		//prepare sorcer.policy with grant AllPermission
		PolicyFileHelper.preparePolicyFile(testOutputDir.getPath());

		List<Artifact> artifacts;
		try {
			artifacts = resolveDependencies(new DefaultArtifact(booter + ":" + sorcerVersion), JavaScopes.RUNTIME);
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException("Error while resolving booter dependencies", e);
		}

		Map<String, String> properties = new HashMap<String, String>();
		String sorcerHome = System.getenv("SORCER_HOME");
		String rioHome = System.getenv("RIO_HOME");
		if (rioHome == null) {
			rioHome = sorcerHome + "/lib/rio";
		}
		properties.put(JavaSystemProperties.JAVA_RMI_SERVER_USE_CODEBASE_ONLY, "false");
		properties.put(JavaSystemProperties.JAVA_PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb.sos");
		properties.put(JavaSystemProperties.JAVA_SECURITY_POLICY, new File(testOutputDir, "sorcer.policy").getPath());
		properties.put(SorcerConstants.SORCER_HOME, sorcerHome);
		properties.put(SorcerConstants.S_RIO_HOME, rioHome);
		properties.put(SorcerConstants.S_WEBSTER_TMP_DIR, new File(sorcerHome, "data").getPath());
		properties.put(SorcerConstants.S_KEY_SORCER_ENV, sorcerEnv);
		properties.put(SorcerConstants.P_WEBSTER_PORT, "" + reservePort());
		properties.put(SorcerConstants.S_BLITZ_HOME, blitzHome.getPath());

		JavaProcessBuilder builder = new JavaProcessBuilder();
		builder.setMainClass(mainClass);
		builder.setProperties(properties);
		builder.setParameters(Arrays.asList(ServiceStarter.SORCER_DEFAULT_CONFIG, servicesConfig.getPath()));
		builder.setClassPath(ArtifactUtil.toString(artifacts));
		builder.setDebugger(debug);
		builder.setOutput(logFile);
		builder.setWorkingDir(workingDir);

		getLog().info("starting sorcer");
		Process2 process = builder.startProcess();
		putProcess(process);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			process.destroy();
			throw new MojoExecutionException("Interrupted", e);
		}
	}

	private void cleanBlitz() {
		if (!cleanBlitz) return;
		getLog().info("Cleaning blitz directory (" + blitzHome + ")");
		try {
			FileUtils.deleteDirectory(blitzHome);
		} catch (IOException e) {
			getLog().info("Could not delete directory", e);
		}
	}

	private int reservePort() {
		int port = Webster.getAvailablePort();
		TestCycleHelper.getInstance().setWebsterPort(port);
		return port;
	}
}
