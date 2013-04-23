package junit.sorcer.core.provider;

import static org.junit.Assert.assertNotNull;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.SorcerConstants;
import sorcer.service.Jobber;
import sorcer.service.Servicer;
import sorcer.util.ProviderAccessor;
import sorcer.util.ProviderLocator;
import sorcer.util.ProviderLookup;
import sorcer.util.Sorcer;
import sorcer.util.Stopwatch;

/**
 * @author Mike Sobolewski
 */

public class ProviderAccessorTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(ProviderAccessorTest.class.getName());

	static {
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
	}
	
	@Test
	public void providerAcessorTest() throws Exception {
		long startTime = System.currentTimeMillis();
		Servicer provider = ProviderAccessor.getProvider(Jobber.class);
		//logger.info("ProviderAccessor provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}
	
	@Test
	public void providerLookupTest() throws Exception {
		long startTime = System.currentTimeMillis();
		Servicer provider = (Servicer)ProviderLookup.getService(Jobber.class);
		//logger.info("ProviderLookup provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}

	@Test
	public void providerLookatorTest() throws Exception {
		long startTime = System.currentTimeMillis();
		Servicer provider = (Servicer)ProviderLocator.getService(Jobber.class);
		//logger.info("ProviderLocator provider: " + provider);
		logger.info(Stopwatch.getTimeString(System.currentTimeMillis() - startTime));
		assertNotNull(provider);

	}

}
