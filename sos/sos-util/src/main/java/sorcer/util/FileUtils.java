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

import sorcer.org.apache.commons.io.input.ClassLoaderObjectInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
        if (result.isAbsolute() || parent == null)
            return result;
        else
            return new File(parent, child);
    }

    public static File getDir(String path) {
        File file = getFile(path);
        return (file != null && file.isDirectory()) ? file : null;
    }

    public static File getFile(String path) {
        if (path == null)
            return null;
        File file = new File(path);
        return file.exists() ? file : null;
    }

    public static <T> T fromFile(File serialized) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ClassLoaderObjectInputStream(Thread.currentThread().getContextClassLoader(), new FileInputStream(serialized));
            return (T) ois.readObject();
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

    public static void toFile(Object o, File f) throws IOException {
        File parent = f.getParentFile();
        if (!parent.exists() && !parent.mkdirs())
            throw new IOException("Could not create parent dir for " + f);
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(o);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
