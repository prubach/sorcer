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

package sorcer.container.core;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafał Krupiński
 */
public class TypeMatcher extends AbstractMatcher<TypeLiteral<?>> {
    private static final Logger log = LoggerFactory.getLogger(TypeMatcher.class);
    private Class<?> type;

    public TypeMatcher(Class type) {
        this.type = type;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        boolean result = type.isAssignableFrom(typeLiteral.getRawType());
        log.debug("match type: {} {}", typeLiteral, result);
        return result;
    }
}
