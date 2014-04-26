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

package sorcer.ui.exertlet;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.sun.jini.start.ServiceDescriptor;
import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.boot.util.ServiceDescriptorProcessor;
import sorcer.config.AbstractBeanListener;
import sorcer.config.BeanListener;
import sorcer.core.SorcerEnv;
import sorcer.core.service.IServiceBuilder;
import sorcer.provider.boot.AbstractServiceDescriptor;
import sorcer.ui.serviceui.UIComponentFactory;
import sorcer.ui.serviceui.UIDescriptorFactory;
import sorcer.ui.serviceui.UIFrameFactory;
import sorcer.util.Artifact;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.GenericUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class ExertletUiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NetletInjector.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), BeanListener.class).addBinding().to(NetletInjector.class);
        Multibinder.newSetBinder(binder(), ServiceDescriptorProcessor.class).addBinding().to(NetletInjector.class);
    }

    static class NetletInjector extends AbstractBeanListener implements ServiceDescriptorProcessor {
        private static Logger log = LoggerFactory.getLogger(NetletInjector.class);

        private final URL serviceui = GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), ArtifactCoordinates.coords("net.jini.lookup:serviceui:2.2.2").toString());
        private URL[] urls = new URL[]{
                GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), Artifact.sorcer("sos-exertlet-sui").toString()),
                GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), Artifact.sorcer("sorcer-ui").toString()),
                serviceui
        };

        public NetletInjector() {
        }

        @Override
        public void preProcess(IServiceBuilder provider) {
            try {
                // URL exportUrl, String className, String name, String helpFilename
                UIDescriptor uiDesc2 = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                        new UIFrameFactory(urls, "sorcer.ui.exertlet.NetletUI", "Exertlet Editor", null)
                );
                provider.addAttribute(uiDesc2);
            } catch (IOException ex) {
                log.error("getServiceUI", ex);
            }
            try {
                UIDescriptor descriptor = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                        new UIComponentFactory(urls, "sorcer.core.provider.ui.ProviderUI")
                );
                provider.addAttribute(descriptor);
            } catch (IOException ex) {
                log.error("getServiceUI", ex);
            }
        }

        @Override
        public void process(ServiceDescriptor descriptor) {
            if (descriptor instanceof AbstractServiceDescriptor)
                process((AbstractServiceDescriptor) descriptor);
        }

        /**
         * @param descriptor AbstractSorcerDescriptor
         */
        public void process(AbstractServiceDescriptor descriptor) {
            Set<URL> codebase = descriptor.getCodebase();
            if (codebase == null || codebase.isEmpty())
                return;
            codebase.add(serviceui);
        }
    }
}
