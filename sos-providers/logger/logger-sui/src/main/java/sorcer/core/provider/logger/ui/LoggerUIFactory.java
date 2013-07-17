package sorcer.core.provider.logger.ui;
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


import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;
import sorcer.ui.serviceui.UIDescriptorFactory;
import sorcer.ui.serviceui.UIFrameFactory;
import sorcer.util.Artifact;

import java.net.URL;

/**
 * @author Rafał Krupiński
 */
public class LoggerUIFactory {
    /**
     * Returns a service UI descriptor for LoggerManagerUI. The service
     * UI allows for viewing remote logs of selected providers.
     *
     * @see sorcer.core.provider.ServiceProvider#getMainUIDescriptor()
     */
    public static UIDescriptor getMainUIDescriptor() {
        UIDescriptor uiDesc = null;
        try {
            URL uiUrl = Resolver.resolveAbsoluteURL(new URL(SorcerEnv.getWebsterUrl()), Artifact.sorcer("logger-sui"));
            URL helpUrl = new URL(SorcerEnv.getWebsterUrl() + "/logger.html");

            uiDesc = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                    new UIFrameFactory(new URL[]{uiUrl},
                            LoggerFrameUI.class
                                    .getName(),
                            "Log Viewer",
                            helpUrl));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uiDesc;
    }
}
