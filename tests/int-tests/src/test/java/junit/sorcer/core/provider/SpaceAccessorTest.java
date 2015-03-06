package junit.sorcer.core.provider;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;
import net.jini.space.JavaSpace05;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.junit.SorcerServiceConfiguration;
import sorcer.service.Accessor;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.space.SpaceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

/**
 * SORCER class
 * User: prubach
 * Date: 23.04.14
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api"})
public class SpaceAccessorTest {
    private final static Logger logger = LoggerFactory.getLogger(SpaceAccessorTest.class
            .getName());

    @BeforeClass
    public static void envSettingsTest() throws ExertionException, ContextException,
            SignatureException {
        try {
            assertNotNull(System.getenv("SORCER_HOME"));
            logger.info("SORCER_HOME: " + SorcerEnv.getHomeDir());
        } catch (AssertionError ae) {
            logger.error("SORCER_HOME must be set and point to the Sorcer root directory!!!");
        }
    }


    @Test
    public void getSpaceTest() throws ExertionException, ContextException,
            SignatureException {
        logger.info("exert space:" + SpaceAccessor.getSpace());

        ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { JavaSpace05.class }, new Entry[] { new Name(SorcerEnv.getActualSpaceName())});
        ServiceItem si = Accessor.getServiceItem(tmpl, null, new String[]{SorcerEnv.getSpaceGroup()});
        Assert.assertNotNull(si);
        logger.info("got service: serviceID=" + si.serviceID + " template="
                + tmpl + " groups=" + SorcerEnv.getSpaceGroup());
    }
}
