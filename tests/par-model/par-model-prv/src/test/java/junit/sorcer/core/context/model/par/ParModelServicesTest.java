package junit.sorcer.core.context.model.par;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.entry;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
import static sorcer.po.operator.agent;
import static sorcer.po.operator.invoke;
import static sorcer.po.operator.invoker;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.core.transaction.TransactionException;

import org.junit.Ignore;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.core.context.model.par.ParModel;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.junit.SorcerServiceConfiguration;
import sorcer.resolver.Resolver;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.Invocation;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.Sorcer;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:model-beans"})
@SorcerServiceConfiguration(":par-model-cfg")
public class ParModelServicesTest {
	private final static Logger logger = Logger.getLogger(ParModelServicesTest.class
			.getName());

	
	@Test
	public void parModelerTest() throws RemoteException, ContextException,
			ExertionException, SignatureException {
		ParModel pm = ParModeler.getParModel();
		logger.info("result: " + invoke(pm, "expr"));
		assertEquals(invoke(pm, "expr"), 60.0);
	}
	
	@Test
	public void parObjectModelServiceTest() throws RemoteException, ContextException, ExertionException, SignatureException {
		ParModel pm = ParModeler.getParModel();
		Task pmt = task(sig("invoke", pm), 
				context(entry("par", "expr"), result("invoke/result")));
		
		logger.info("result: " + value(pmt));
		assertEquals(value(pmt), 60.0);
		
		logger.info("result: " + exert(pmt));
		assertEquals(get(exert(pmt), "invoke/result"), 60.0);
	}

    @Test
	public void parNetModelServiceTest() throws RemoteException, ContextException, 
			ExertionException, SignatureException {
		// the provider in ex6/bin parmodel-prv-run.xml
        // Signature - actualName...
		Task pmt = task(sig("invoke", Invocation.class,
               null, "ParModel Service"),
				context(entry("par", "expr"), result("invoke/result")));
		
//		logger.info("result: " + value(pmt));
		assertEquals(value(pmt), 60.0);
		
//		logger.info("result: " + exert(pmt));
		assertEquals(get(exert(pmt), "invoke/result"), 60.0);
	}

    @Test
	public void parNetVarModelServiceTest() throws RemoteException, ContextException, 
			ExertionException, SignatureException {
		// the provider in ex6/bin varparmodel-prv-run.xml
		Task pmt = task(sig("invoke", Invocation.class, null, "ParModel Service"),
				context(entry("par", "expr"), result("invoke/result")));

//		logger.info("result: " + value(pmt));
		assertEquals(value(pmt), 60.0);
		
//		logger.info("result: " + exert(pmt));
		assertEquals(get(exert(pmt), "invoke/result"), 60.0);
	}

	@Test
	public void parObjectModelAgentTest() throws RemoteException, ContextException, ExertionException, 
			SignatureException, MalformedURLException {
		
		ParModel pm = ParModeler.getParModel();
        logger.info("CP: " + new URL(SorcerEnv.getWebsterUrl() + "/" + Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans")));
		Task pmt = task(sig("invoke", pm), context(
				//entry("par", "getSphereVolume"),
                invoker("getSphereVolume", invoker("getSphereVolume")),
				result("sphere/volume"),
				entry("sphere/radius", 20.0),
				agent("getSphereVolume",
					"junit.sorcer.core.invoker.service.Volume",
        new URL(SorcerEnv.getWebsterUrl() + "/" + Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans")
							))));
		
		logger.info("result: " + value(pmt));
		assertEquals(value(pmt), 33510.32163829113);
	}

	@Test
	public void parObjectModelMultiAgentTest() throws RemoteException, ContextException, ExertionException, 
			SignatureException, MalformedURLException {
		
		ParModel pm = ParModeler.getParModel();
		
		// invoking non existing agent and the return value specified
		Task pmt = task(sig("invoke", pm), context(
				//entry("par", "getSphereVolume"),
                invoker("getSphereVolume", invoker("getSphereVolume")),
				result("sphere/volume"),
				entry("sphere/radius", 20.0),
				agent("getSphereVolume",
                        "junit.sorcer.core.invoker.service.Volume",
                        new URL(SorcerEnv.getWebsterUrl() + "/" +
                                Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))),
                agent("getCylinderSurface",
                        "junit.sorcer.core.invoker.service.Volume",
                        new URL(SorcerEnv.getWebsterUrl() + "/" +
                                Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))
                )
                ));
		
		logger.info("result: " + value(pmt));
		assertEquals(value(pmt), 33510.32163829113);
		
		// the existing agent and the return value specified
		pmt = task(sig("invoke", pm), context(
				//entry("par","getCylinderSurface"),
                invoker("getCylinderSurface", invoker("getCylinderSurface")),
				result("cylinder/surface"),
				entry("cylinder/radius", 1.0), 
				entry("cylinder/height", 2.0),
                agent("getCylinderSurface",
                        "junit.sorcer.core.invoker.service.Volume",
                        new URL(SorcerEnv.getWebsterUrl() + "/" +
                                Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))
                )));
		
