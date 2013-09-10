package sorcer.schema;
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


import sorcer.service.Direction;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for java-based context schema. A context schema is a java interface with annotated getters and setters.
 * Sorcer can retrieve list of context paths with type and validate a context against this this list.
 * The provider programmer may convert the context to the interface with methods marked with @Path annotations.
 *
 * @author Rafał Krupiński
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Path {
    public String value();

    public Direction direction = Direction.INOUT;
    public boolean required = true;
}
