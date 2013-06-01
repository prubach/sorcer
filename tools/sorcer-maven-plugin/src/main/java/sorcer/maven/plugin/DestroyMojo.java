/**
 *
 * Copyright 2013 Rafał Krupiński
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import sorcer.maven.util.Process2;
import sorcer.maven.util.TestCycleHelper;

/**
 * @author Rafał Krupiński
 */
@Mojo(name = "destroy", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class DestroyMojo extends AbstractSorcerMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Process2 process = getProcess();
		if (process != null) {
			if (process.running()) {
				getLog().info("Killing the process");
				process.destroy();
			} else {
				getLog().warn("Destroy mojo can only be used together with boot mojo");
			}
		}
		if(TestCycleHelper.getInstance().isFail()){
			throw new MojoExecutionException("Build failed (see previous logs)");
		}
	}
}
