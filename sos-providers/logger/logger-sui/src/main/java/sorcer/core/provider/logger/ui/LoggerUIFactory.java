package sorcer.core.provider.logger.ui;
/**
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

import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.ui.serviceui.UIDescriptorFactory;
import sorcer.ui.serviceui.UIFrameFactory;
import sorcer.util.Artifact;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.GenericUtil;
import sorcer.util.Sorcer;

import java.net.URL;

/**
 * @author Rafał Krupiński
 */
public class LoggerUIFactory {
    /**
     * Returns a service UI descriptor for LoggerManagerUI. The service
     * UI allows for viewing remote logs of selected providers.
     */
    public static UIDescriptor getMainUIDescriptor() {
        UIDescriptor uiDesc = null;
        try {
            URL[] uiUrls = new URL[]{
                    GenericUtil.toArtifactUrl(Sorcer.getCodebaseRoot(), Artifact.sorcer("logger-sui").toString()),
                    GenericUtil.toArtifactUrl(Sorcer.getCodebaseRoot(), Artifact.sorcer("sorcer-ui").toString()),
                    GenericUtil.toArtifactUrl(Sorcer.getCodebaseRoot(), ArtifactCoordinates.coords("com.sorcersoft.river:serviceui:3.0-M1").toString())
            };

            uiDesc = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                    new UIFrameFactory(uiUrls,
                            LoggerFrameUI.class
                                    .getName(),
                            "Log Viewer",
                            //new URL(SorcerEnv.getWebsterUrl() + "/logger.html")
                            null
                    ));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uiDesc;
    }
}
