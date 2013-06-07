package sorcer.launcher;
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

import sorcer.core.SorcerEnv;
import sorcer.util.Process2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;

/**
 * @author Rafał Krupiński
 */
public class SorcerProcessBuilder extends JavaProcessBuilder {
    protected String sorcerHome;

    public SorcerProcessBuilder(SorcerEnv env) {
        sorcerHome = env.getSorcerHome();
    }

/*    @Override
    public Process2 startProcess() throws IOException {
    //...
        return super.startProcess();
    }*/

    @Override
    protected void updateEnvironment(Map<String, String> env) {
        super.updateEnvironment(env);
        env.put(E_SORCER_HOME, sorcerHome);
        env.put(E_RIO_HOME, new File(sorcerHome, "lib/rio").getPath());
    }
}
