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

package sorcer.resolver;

import org.rioproject.resolver.RemoteRepository;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.util.FileUtils;
import sorcer.util.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class SorcerResolver implements Resolver {
    final protected static Logger log = LoggerFactory.getLogger(SorcerResolver.class);

    private static SorcerResolver inst;

    final Resolver resolver;

    private final Map<String, String[]> cache = new HashMap<String, String[]>();
    private long storeLastMod;
    private File store = SorcerEnv.getEnvironment().getResolverCache();

    public static synchronized Resolver getResolver() throws ResolverException {
        if (inst == null)
            inst = new SorcerResolver(ResolverHelper.getResolver());
        return inst;
    }

    {
        updateCacheStore(!SorcerEnv.getEnvironment().isClearResolverCache());

        Thread t = new Thread(new CacheStorer(), "Cache storer");
        t.setDaemon(true);
        t.start();
    }

    public SorcerResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String[] getClassPathFor(String artifact) throws ResolverException {
        if (cache.containsKey(artifact))
            return cache.get(artifact);
        return updateCache(artifact, resolver.getClassPathFor(artifact));
    }

    @Override
    public String[] getClassPathFor(String artifact, RemoteRepository[] repositories) throws ResolverException {
        if (cache.containsKey(artifact))
            return cache.get(artifact);
        return updateCache(artifact, resolver.getClassPathFor(artifact, repositories));
    }

    private String[] updateCache(String artifact, String[] classPath) {
        if (!Arrays.deepEquals(classPath, cache.get(artifact))) {
            cache.put(artifact, classPath);
            synchronized (cache) {
                cache.notify();
            }
        }
        return classPath;
    }

    @Override
    public URL getLocation(String artifact, String artifactType) throws ResolverException {
        return resolver.getLocation(artifact, artifactType);
    }

    @Override
    public URL getLocation(String artifact, String artifactType, RemoteRepository[] repositories) throws ResolverException {
        return resolver.getLocation(artifact, artifactType, repositories);
    }

    @Override
    public Collection<RemoteRepository> getRemoteRepositories() {
        return resolver.getRemoteRepositories();
    }

    @Override
    @Deprecated
    public String[] getClassPathFor(String artifact, File pom, boolean download) throws ResolverException {
        return resolver.getClassPathFor(artifact, pom, download);
    }

    protected void updateCacheStore(boolean read) {
        Closeable file = null;
        FileLock lock = null;
        try {
            FileChannel ch;
            // new RandomAccessFile should throw FileNotFoundException if the file doesn't exist
            boolean myRead = read && store.exists();
            if (myRead) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(store, "rw");
                file = randomAccessFile;
                ch = randomAccessFile.getChannel();
            } else {
                // check if store exists, if not new empty file is created.
                store.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(store);
                file = fileOutputStream;
                ch = fileOutputStream.getChannel();
            }

            lock = ch.lock();

            if (myRead) {
				long storeNewLastMod = store.lastModified();
                if (store.length() > 0 && storeNewLastMod > storeLastMod) {
                    Map<String, String[]> newMap = FileUtils.fromFile(Channels.newInputStream(ch));
                    cache.putAll(newMap);
                    storeLastMod = storeNewLastMod;
                }
            }
            FileUtils.toFile(cache, Channels.newOutputStream(ch));
        } catch (Exception e) {
            log.debug("Error", e);
        } finally {
            if (lock != null)
                try {
                    lock.release();
                } catch (IOException e1) {
                    log.warn("Error", e1);
                }
            IOUtils.closeQuietly(file);
        }
    }

    class CacheStorer implements Runnable {
        @Override
        public void run() {
            synchronized (cache) {
                while (true)
                    try {
                        cache.wait();
                        updateCacheStore(true);
                    } catch (InterruptedException e) {
                        log.warn("Interrupted", e);
                    }
            }
        }
    }
}
