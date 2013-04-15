package junit.sorcer.core.context.model;

import static org.junit.Assert.assertEquals;
import static sorcer.po.operator.add;
import static sorcer.po.operator.groovy;
import static sorcer.po.operator.par;
import static sorcer.po.operator.model;
import static sorcer.po.operator.pars;
import static sorcer.po.operator.put;
import static sorcer.po.operator.result;
import static sorcer.po.operator.set;
import static sorcer.po.operator.value;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.context.model.Par;
import sorcer.core.context.model.ServiceModel;
import sorcer.core.invoker.GroovyInvoker;
import sorcer.core.invoker.ServiceInvoker;
import sorcer.service.ContextException;
import sorcer.service.ServiceExertion;
import sorcer.util.Sorcer;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class InvokeModelTest {
	private final static Logger logger = Logger.getLogger(InvokeModelTest.class
			.getName());

	static {
		ServiceExertion.debug = true;
		System.setProperty("java.util.logging.config.file",
				System.getenv("IGRID_HOME") + "/configs/sorcer.logging");
		System.setProperty("java.security.policy", System.getenv("IGRID_HOME")
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBaseByArtifacts(new String[] { 
				"org.sorcersoft.sorcer:ju-arithmetic-api" });}

	@Test
	public void arithmeticParModelTest() throws RemoteException,
			ContextException {
		ServiceModel pm = new ServiceModel("arithmetic-model");

		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("add", new GroovyInvoker("x + y", pars("x", "y")));

		assertEquals(pm.getValue("x"), 10.0);
		assertEquals(pm.getValue("y"), 20.0);
		logger.info("add value: " + pm.getValue("add"));
		assertEquals(pm.getValue("add"), 30.0);

		logger.info("invoker value: "
				+ ((ServiceInvoker) pm.getAsis("add")).invoke());

		pm.setReturnPath("add");
		logger.info("pm context value: " + pm.invoke());

	}

	@Test
	public void parsWithModelTest() throws RemoteException, ContextException {
		Par x = new Par("x", 10.0);
		Par y = new Par("y", 20.0);
		Par add = new Par("add", new GroovyInvoker("x + y", pars("x", "y")));

		ServiceModel pm = new ServiceModel("arithmetic-model");
		pm.add(x, y, add);

		assertEquals(x.getValue(), 10.0);
		assertEquals(pm.getValue("x"), 10.0);

		assertEquals(y.getValue(), 20.0);
		assertEquals(pm.getValue("y"), 20.0);

		logger.info("add value: " + pm.getValue("add"));
		assertEquals(add.getValue(), 30.0);
		assertEquals(pm.getValue("add"), 30.0);

		logger.info("invoker value: "
				+ ((ServiceInvoker) pm.getAsis("add")).invoke());

		pm.setReturnPath("add");
		logger.info("pm context value: " + pm.invoke());

		x = pm.getPar("x");
		y = pm.getPar("y");
		add = pm.getPar("add");
		assertEquals(x.getValue(), 10.0);
		assertEquals(y.getValue(), 20.0);
		assertEquals(add.getValue(), 30.0);
	}

	@Test
	public void dslParModelTest() throws RemoteException,
			ContextException {
		ServiceModel pm = model(par("x", 10.0), par("y", 20.0),
				par("add", new GroovyInvoker("x + y", pars("x", "y"))));

		assertEquals(value(pm, "x"), 10.0);
		assertEquals(value(pm, "y"), 20.0);
		assertEquals(value(pm, "add"), 30.0);

		result(pm, "add");
		assertEquals(value(pm), 30.0);
	}
	
	@Test
	public void mutateParModelTest() throws RemoteException,
			ContextException {
		
		logger.info("par set:" + pars("x", "y"));
		 
		ServiceModel pm = model(par("x", 10.0), par("y", 20.0),
				par("add", new GroovyInvoker("x + y", pars("x", "y"))));
		
		Par x = par(pm, "x");
		logger.info("par x: " + x);
		set(x, 20.0);
		logger.info("val x: " + value(x));
		logger.info("val x: " + value(pm, "x"));

		put(pm, "y", 40.0);
		
		logger.info("par model1:" + pm);
		
		assertEquals(value(pm, "x"), 20.0);
		assertEquals(value(pm, "y"), 40.0);
		assertEquals(value(pm, "add"), 60.0);
		result(pm, "add");
		assertEquals(value(pm), 60.0);
		
		put(pm, par("x", 10.0), par("y", 20.0));
		assertEquals(value(pm, "x"), 10.0);
		assertEquals(value(pm, "y"), 20.0);
		
		logger.info("par model2:" + pm);
		assertEquals(value(pm, "add"), 30.0);
		
		result(pm, "add");
		assertEquals(value(pm), 30.0);
		
		
		// with new arguments, closure
		assertEquals(value(pm, par("x", 10.0), par("y", 20.0)), 30.0);
		
		
		add(pm, par("z", groovy("(x * y) + add", pars("x", "y", "add"))));
		logger.info("z value: " + value(pm, "z"));
		assertEquals(value(pm, "z"), 230.0);
		
	}
	
}
