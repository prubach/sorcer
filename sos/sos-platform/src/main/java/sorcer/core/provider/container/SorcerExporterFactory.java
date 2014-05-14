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

package sorcer.core.provider.container;

import sorcer.container.jeri.ExporterFactory;
import sorcer.jini.jeri.SorcerILFactory;

import java.util.Map;

/**
 * Exporter factory creating BasicJeriExporter with SorcerILFactory
 *
 * @author Rafał Krupiński
 */
public class SorcerExporterFactory extends ExporterFactory {
    public SorcerExporterFactory(Map<Class, Object> beanMap, ClassLoader classLoader) {
        super(new SorcerILFactory(beanMap, classLoader));
    }
}
