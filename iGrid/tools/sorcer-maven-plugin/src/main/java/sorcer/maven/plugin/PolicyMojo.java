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
