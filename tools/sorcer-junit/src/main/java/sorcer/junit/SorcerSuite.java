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

package sorcer.junit;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class SorcerSuite extends Suite {
    public SorcerSuite(Class<?> klass) throws InitializationError {
        super(klass, getRunners(klass));
    }

    private static List<Runner> getRunners(Class<?> klass) throws InitializationError {
        SorcerServiceConfigurations sorcerServiceConfigurations = klass.getAnnotation(SorcerServiceConfigurations.class);
        SorcerServiceConfiguration sorcerServiceConfiguration = klass.getAnnotation(SorcerServiceConfiguration.class);
        if (sorcerServiceConfiguration == null && sorcerServiceConfigurations == null)
            return Arrays.asList((Runner) new SorcerRunner(klass, null));

        if (sorcerServiceConfiguration != null && sorcerServiceConfigurations != null)
            throw new InitializationError("Both @SorcerServiceConfigurations and @SorcerServiceConfiguration is allowed");

        SorcerServiceConfiguration[] configurations;
        if (sorcerServiceConfigurations != null) {
            configurations = sorcerServiceConfigurations.value();
            if (configurations == null || configurations.length == 0)
                throw new InitializationError("@SorcerServiceConfigurations annotation without any @SorcerServiceConfiguration entries");
        } else
            configurations = new SorcerServiceConfiguration[]{sorcerServiceConfiguration};

        List<Runner> result = new ArrayList<Runner>(configurations.length);
        for (SorcerServiceConfiguration configuration : configurations) {
            if (configuration.value() == null || configuration.value().length == 0)
                throw new InitializationError("@SorcerServiceConfiguration annotation without any configuration files");
            result.add(new SorcerRunner(klass, configuration));
        }
        return result;
    }
}
