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

package sorcer.container.jeri;

import net.jini.jeri.BasicJeriExporter;

import javax.inject.Provider;

/**
 * @author Rafał Krupiński
 */
public class ExporterFactories {
    public static final Provider<BasicJeriExporter> EXPORTER = new ExporterFactory();
    public static final Provider<BasicJeriExporter> TRUSTED = ExporterFactory.trusted(null, null);

    public static BasicJeriExporter getBasicTcp() {
        return EXPORTER.get();
    }

    public static BasicJeriExporter getTrustedTcp() {
        return TRUSTED.get();
    }

}
