
package sorcer.core.requestor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import org.codehaus.groovy.control.CompilationFailedException;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;
import sorcer.resolver.VersionResolver;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.tools.webster.InternalWebster;
import sorcer.tools.webster.Webster;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.GenericUtil;
import sorcer.util.JavaSystemProperties;

import static sorcer.util.ArtifactCoordinates.coords;

abstract public class
        ServiceRequestor implements Requestor {
    /** Logger for logging information about this instance */
    protected static final Logger logger = LoggerFactory
            .getLogger(ServiceRequestor.class.getName());
    private Webster webster;

    protected Properties props;
    protected Exertion exertion;
    protected String jobberName;
	protected GroovyShell shell;
    protected static String[] codebaseJars;
    protected static boolean isWebsterInt = false;
    protected static ServiceRequestor requestor = null;
    final static String REQUESTOR_PROPERTIES_FILENAME = "requestor.properties";

    public static void main(String... args) throws Exception {
        prepareBasicEnvironment();
        Webster myWebster = prepareCodebase();
        initialize(args);
        requestor.webster = myWebster;
        requestor.preprocess(args);
        requestor.process(args);
		requestor.postprocess(args);
    }

	public static void initialize(String... args) {
        // Initialize system properties: configs/sorcer.env
		SorcerEnv.getEnvProperties();
        String runnerType = null;
        if (args.length == 0) {
            System.err
					.println("Usage: Java sorcer.core.requestor.ServiceRequestor  <runnerType>");
            System.exit(1);
        } else {
            runnerType = args[0];
        }
        try {
            requestor = (ServiceRequestor) Class.forName(runnerType)
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
			logger.info("Not able to create service requestor: " + runnerType);
            System.exit(1);
        }
        String str = System.getProperty(REQUESTOR_PROPERTIES_FILENAME);
        logger.info(REQUESTOR_PROPERTIES_FILENAME + " = " + str);
        if (str != null) {
            requestor.loadProperties(str); // search the provider package
        } else {
            requestor.loadProperties(REQUESTOR_PROPERTIES_FILENAME);
        }
        }

    public static void setCodeBaseByArtifacts(String[] artifactCoords) {
        String[] jars = new String[artifactCoords.length];
        for (int i = 0; i < artifactCoords.length; i++) {
            jars[i] = Resolver.resolveRelative(artifactCoords[i]);
        }
        SorcerEnv.setCodeBase(jars);
    }

    public void setExertion(Exertion exertion) {
        this.exertion = exertion;
    }

    abstract public Exertion getExertion(String... args)
            throws ExertionException, ContextException, SignatureException;

    public String getJobberName() {
        return jobberName;
    }

    public void preprocess(String... args) throws ContextException {
        Exertion in = null;
        try {
            in = requestor.getExertion(args);
            if (logger.isDebugEnabled())
                logger.debug("Runner java.rmi.server.codebase: "
                        + System.getProperty("java.rmi.server.codebase"));
        } catch (ExertionException e) {
			logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
            System.exit(1);
        } catch (ContextException e) {
			logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
            System.exit(1);
        } catch (SignatureException e) {
            logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
            System.exit(1);
        }
        if (in != null)
            requestor.setExertion(in);
        if (exertion != null)
            logger.info(">>>>>>>>>> Input context: \n" + exertion.getContext());
    }

	public void process(String... args) throws ContextException {
        try {
            exertion = ((ServiceExertion) exertion).exert(
                    requestor.getTransaction(), requestor.getJobberName());
		} catch (RemoteException e) {
            logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
		} catch (TransactionException e) {
            logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
		} catch (ExertionException e) {
            logger.error("ServiceRequestor: " + e.getMessage() + "\nat:\n" + Arrays.toString(e.getStackTrace()).replace(", ", "\n") + "\n");
        }
    }

    public void postprocess(String... args) throws ExertionException, ContextException {
        if (exertion != null) {
            logger.info("<<<<<<<<<< Exceptions: \n" + exertion.getExceptions());
            logger.info("<<<<<<<<<< Traces: \n" + exertion.getControlContext().getTrace());
            logger.info("<<<<<<<<<< Ouput context: \n" + exertion.getContext());
        }
        // Exit webster
        if (isWebsterInt && webster != null) {
            webster.terminate();
    }
	}

	public Object evaluate(File scriptFile) throws CompilationFailedException,
			IOException {
		shell = new GroovyShell();
		return shell.evaluate(scriptFile);
	}

    public Transaction getTransaction() {
        return null;
    }

    /**
     * Loads service requestor properties from a <code>filename</code> file. By
     * default a service requestor loads its properties from
     * <code>requestor.properties</code> file located in the requestor's
     * package. Also, a service requestor properties file name can be specified
     * as a system property when starting the requestor with
     * <code>-DrequestorProperties=&ltfilename&gt<code>. In this case the requestor loads
     * properties from <code>filename</code> file. Properties are accessible
     * calling the <code>
     * getProperty(String)</code> method.
     *
     * @param filename
     *            the properties file name see #getProperty
     */
    public void loadProperties(String filename) {
        logger.info("loading requestor properties:" + filename);
        String propsFile = System.getProperty("requestor.properties.file");

        try {
            if (propsFile != null) {
                props.load(new FileInputStream(propsFile));
            } else {
                // check the class resource
                InputStream is = this.getClass().getResourceAsStream(filename);
                // check local resource
                if (is == null)
                    is = new FileInputStream(filename);
                if (is != null) {
                    props = new Properties();
                    props.load(is);
                } else {
                    System.err
                            .println("Not able to open stream on properties: "
                                    + filename);
                    System.err.println("Service runner class: "
                            + this.getClass());
                    return;
                }
            }
        } catch (IOException ioe) {
            logger.info("Not able to load requestor properties");
            // ioe.printStackTrace();
        }

    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public String getProperty(String property, String defaultValue) {
        return props.getProperty(property, defaultValue);
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

    public static Webster prepareCodebase() {
        return prepareCodebase((ArtifactCoordinates[])null);
    }

    public static Webster prepareCodebase(String[] artifactCoords) {
        if (artifactCoords==null)
            return prepareCodebase((ArtifactCoordinates[])null);
        ArtifactCoordinates[] acc = new ArtifactCoordinates[artifactCoords.length];
        int i = 0;
        for (String ac : artifactCoords) {
            acc[i++] = ArtifactCoordinates.coords(ac);
        }
        return prepareCodebase(acc);
    }

    public static Webster prepareCodebase(ArtifactCoordinates[] artifactCoords) {
        // Initialize system properties: configs/sorcer.env
        SorcerEnv.getEnvProperties();
        String val = System.getProperty(SorcerConstants.SORCER_WEBSTER_INTERNAL);
        if (val != null && val.length() != 0) {
            isWebsterInt = val.equals("true");
        }
        String exertrun = System.getProperty(SorcerConstants.R_CODEBASE);
        List<String> codebase = new LinkedList<String>();
        if (exertrun!=null || artifactCoords!=null) {
            if (exertrun!=null && !exertrun.isEmpty()) {
                String[] artifacts = exertrun.split(" ");
                for (String artifact : artifacts) {
                    codebase.add(resolve(coords(artifact)));
                }
            }
            if (artifactCoords!=null)
                for (ArtifactCoordinates artCord : artifactCoords) {
                    codebase.add(resolve(artCord));
                }

            logger.debug("ServiceRequestor generated codebase: " + codebase.toString());
            if (isWebsterInt)
                System.setProperty(SorcerConstants.CODEBASE_JARS, codebase.toString());
            else
                System.setProperty(JavaSystemProperties.RMI_SERVER_CODEBASE, codebase.toString());

            codebaseJars = codebase.toArray(new String[codebase.size()]);
        }

        if (isWebsterInt && codebaseJars!=null && codebaseJars.length>0) {
            String roots = System.getProperty(SorcerConstants.WEBSTER_ROOTS);
            String[] tokens = null;
            if (roots != null)
                tokens = toArray(roots);
            try {
                return InternalWebster.startWebster(codebaseJars, tokens);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static String resolve(ArtifactCoordinates coords) {
        if (coords.getVersion() == null)
            coords.setVersion(VersionResolver.instance.resolveVersion(coords));
        return GenericUtil.toArtifactUrl(SorcerEnv.getCodebaseRoot(), coords.toString()).toExternalForm();
    }
    // Utility for setting the basic environment properties
    // It is required by scilab script that invokes exertions from scilab

    public static void prepareEnvironment() {
        System.setProperty("webster.internal", "true");
        prepareBasicEnvironment();
    }

    protected static void prepareBasicEnvironment(){
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.url|org.rioproject.url");
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME") + "/configs/sorcer.policy");
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", "sorcer.rio.rmi.SorcerResolvingLoader");
        System.setProperty("org.rioproject.resolver.jar", SorcerEnv.getProperty("sorcer.local.repo.location") +
                "/org/rioproject/resolver/resolver-aether/" + SorcerEnv.getRioVersion() + "/resolver-aether-" + SorcerEnv.getRioVersion() + ".jar");
        //System.setProperty("java.util.logging.config.file", System.getenv("SORCER_HOME") + "/configs/sorcer.logging");
        System.setSecurityManager(new SecurityManager());
    }
}
