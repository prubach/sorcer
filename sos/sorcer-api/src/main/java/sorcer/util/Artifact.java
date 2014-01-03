/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.util;

/**
 * This is a tool to make referring to sorcer jars a bit easier.
 *
 * Please avoid using it in examples for other artifacts than core SORCER.
 * 
 * @author Rafał Krupiński
 */
public class Artifact {
	private static final String SORCER_GROUP_ID = "org.sorcersoft.sorcer";

	public static ArtifactCoordinates sorcer(String artifactId) {
		return ArtifactCoordinates.coords(SORCER_GROUP_ID + ":" + artifactId);
	}

	public static ArtifactCoordinates getSosPlatform() {
		return sorcer("sos-platform");
	}

	public static ArtifactCoordinates getSorcerApi() {
		return sorcer("sorcer-api");
	}
}
