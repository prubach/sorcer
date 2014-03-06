package sorcer.co.tuple;
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


import java.util.Arrays;

/**
* @author Rafał Krupiński
*/
public class ParameterTypes extends Path {
    private static final long serialVersionUID = 1L;
    public Class[] parameterTypes;

    public ParameterTypes(Class... parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

@Override
    public String toString() {
        return "parameterTypes: " + Arrays.toString(parameterTypes);
    }
}
