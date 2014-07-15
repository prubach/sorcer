package sorcer.util;

import sorcer.core.provider.DatabaseStorer;
import sorcer.core.provider.Provider;

import java.io.FileInputStream;

/**
 * SORCER class
 * User: prubach
 * Date: 15.07.14
 */
public class ProviderUtil {

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

}
