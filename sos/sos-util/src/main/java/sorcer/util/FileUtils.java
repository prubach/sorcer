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

package sorcer.util;

import java.io.File;

/**
 * @author Rafał Krupiński
 */
public class FileUtils {
    /**
     * Same as new File(parent, child), but if child is absolute, return new File(child)
     */
    public static File getFile(File parent, String child) {
        if (child == null)
            return parent;
        File result = new File(child);
        if (result.isAbsolute())
            return result;
        else
            return new File(parent, child);
    }
}
