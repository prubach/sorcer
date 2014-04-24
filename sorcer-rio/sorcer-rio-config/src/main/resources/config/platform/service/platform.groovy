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


package config.platform.service

import com.sun.jini.start.ServiceDescriptor
import sorcer.boot.ResolvingServiceDescriptor
import sorcer.core.SorcerEnv

import static sorcer.core.SorcerConstants.SORCER_VERSION

/**
 * this script defines a class that implicitly extends {@link sorcer.boot.platform.PlatformDescriptor}
 */

ServiceDescriptor[] getPlatformServices() {
    def policy = new File(SorcerEnv.homeDir, "configs/sorcer.policy")
    return [
            new ResolvingServiceDescriptor(
                    "org.sorcersoft.sorcer:dbp-api:" + SORCER_VERSION,
                    policy,
                    "org.sorcersoft.sorcer:dbp-handler:" + SORCER_VERSION,
                    "sorcer.util.bdb.HandlerInstaller"
            ),
            new ResolvingServiceDescriptor(
                    "org.sorcersoft.sorcer:sos-exertlet-sui:" + SORCER_VERSION,
                    policy,
                    "org.sorcersoft.sorcer:exertlet-platform:" + SORCER_VERSION,
                    "sorcer.ui.exertlet.ExertletUiModule"
            ),
            new ResolvingServiceDescriptor(
                    "org.sorcersoft.sorcer:logger-api:" + SORCER_VERSION,
                    policy,
                    "org.sorcersoft.sorcer:logger-platform:" + SORCER_VERSION,
                    "sorcer.platform.logger.RemoteLoggerInstaller"
            )
    ]
}
