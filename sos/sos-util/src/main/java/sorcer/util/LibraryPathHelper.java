/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.util;

import java.lang.reflect.Field;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.io.File.pathSeparator;
import static sorcer.util.JavaSystemProperties.LIBRARY_PATH;

/**
 * LibraryPathHelper implements a Set&lt;String> synchronized with system property java.library.path. each change is immediately reflected in the Java runtime. All Set operations are synchronized.
 *
 * @author Rafał Krupiński
 */
public class LibraryPathHelper extends AbstractSet<String> {
    private static final Logger log = LoggerFactory.getLogger(LibraryPathHelper.class);

    static Field fieldSysPath;

    static {
        fieldSysPath = prepareSysPathField();
    }

    private static Set<String> currentElements() {
        Set<String> result = new HashSet<String>();
        Collections.addAll(result, System.getProperty(LIBRARY_PATH, "").split(pathSeparator));
        return result;
    }

    private static Field prepareSysPathField() {
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            return fieldSysPath;
        } catch (Exception e) {
            throw new RuntimeException("Could not update java.library.path system property", e);
        }
    }

    public static Set<String> getLibraryPath() {
        return Collections.synchronizedSet(new LibraryPathHelper());
    }

    protected void updateLibraryPath(Set<String> newPaths) {
        Set<String> current = currentElements();
        if (current.equals(newPaths)) return;
        reportSetDifference(current, newPaths);

        System.setProperty(LIBRARY_PATH, StringUtils.join(newPaths, pathSeparator));

        try {
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Could not update java.library.path system property", e);
        }
    }

    private void reportSetDifference(Collection<String> current, Collection<String> updated) {
        if (!log.isInfoEnabled()) return;
        StringBuilder message = new StringBuilder("Modifying ").append(LIBRARY_PATH).append(": ");
        message.append(oneWayDiff(current, updated))
                .append(" -> ")
                .append(oneWayDiff(updated, current));

        log.info(message.toString());
    }

    private String oneWayDiff(Collection<String> old, Collection<String> updated) {
        List<String> result = new ArrayList<String>(old.size());
        for (String elem : old) {
            result.add(updated.contains(elem) ? elem : "[" + elem + "]");
        }
        return StringUtils.join(result, pathSeparator);
    }

    @Override
    public Iterator<String> iterator() {
        class OnlineIterator implements Iterator<String> {
            private Set<String> set;
            private Iterator<String> i;

            OnlineIterator(Set<String> set) {
                this.set = set;
                i = set.iterator();
            }

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public String next() {
                return i.next();
            }

            @Override
            public void remove() {
                i.remove();
                updateLibraryPath(set);
            }
        }
        return new OnlineIterator(currentElements());
    }

    @Override
    public int size() {
        return currentElements().size();
    }

    @Override
    public boolean add(String s) {
        Set<String> set = currentElements();
        boolean success = set.add(s);
        if (success) {
            updateLibraryPath(set);
        }
        return success;
    }

    @Override
    public boolean remove(Object o) {
        Set<String> set = currentElements();
        boolean success = set.remove(o);
        if (success) {
            updateLibraryPath(set);
        }
        return success;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Set<String> strings = currentElements();
        boolean set = strings.removeAll(c);
        updateLibraryPath(strings);
        return set;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        Set<String> strings = currentElements();
        boolean set = strings.addAll(c);
        updateLibraryPath(strings);
        return set;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<String> set = currentElements();
        boolean result = set.retainAll(c);
        updateLibraryPath(set);
        return result;
    }
}
