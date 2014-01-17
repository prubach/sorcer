package sorcer.launcher;
/**
 *
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.util.Process2;

import java.io.File;
import java.io.IOException;

import static sorcer.core.SorcerConstants.E_RIO_HOME;
import static sorcer.core.SorcerConstants.E_SORCER_HOME;

/**
 * Sorcer-specific process builder
 *
 * @author Rafał Krupiński
 */
public class SorcerProcessBuilder extends JavaProcessBuilder {
    protected String sorcerHome;

    public SorcerProcessBuilder(String sorcerHome) {
        super("SORCER");
        this.sorcerHome = sorcerHome;
    }

    public SorcerProcessBuilder(SorcerEnv env) {
        this(env.getSorcerHome());
    }

    public void setRioHome(String rioHome) {
        environment.put(E_RIO_HOME, rioHome);
    }

    @Override
    public Process2 startProcess() throws IOException {
        environment.put(E_SORCER_HOME, sorcerHome);
        if (!environment.containsKey(E_RIO_HOME)) {
            String rioHome = System.getenv(SorcerConstants.E_RIO_HOME);
            if (rioHome == null) rioHome = new File(sorcerHome, "lib/rio").getPath();
            environment.put(E_RIO_HOME, rioHome);
        }
        return super.startProcess();
    }
}