		assertEquals(value(pmt), 18.84955592153876);

		// the existing agent and no return value specified
		pmt = task(sig("invoke", pm), context(
				//entry("par", "getCylinderSurface"),
                invoker("getCylinderSurface", invoker("getCylinderSurface")),
                entry("cylinder/radius", 1.0),
				entry("cylinder/height", 2.0),
                agent("getCylinderSurface",
                        "junit.sorcer.core.invoker.service.Volume",
                        new URL(SorcerEnv.getWebsterUrl() + "/" +
                                Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))
                )));

		assertEquals(get((Context)value(pmt), "cylinder/surface"), 18.84955592153876);
	}

	@Test
	public void parNetModelAgentTest() throws RemoteException, ContextException, ExertionException, 
			SignatureException, MalformedURLException, TransactionException {
		Task pmt = task(sig("invoke", Invocation.class, "ParModel Service"),
				context(invoker("getSphereVolume", invoker("getSphereVolume")),
						entry("sphere/radius", 20.0),
                        agent("getSphereVolume",
                                "junit.sorcer.core.invoker.service.Volume",
                                new URL(SorcerEnv.getWebsterUrl() + "/" +
                                        Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans")
                                        ))));
	
		
		Context cxt = (Context)value(pmt);
		logger.info("result cxt: " + cxt);
		assertEquals(get(cxt, "sphere/radius"), 20.0);
		assertEquals(get(cxt, "sphere/volume"), 33510.32163829113);
		
		pmt = task(sig("invoke", Invocation.class, "ParModel Service"), 
				context(invoker("getSphereVolume", invoker("getSphereVolume")),
						result("sphere/volume"),
						entry("sphere/radius", 20.0),
                        agent("getSphereVolume",
                                "junit.sorcer.core.invoker.service.Volume",
                                new URL(SorcerEnv.getWebsterUrl() + "/" +
                                        Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))
                                )));
		
		assertEquals( 33510.32163829113, value(pmt));
	}

	@Test
	public void parNetModelMultiAgentTest() throws RemoteException, ContextException, ExertionException, 
			SignatureException, MalformedURLException, TransactionException {
		// the provider in ex6/bin parmodel-prv-run.xml
		Task pmt = task(sig("invoke", Invocation.class, "ParModel Service"), 
				context(invoker("getSphereVolume", invoker("getSphereVolume")),
						result("sphere/volume"),
						entry("sphere/radius", 20.0),
						agent("getSphereVolume",
							"junit.sorcer.core.invoker.service.Volume",
                                new URL(SorcerEnv.getWebsterUrl() + "/" +
                                        Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))),
                        agent("getCylinderSurface",
                                "junit.sorcer.core.invoker.service.Volume",
                                new URL(SorcerEnv.getWebsterUrl() + "/" +
                                        Resolver.getResolver().resolveRelative("org.sorcersoft.sorcer:model-beans"))
                        )));
	
		assertEquals(value(pmt), 33510.32163829113);
		
		pmt = task(sig("invoke", Invocation.class, "ParModel Service"), 
				context(invoker("getCylinderSurface", invoker("getCylinderSurface")),
						result("cylinder/surface"),
						//invoker("getCylinderSurface"),
						result("cylinder/surface"),
						entry("cylinder/radius", 1.0), 
						entry("cylinder/height", 2.0)));
		
		assertEquals(value(pmt), 18.84955592153876);
	}
}
