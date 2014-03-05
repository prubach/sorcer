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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public interface ILauncher {
    void start() throws IOException;

    void stop();

    void preConfigure();

    void setLogDir(File logDir);

    void setHome(File home);

    void setConfigs(List<String> configs);

    void setConfigDir(File config);

    void setSorcerListener(SorcerListener listener);

    void setProfile(String profile) throws IOException;

    void setWaitMode(WaitMode waitMode);

    void setRioConfigs(List<String> rioConfigs);

    public enum WaitMode {
        no, start, end
    }
}
