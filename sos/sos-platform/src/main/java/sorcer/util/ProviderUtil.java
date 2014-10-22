package sorcer.util;

import edu.emory.mathcs.util.classloader.URIClassLoader;
import org.rioproject.loader.ServiceClassLoader;
import org.rioproject.resolver.ResolverHelper;
import org.rioproject.url.artifact.ArtifactURLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.provider.Provider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * SORCER class
 * User: prubach
 * Date: 15.07.14
 */
public class ProviderUtil {

    protected final static Logger logger = LoggerFactory.getLogger(ProviderUtil.class);

    public static final String LOCAL_JARS = "LOCAL";
    public static final String REMOTE_JARS = "REMOTE";

    public static void destroy(String providerName, Class serviceType) {
        Provider prv = (Provider) ProviderLookup.getService(providerName,
                serviceType);
        if (prv != null)
            try {
                prv.destroy();
            } catch (Throwable t) {
                // a dead provider will be not responding anymore
                //t.printStackTrace();
            }
    }

    public static void destroyNode(String providerName, Class serviceType) {
        Provider prv = (Provider) ProviderLookup.getService(providerName,
                serviceType);
        if (prv != null)
            try {
                prv.destroyNode();
            } catch (Throwable t) {
                // a dead provider will be not responding anymore
                //t.printStackTrace();
            }
    }

    // this method exits the jvm if the file or directory is not readable; the exit is
    // necessary for boot strapping providers since exceptions in provider constructors
    // are simply caught and ignored...exit brings the provider down, which is good.
    public static void checkFileExistsAndIsReadable(File file, Provider sp) {

        try {

            if(!file.exists()) {
                System.out.println("***error: file does not exist = "
                        + file.getAbsolutePath());
                if (sp != null) sp.destroy();
                throw new IOException("***error: file does not exist = "
                        + file.getAbsolutePath());

            }

            if (!file.canRead()){
                System.out.println("***error: file does not have read permission = "
                        + file.getAbsolutePath());
                if (sp != null) sp.destroy();
                throw new IOException("***error: file does not have read permission = "
                        + file.getAbsolutePath());
            }

        } catch (IOException e) {
            System.out.println("***error: " + e.toString()
                    + "; problem with file = " + file.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
            throw new RuntimeException(e);
        }
    }


    /**
     * Get the aggregate classpath, using ClassLoader hierarchy
     *
     * @param loader The ClassLoader to use as a starting point. if {@code null}, the {@code Thread}'s context
     *               classloader will be used.
     *
     * @return A {@link java.util.Map} containing local and remote jars. If local jars (file based) are identified, the
     * local jars values is a {@code File.separator} delimited string of classpath artifacts. If remote jars
     * are identified (http based), the remote jars values is a space delimited string of classpath artifacts.
     *
     * @throws Exception
     */
    public static Map<String, String> getAggregateClassPath(
            final ClassLoader loader) throws Exception {
        Map<String, String> jarMap = new HashMap<String, String>();
        List<ClassLoader> loaderList = new ArrayList<ClassLoader>();
        System.out.println("========================================");
        System.out.println(loader.getClass().getName());
        System.out.println("========================================");
        ClassLoader currentLoader = loader == null ? Thread.currentThread()
                .getContextClassLoader() : loader;
        StringBuilder fileUrlBuilder = new StringBuilder();
        StringBuilder httpUrlBuilder = new StringBuilder();
        while (currentLoader != null
                && !ClassLoader.getSystemClassLoader().equals(currentLoader)) {
            loaderList.add(currentLoader);
            currentLoader = currentLoader.getParent();
        }
        java.util.Collections.reverse(loaderList);
        for (ClassLoader cl : loaderList) {
            URL[] urls = null;
            if (cl instanceof ServiceClassLoader) {
                urls = ((ServiceClassLoader) cl).getSearchPath();
            } else if (cl instanceof URIClassLoader) {
                ClassLoader cCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
                try {
                    urls = ((URIClassLoader) cl).getURLs();
                } finally {
                    Thread.currentThread().setContextClassLoader(cCL);
                }
            } else if (cl instanceof URLClassLoader) {
                ClassLoader cCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
                /* Subclasses of URLClassLoader may override getURLs(), in order to
                 * return the URLs of the provided export codebase. This is a workaround to get
                 * the search path (not the codebase) of the URLClassLoader. We get the ucp property from the
                 * URLCLassLoader (of type sun.misc.URLClassPath), and invoke the sun.misc.URLClassPath.getURLs()
                 * method */
                try {
                    try {
                        for(java.lang.reflect.Field field : URLClassLoader.class.getDeclaredFields()) {
                            if(field.getName().equals("ucp")) {
                                field.setAccessible(true);
                                Object ucp = field.get(cl);
                                Method getURLs = ucp.getClass().getMethod("getURLs");
                                urls = (URL[])getURLs.invoke(ucp);
                                break;
                            }
                        }
                    } catch(Exception e) {
                        logger.warn("Could not get or access field \"ucp\", just call getURLs()", e);
                        urls = ((URLClassLoader) cl).getURLs();
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(cCL);
                }
            }

            if (urls != null) {
                for (URL url : urls) {
                    logger.debug("Processing url: " + url.toExternalForm());
                    if (url.getProtocol().equals("artifact")) {
                        String artifact = new ArtifactURLConfiguration(
                                url.toExternalForm()).getArtifact();

                        for (String item : ResolverHelper.getResolver()
                                .getClassPathFor(artifact)) {
                            if (fileUrlBuilder.length() > 0) {
                                fileUrlBuilder.append(File.pathSeparator);
                            }
                            fileUrlBuilder.append(item);
                        }

                    } else if (url.getProtocol().startsWith("http")) {
                        if (httpUrlBuilder.length() > 0) {
                            httpUrlBuilder.append(" ");
                        }
                        httpUrlBuilder.append(url.toExternalForm());
                    } else {
                        File jar = new File(url.toURI());
                        if (fileUrlBuilder.length() > 0) {
                            fileUrlBuilder.append(File.pathSeparator);
                        }
                        fileUrlBuilder.append(jar.getAbsolutePath());
                    }
                }
            }
        }

		/*
		 * Build the table only if we have file URLs. This happens if the
		 * ClassLoader is a Rio classloader, and triggers the approach for the
		 * DelegateLauncher to create a URLClassLoader. If there are no file
		 * URLs. the CLASSPATH environment variable will be used.
		 */
        if (fileUrlBuilder.length() > 0) {
            fileUrlBuilder.insert(0, File.pathSeparator);
            fileUrlBuilder.insert(0, System.getProperty("java.class.path"));
            jarMap.put(LOCAL_JARS, fileUrlBuilder.toString());

            if (httpUrlBuilder.length() > 0) {
                jarMap.put(REMOTE_JARS, httpUrlBuilder.toString());
            }
        }

        return jarMap;
    }


}
