package sorcer.env;

import sorcer.util.JavaSystemProperties;

import java.io.File;

/**
 * SORCER class
 * User: prubach
 * Date: 25.11.14
 */
public class SorcerHome {

    private static final String SORCER_HOME = "sorcer.home";
    private static final String ENV_SORCER_HOME = "SORCER_HOME";

    public static String sorcerHome = "";

    static {
        String envSorcerHome = System.getenv(ENV_SORCER_HOME);
        sorcerHome = (envSorcerHome!=null && !envSorcerHome.isEmpty() ?
                envSorcerHome : System.getProperty(SORCER_HOME));

        String rioHomeDir = "";
        File sorcerHomefile = new File(sorcerHome);
        String[] fileList = sorcerHomefile.list();
        for(String name : fileList) {
            if (new File(sorcerHomefile, name).isDirectory() && name.startsWith("rio")) {
                rioHomeDir = name;
                break;
            }
        }
        JavaSystemProperties.ensure("RIO_HOME", (System.getenv("RIO_HOME") != null ? System.getenv("RIO_HOME") : new File(sorcerHome, rioHomeDir).getPath()));
    }

    public static boolean isClearResolverCache() {
        if (System.getProperty("sorcer.resolver.cache.clear")!=null)
            return Boolean.getBoolean(System.getProperty("sorcer.resolver.cache.clear"));
        else
            return false;
    }
}
