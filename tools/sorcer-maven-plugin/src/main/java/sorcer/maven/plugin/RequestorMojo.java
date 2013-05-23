/*
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
import sorcer.core.SorcerConstants;
import sorcer.maven.util.ArtifactUtil;
import sorcer.maven.util.JavaProcessBuilder;
import sorcer.maven.util.Process2;
import sorcer.maven.util.TestCycleHelper;
import sorcer.provider.boot.Booter;
import sorcer.util.JavaSystemProperties;

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

	@Parameter(defaultValue = "${project.build.directory}/sorcer.env")
	protected File sorcerEnvFile;

	@Parameter(defaultValue = "test")
	protected String scope;

	@Parameter(property = "sorcer.requestor.debug")
	protected boolean debug;

	@Parameter
	protected String[] requestorCodebase = new String[0];

	@Parameter
	protected String[] requestorClasspath = new String[0];

	@Parameter(defaultValue = "${project.build.directory}/requestor.log")
	protected File logFile;

	@Parameter
	protected ClientConfiguration[] requestors;

	@Parameter(defaultValue = "${env.SORCER_HOME}")
	protected File sorcerHome;

	/**
	 * Milliseconds to wait before stopping the requestor
	 */
	@Parameter(defaultValue = "10000", property = "client.timeout")
	protected int timeout;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!project.getPackaging().equals("jar")) {
			getLog().warn("Plugin misconfigured: running on a project of packaging other than jar");
		}

		Map<String, String> defaultSystemProps = new HashMap<String, String>();
		defaultSystemProps.put(JavaSystemProperties.JAVA_UTIL_LOGGING_CONFIG_FILE, new File(sorcerHome, "configs/sorcer.logging").getPath());
		defaultSystemProps.put(JavaSystemProperties.JAVA_SECURITY_POLICY, new File(testOutputDir, "sorcer.policy").getPath());
		defaultSystemProps.put(SorcerConstants.S_KEY_SORCER_ENV, sorcerEnvFile.getPath());
		defaultSystemProps.put(JavaSystemProperties.JAVA_RMI_SERVER_USE_CODEBASE_ONLY, "false");

		Collection<ClientRuntimeConfiguration> configurations = buildClientsList();
		for (ClientRuntimeConfiguration config : configurations) {
			JavaProcessBuilder builder = config.preconfigureProcess();

			Map<String, String> properties = new HashMap<String, String>();
			properties.putAll(defaultSystemProps);
			properties.putAll(config.getSystemProperties());
			builder.setProperties(properties);

			builder.setDebugger(debug);
			builder.setOutput(logFile);

			try {
				getLog().info("Starting requestor process");
				Process2 process = builder.startProcess();
				if (debug) {
					process.waitFor();
					getLog().info("Requestor process has finished");
				} else {
					process.waitFor(timeout, true);
					getLog().warn("Requestor process has been destroyed after " + timeout + "ms.");
				}
			} catch (InterruptedException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
	}

	private Collection<ClientRuntimeConfiguration> buildClientsList() throws MojoExecutionException {
		String host = "http://" + Booter.getWebsterHostName() + ":" + TestCycleHelper.getInstance().getWebsterPort();
		if (requestors == null || requestors.length == 0) {
			ClientRuntimeConfiguration config = new ClientRuntimeConfiguration(mainClass, buildClasspath(requestorClasspath));
			updateCodebaseAndRoots(config, host, requestorCodebase);
			return Arrays.asList(config);
		} else {
			List<ClientRuntimeConfiguration> result = new ArrayList<ClientRuntimeConfiguration>(requestors.length);
			for (ClientConfiguration requestor : requestors) {
				String configMainClass = mainClass != null ? mainClass : requestor.mainClass;
				ClientRuntimeConfiguration config = new ClientRuntimeConfiguration(configMainClass, buildClasspath(requestor.classpath));
				updateCodebaseAndRoots(config, host, requestor.codebase);
			}
			return result;
		}
	}

	private Collection<String> buildClasspath(String[] userClasspath) throws MojoExecutionException {
		Collection<Artifact> artifacts = resolveDependencies(KEY_REQUESTOR, userClasspath, JavaScopes.TEST);
		Collection<String> classPathList = ArtifactUtil.toString(artifacts);
		classPathList.add(project.getBuild().getTestOutputDirectory());
		return classPathList;
	}

	private void updateCodebaseAndRoots(ClientRuntimeConfiguration config, String websterUrl, String[] userCodebase) throws MojoExecutionException {
		Collection<Artifact> artifacts = resolveDependencies(KEY_REQUESTOR, userCodebase, JavaScopes.TEST);
		List<String> codebase = new LinkedList<String>();
		try {
			String repositoryPath = repositorySystemSession.getLocalRepository().getBasedir().getCanonicalPath();
			List<String> websterRoots = new LinkedList<String>();
			for (Artifact artifact : artifacts) {
				File artifactFile = artifact.getFile();
				String artifactPath = artifactFile.getCanonicalPath();
				if (artifactPath.startsWith(repositoryPath)) {
					// add jar from repository
					codebase.add(websterUrl + artifactPath.substring(repositoryPath.length()));
				} else {
					// add jar from target dir
					codebase.add(websterUrl + '/' + artifactFile.getName());
					websterRoots.add(artifactFile.getParent());
				}
			}
			// if list of root directories for webster is not empty, we must add the default directory
			if (!websterRoots.isEmpty()) {
				websterRoots.add(repositorySystemSession.getLocalRepository().getBasedir().getPath());
			}
			config.setWebsterRoots(websterRoots);
			config.codebase = codebase.toArray(new String[codebase.size()]);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected Collection<Artifact> resolveDependencies(String propertyKey, String[] userEntries, String scope)
			throws MojoExecutionException {
		List<String> coordinates = new LinkedList<String>();
		if (propertyKey != null && project.getProperties().containsKey(propertyKey)) {
			coordinates.add(project.getProperties().getProperty(propertyKey));
		}
		if (userEntries != null) {
			coordinates.addAll(Arrays.asList(userEntries));
		}
		try {
			Set<Artifact> artifacts = new HashSet<Artifact>(coordinates.size());
			for (String coords : coordinates) {
				artifacts.addAll(resolveDependencies(new DefaultArtifact(coords), scope));
			}
			return artifacts;
		} catch (DependencyResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
