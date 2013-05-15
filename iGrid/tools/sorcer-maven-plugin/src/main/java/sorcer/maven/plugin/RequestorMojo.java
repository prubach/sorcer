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
import sorcer.maven.util.TestCycleHelper;

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

	@Parameter(defaultValue = "runtime")
	protected String scope;

	@Parameter(property = "sorcer.requestor.debug")
	protected boolean debug;

	@Parameter
	protected List<String> requestorCodebase = new ArrayList<String>();

	@Parameter
	protected List<String> requestorClasspath = new ArrayList<String>();

	@Parameter(defaultValue = "${project.build.directory}/requestor.log")
	protected File logFile;

	/**
	 * Milliseconds to wait before starting the requestor
	 */
	@Parameter(defaultValue = "3000")
	protected int waitBeforeRun;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!project.getPackaging().equals("jar")) {
			getLog().warn("Plugin misconfigured: runnig on a project with packaging other than jar");
		}

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
		builder.setDebugger(debug);
		builder.setOutput(logFile);

		try {
			if (waitBeforeRun > 0) {
				Thread.sleep(waitBeforeRun);
			}
			getLog().info("Starting requestor process");
			Process2 process = builder.startProcess();
			if (debug) {
				process.waitFor();
			} else {
				process.waitFor(10000, true);
			}
			getLog().info("Requestor process has finished");
		} catch (InterruptedException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private Collection<String> buildClasspath() throws MojoExecutionException {
		Collection<Artifact> artifacts = resolveDependencies(KEY_REQUESTOR, requestorClasspath, JavaScopes.TEST);
		Collection<String> classPathList = ArtifactUtil.toString(artifacts);
		classPathList.add(project.getBuild().getTestOutputDirectory());
		return classPathList;
	}

	private List<String> websterRoots = new LinkedList<String>();

	private String buildCodeBase() throws MojoExecutionException {
		String host = "http://192.168.0.5:" + TestCycleHelper.getInstance().getWebsterPort();
		Collection<Artifact> artifacts = resolveDependencies(KEY_REQUESTOR, requestorCodebase, JavaScopes.TEST);

		try {
			String repositoryPath = repositorySystemSession.getLocalRepository().getBasedir().getCanonicalPath();
			List<String> codeBaseList = new LinkedList<String>();
			for (Artifact artifact : artifacts) {
				File artifactFile = artifact.getFile();
				String artifactPath = artifactFile.getCanonicalPath();
				if (artifactPath.startsWith(repositoryPath)) {
					// add jar from repository
					codeBaseList.add(host + artifactPath.substring(repositoryPath.length()));
				} else {
					// add jar from target
					codeBaseList.add(host + '/' + artifactFile.getName());
					websterRoots.add(artifactFile.getParent());
				}
			}
			return StringUtils.join(codeBaseList, " ");
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected Collection<Artifact> resolveDependencies(String propertyKey, Collection<String> userEntries, String scope)
			throws MojoExecutionException {
		List<String> coordinates = new LinkedList<String>();
		if (propertyKey != null && project.getProperties().containsKey(propertyKey)) {
			coordinates.add(project.getProperties().getProperty(propertyKey));
		}
		if (userEntries != null) {
			coordinates.addAll(userEntries);
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
