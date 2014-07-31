package sorcer.rio.util;

import org.apache.commons.lang3.StringUtils;
import org.rioproject.config.PlatformCapabilityConfig;
import org.rioproject.resolver.Artifact;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import sorcer.resolver.SorcerResolver;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Extend PlatformCapabilityConfig with<ul>
 * <li>provide collection of artifacts
 * <li>artifact coordinates without version
 * <li>option not to resolve transitive dependencies
 *
 * @author Rafał Krupiński
 */
public class SorcerCapabilityDescriptor extends PlatformCapabilityConfig {
    private static Resolver resolver;

    public SorcerCapabilityDescriptor(String name, String version, String classpath) throws ResolverException {
        super(name, version, classpath);
    }

    public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
                                      String classpath) throws ResolverException {
        super(name, version, description, manufacturer, getClasspath(classpath, version, true));
    }

    public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
                                      Collection<String> classpath) throws ResolverException {
        super(name, version, description, manufacturer, getClasspath(classpath, version, true));
    }

    public SorcerCapabilityDescriptor(String name, String version, String description, String manufacturer,
                                      Collection<String> classpath, boolean transitive) throws ResolverException {
        super(name, version, description, manufacturer, getClasspath(classpath, version, transitive));
    }

    protected static String getClasspath(Collection<String> classpath, String version, boolean transitive) throws ResolverException {
        return StringUtils.join(getClasspath0(classpath, version, transitive), File.pathSeparator);
    }

    private static Collection<String> getClasspath0(Collection<String> classpath, String version, boolean transitive) throws ResolverException {
        Collection<String> result = new HashSet<String>();
        for (String entry : classpath) {
            result.addAll(resolve(entry, version, transitive));
        }
        return result;
    }

    protected static String getClasspath(String classpath, String version, boolean transitive) throws ResolverException {
        Collection<String> result = new HashSet<String>();
        result.addAll(resolve(classpath, version, transitive));
        return StringUtils.join(result, File.pathSeparator);
    }

    protected static Collection<String> resolve(String entry, String version, boolean transitive) throws ResolverException {
        String coords = entry;
        if (!Artifact.isArtifact(coords))
            coords = entry + ":" + version;
        if (Artifact.isArtifact(coords))
            return resolve(coords, transitive);

        return getClasspath0(Arrays.asList(StringUtils.split(entry, File.separatorChar)), version, transitive);
    }

    protected static Collection<String> resolve(String entry, boolean transitive) throws ResolverException {
        if (transitive)
            return Arrays.asList(resolver.getClassPathFor(entry));
        else {
            URL location = resolver.getLocation(entry, new Artifact(entry).getType());
            try {
                return Collections.singleton(new File(location.toURI()).getPath());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(entry + " resolved to invalid URL " + location, e);
            }
        }
    }

    static {
        try {
            resolver = SorcerResolver.getResolver();
        } catch (ResolverException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
