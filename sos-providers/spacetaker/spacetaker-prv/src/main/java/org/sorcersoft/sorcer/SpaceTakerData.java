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

package org.sorcersoft.sorcer;

import sorcer.core.SorcerConstants;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.provider.outrigger.SpaceTakerConfiguration;
import sorcer.core.service.IServiceBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerData {
    public IServiceBuilder provider;
    public SpaceTakerConfiguration config;
    public String spaceName;
    public String spaceGroup;

    public Set<ExertionEnvelop> entries;

    public SpaceTakerData(IServiceBuilder builder, SpaceTakerConfiguration config) {
        this.provider = builder;
        this.config = config;

        entries = new HashSet<ExertionEnvelop>();
        for (Class iface : config.interfaces)
            entries.add(ExertionEnvelop.getTemplate(iface, SorcerConstants.ANY));
        if (!config.matchInterfaceOnly)
            for (Class iface : config.interfaces)
                entries.add(ExertionEnvelop.getTemplate(iface, builder.getName()));
    }
}
