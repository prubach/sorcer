package sorcer.util;
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


import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Rafał Krupiński
 */
public class ConfigurableThreadFactory implements ThreadFactory {
    private ThreadFactory threadFactory;
    private boolean daemon = false;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public ConfigurableThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    public ConfigurableThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = threadFactory.newThread(runnable);
        thread.setDaemon(daemon);
        //thread.setContextClassLoader();
        //thread.setPriority();

        if (uncaughtExceptionHandler == null)
            uncaughtExceptionHandler = new LoggingExceptionHandler();
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);

        return thread;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }
}
