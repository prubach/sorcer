package sorcer.service;
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


import sorcer.core.exertion.NetTask;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ObjectSignature;

/**
 * @author Rafał Krupiński
 */
public class TaskFactory {
    public static Task task(String name, Signature signature, Context context)
            throws SignatureException {
        if (signature instanceof NetSignature) {
            return new NetTask(name, signature, context);
        } else if (signature instanceof ObjectSignature) {
            return new ObjectTask(name, signature, context);
        } else
            return new Task(name, signature, context);
    }
}
