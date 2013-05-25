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
import sorcer.maven.util.TestCycleHelper;
import sorcer.tools.webster.Webster;

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

	/**
	 * Log file to redirect standard and error output to. This only works for
	 * java 1.7+
	 */
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
		properties.put("provider.webster.port", "" + reservePort());

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

	private int reservePort() {
		int port = Webster.getAvailablePort();
		TestCycleHelper.getInstance().setWebsterPort(port);
		return port;
	}
}
