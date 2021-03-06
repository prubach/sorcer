/*
 * Copyright 2008 the original author or authors.
 * Copyright 2005 Sun Microsystems, Inc.
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
package sorcer.tools.webster;

import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.resolver.VersionResolver;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.GenericUtil;
import sorcer.util.JavaSystemProperties;
import sorcer.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static sorcer.core.SorcerConstants.CODEBASE_JARS;
import static sorcer.core.SorcerConstants.CODEBASE_SEPARATOR;
import static sorcer.core.SorcerConstants.S_WEBSTER_INTERFACE;

/**
 * Helper class for starting an Internal Webster
 *
 * @author Dennis Reedy and Mike Sobolewski
 */
public class InternalWebster {
    private static Logger logger = LoggerFactory.getLogger(InternalWebster.class.getName());
    private static boolean debug = false;
    public static final String WEBSTER_ROOTS = "sorcer.webster.roots";

    /**
     * Start an internal webster, setting the webster root to the location of
     * SORCER lib-dl directories, and appending exportJars as the codebase jars
     * for the JVM.
     *
     * @param exportJars
     *            The jars to set for the codebase
     *
     * @return The port Webster has been started on
     *
     * @throws IOException
     *             If there are errors creating Webster
     */
    public static Webster startWebster(String... exportJars) throws IOException {
        return startWebster(exportJars, null);
    }

    /**
     * Start an internal webster, setting the webster root to the location of
     * SORCER lib-dl directories, and appending exportJars as the codebase jars
     * for the JVM.
     *
     * @param exportJars
     *            The jars to set for the codebase
     *
     * @return The port Webster has been started on
     *
     * @throws IOException
     *             If there are errors creating Webster
     */
    public static Webster startWebster(String[] exportJars, String[] websterRoots) throws IOException {
        String codebase = System.getProperty("java.rmi.server.codebase");
		if (codebase != null)
			logger.debug("Codebase is alredy specified: "
                    + codebase);

        String d = System.getProperty("webster.debug");
        if (d != null && d.equals("true"))
            debug = true;

        String roots;
        InetAddress ip = SorcerEnv.getLocalHost();
        String localIPAddress = ip.getHostAddress();
        String sorcerHome = System.getProperty("sorcer.home");
        roots = System.getProperty(WEBSTER_ROOTS);
        String fs = File.separator;
        StringBuffer sb = new StringBuffer();
        if (roots == null && websterRoots == null) {
            // defaults Sorcer roots
            sb.append(";").append(SorcerEnv.getRepoDir()).append(";").append(SorcerEnv.getLibPath());
        } else if (websterRoots != null) {
            for (int i=0; i<websterRoots.length; i++) {
                sb.append(';').append(websterRoots[i]);
            }
        }
        roots = sb.toString();

        String sMinThreads = System.getProperty("sorcer.webster.minThreads",
                "1");
        int minThreads = 1;
        try {
            minThreads = Integer.parseInt(sMinThreads);
        } catch (NumberFormatException e) {
            logger.warn("Bad Min Threads Number [" + sMinThreads
                    + "], " + "default to " + minThreads, e);
        }
        String sMaxThreads = System.getProperty("webster.maxThreads",
                "10");
        int maxThreads = 10;
        try {
            maxThreads = Integer.parseInt(sMaxThreads);
        } catch (NumberFormatException e) {
            logger.warn("Bad Max Threads Number [" + sMaxThreads
                    + "], " + "default to " + maxThreads, e);
        }
        String sPort = System.getProperty("webster.port", "0");
        int port = 0;
        try {
            port = Integer.parseInt(sPort);
        } catch (NumberFormatException e) {
            logger.warn("Bad port Number [" + sPort + "], "
                    + "default to " + port, e);
        }

        String address = System.getProperty(S_WEBSTER_INTERFACE);
        Webster webster = new Webster(port, roots, address, minThreads, maxThreads, true);
        //Webster webster = new Webster(port, roots, address);
        //, minThreads, maxThreads, true);
        port = webster.getPort();
        if (logger.isDebugEnabled())
            logger.debug("Webster MinThreads=" + minThreads + ", "
                    + "MaxThreads=" + maxThreads);

        if (logger.isDebugEnabled())
            logger.debug("Webster serving on port=" + port);

        String[] jars = null;
        String jarsList = null;
        if (exportJars != null)
            jars = exportJars;
        else {
            jarsList = System.getProperty(CODEBASE_JARS);
            if (jarsList == null || jarsList.length() == 0)
                throw new RuntimeException(
                        "No jar files available for the webster codebase");
            else
                jars = toArray(jarsList);
        }

        Set<String> codebaseSet = new HashSet<String>();
        for (String export : jars)
            if (export.startsWith("artifact:")) {
                logger.debug("adding artifact as is: " + export);
                codebaseSet.add(export);
            } else if (ArtifactCoordinates.isArtifact(export)) {
                String url = resolve(export);
                logger.debug("Adding " + export + " as " + url);
                codebaseSet.add(url);
            } else {
                String url = pathToHttpUrl(export, localIPAddress, port);
                logger.debug("Adding " + export + " as " + url);
                codebaseSet.add(url);
            }
        codebase = StringUtils.join(codebaseSet, CODEBASE_SEPARATOR);
        System.setProperty(JavaSystemProperties.RMI_SERVER_CODEBASE, codebase);
        System.setProperty(SorcerConstants.P_WEBSTER_PORT, Integer.toString(webster.getPort()));
        System.setProperty(SorcerConstants.P_WEBSTER_INTERFACE, webster.getAddress());
        SorcerEnv.updateWebster();
        logger.debug("Setting 'webster URL': " + SorcerEnv.getWebsterUrl());
        logger.debug("Setting 'java.rmi.server.codebase': " + codebase);

        return webster;
    }

    private static String resolve(String coords) {
        ArtifactCoordinates artifact = ArtifactCoordinates.coords(coords);
        if (artifact.getVersion() == null)
            artifact.setVersion(VersionResolver.instance.resolveVersion(artifact.getGroupId(), artifact.getArtifactId()));
        return GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), artifact.toString()).toExternalForm();
    }

    private static String pathToHttpUrl(String path, String address, int port){
        return "http://" + address + ":" + port + "/" + path;
    }

    private static String[] toArray(String arg) {
        StringTokenizer token = new StringTokenizer(arg, " ,;");
        String[] array = new String[token.countTokens()];
        int i = 0;
        while (token.hasMoreTokens()) {
            array[i] = token.nextToken();
            i++;
        }
        return (array);
    }
}
