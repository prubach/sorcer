package sorcer.resolver;
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


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.JavaSystemProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class ProjectArtifactResolver implements ArtifactResolver {
    private static final Logger log = LoggerFactory.getLogger(ProjectArtifactResolver.class);

    private List<File> roots = new ArrayList<File>();

    public ProjectArtifactResolver() throws IOException {
        File homeDir = SorcerEnv.getHomeDir().getCanonicalFile();
        File userDir = new File(System.getProperty(JavaSystemProperties.USER_DIR)).getCanonicalFile();
        File extDir = SorcerEnv.getExtDir().getCanonicalFile();

        String homePath = homeDir.getPath();
        String userPath = userDir.getPath();
        String extPath = extDir.getPath();

        //if one directory is ancestor of another, use only the ancestor
        if (homeDir.equals(userDir) || homePath.startsWith(userPath))
            roots.add(userDir);
        else if (userPath.startsWith(homePath))
            roots.add(homeDir);
        else {
            roots.add(userDir);
            roots.add(homeDir);
        }
        //if SORCER_EXT points to a different directory add it
        if (!extPath.equals(homePath))
            roots.add(extDir);
    }

    @Override
    public String resolveAbsolute(ArtifactCoordinates artifactCoordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolveAbsolute(String artifactCoordinates) {
        for (File root : roots) {
            File result = resolveRelative(root, artifactCoordinates);
            if (result != null)
                return result.getPath();
        }
        return null;
    }

    @Override
    public String resolveRelative(ArtifactCoordinates artifactCoordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolveRelative(String artifactCoordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRootDir() {
        return roots.get(0).getPath();
    }

    @Override
    public String resolveSimpleName(String simpleName, String packaging) {
        return null;
    }

    protected File resolveRelative(File root, String artifactId) {
        Collection<File> files = FileUtils.listFiles(root, new ArtifactIdFileFilter(artifactId), DirectoryFileFilter.INSTANCE);
        if (files.size() > 0) {
            File result = files.iterator().next();
            if (files.size() > 1) {
                log.warn("Found {} files in {} possibly matching artifactId, using {}", files.size(), root, result);
                log.debug("Files found: {}", files);
            }
            return result;
        }
        return null;
    }
}

class ArtifactIdFileFilter extends AbstractFileFilter {
    private String artifactId;

    public ArtifactIdFileFilter(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public boolean accept(File dir, String name) {
        String parent = dir.getName();
        String grandParent = dir.getParentFile().getName();
        return
                new File(dir, name).isFile() && name.startsWith(artifactId + "-") && name.endsWith(".jar") && (
                        //check development structure
                        "target".equals(parent)
                                //check repository just in case
                                || artifactId.equals(grandParent)
                )
                        //check distribution structure
                        || "lib".equals(grandParent) && (artifactId + ".jar").equals(name)
                ;
    }
}
