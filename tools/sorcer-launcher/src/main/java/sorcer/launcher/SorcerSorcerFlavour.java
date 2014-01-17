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

import java.util.Arrays;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class SorcerSorcerFlavour extends SorcerFlavour {
    @Override
    public String getMainClass() {
        return "sorcer.boot.ServiceStarter";
    }

    @Override
    protected String[] getFlavourSpecificClassPath() {
        return new String[]{
                "net.jini:jsk-resources",
                "org.rioproject:rio-lib",
                "org.sorcersoft.sorcer:util-rio",
                //"org.sorcersoft.sorcer:sos-webster",
                "com.google.guava:guava:15.0",
                "commons-io:commons-io"
        };
    }

    @Override
    public List<String> getDefaultConfigs() {
        return Arrays.asList("configs/sorcer-boot.config");
    }

    @Override
    public OutputConsumer getConsumer() {
        return new SorcerOutputConsumer();
    }
}
