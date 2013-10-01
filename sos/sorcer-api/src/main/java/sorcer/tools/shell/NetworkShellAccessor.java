package sorcer.tools.shell;
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


import java.lang.reflect.InvocationTargetException;

/**
 * @author Rafał Krupiński
 */
public class NetworkShellAccessor {
    public static INetworkShell getNetworkShell(ClassLoader cl) {
        try {
            Class<?> shellClass = Class.forName("sorcer.tools.shell.NetworkShell", false, cl);
            return (INetworkShell) shellClass.getMethod("getInstance").invoke(null);
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new RuntimeException(cause);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
