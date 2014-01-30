/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.launcher.process;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.util.Process2;

import java.util.List;

/**
 * Runnable that destroys all processes in a list passed in the constructor
 *
 * @author Rafał Krupiński
 */
public class ProcessDestroyer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProcessDestroyer.class);
    private List<Process> children;

    public ProcessDestroyer(List<Process> children) {
        assert children != null;
        this.children = children;
    }

    @Override
    public void run() {
        for (Process child : children) {
            Process2 p2;
            if ((child instanceof Process2)) {
                p2 = (Process2) child;
            } else {
                p2 = new Process2(child, "child process");
            }

            log.info("Killing {}", p2);
            try {
                int exit = p2.destroyAndExitCode();
                log.info("Exit code for {} is {}", p2, exit);
            } catch (InterruptedException e) {
                log.warn("Interrupted on {}", p2, e);
            }
        }
    }
}
