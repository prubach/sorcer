/*
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

package sorcer.launcher;


import sorcer.util.Process2;

import static java.lang.System.out;

/**
 * @author Rafał Krupiński
 */
public class ProcessDestroyer implements Runnable {
    private Process2 process;
    private boolean doKill = true;
    private String name;

    public ProcessDestroyer(Process2 sorcerProcess, String name) {
        this.name = name;
        assert sorcerProcess != null;
        process = sorcerProcess;
    }

    @Override
    public void run() {
        if (!doKill) return;

        if (process.running()) {
            out.print("Killing " + name + " process");
            int exit = process.destroyAndExitCode();
            out.println("; exit code = " + exit);
        } else {
            out.println("The " + name + " process is already down");
        }
    }

    public void setDoKill(boolean doKill) {
        this.doKill = doKill;
    }
}
