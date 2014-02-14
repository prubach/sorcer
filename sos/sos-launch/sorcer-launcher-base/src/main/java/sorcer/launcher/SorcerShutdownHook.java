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

package sorcer.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Rafał Krupiński
 */
public class SorcerShutdownHook implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SorcerShutdownHook.class);
    public static final SorcerShutdownHook instance = install();

    private Thread shutdownHook;
    private Deque<WeakReference<Launcher>> launchers = new LinkedList<WeakReference<Launcher>>();

    private static SorcerShutdownHook install() {
        SorcerShutdownHook result = new SorcerShutdownHook();
        result.shutdownHook = new Thread(result, "SORCER shutdown hook");
        Runtime.getRuntime().addShutdownHook(result.shutdownHook);
        return result;
    }

    @Override
    public void run() {
        WeakReference<Launcher> o;
        while ((o = launchers.pollLast()) != null) {
            Launcher launcher = o.get();
            if (launcher != null) {
                log.info("Stopping {}", launcher);
                launcher.stop();
            }
        }
    }

    public void add(Launcher launcher) {
        launchers.add(new WeakReference<Launcher>(launcher));
    }
}
