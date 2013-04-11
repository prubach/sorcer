package junit.sorcer.core.context.model;

import static org.junit.Assert.assertEquals;
import static sorcer.po.operator.par;
import static sorcer.po.operator.parModel;
import static sorcer.po.operator.result;
import static sorcer.po.operator.value;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.context.model.Par;
import sorcer.core.context.model.ParModel;
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
				"org.sorcersoft.sorcer:ju-arithmetic-api" });			
	}

	@Test
	public void arithmeticParModelTest() throws RemoteException,
			ContextException {
		ParModel pm = new ParModel("arithmetic-model");

		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("add", new GroovyInvoker("x + y"));

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
		Par add = new Par("add", new GroovyInvoker("x + y"));

		ParModel pm = new ParModel("arithmetic-model");
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
		ParModel pm = parModel(par("x", 10.0), par("y", 20.0),
				par("add", new GroovyInvoker("x + y")));

		assertEquals(value(pm, "x"), 10.0);
		assertEquals(value(pm, "y"), 20.0);
		assertEquals(value(pm, "add"), 30.0);

		result(pm, "add");
		assertEquals(value(pm), 30.0);
	}
}
