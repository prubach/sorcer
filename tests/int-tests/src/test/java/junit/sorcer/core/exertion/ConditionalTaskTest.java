package junit.sorcer.core.exertion;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.inEnt;
import static sorcer.eo.operator.alt;
import static sorcer.eo.operator.condition;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.cxt;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.opt;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
import static sorcer.eo.operator.xrt;
import static sorcer.po.operator.add;
import static sorcer.po.operator.put;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.MultiplierImpl;
import junit.sorcer.core.provider.SubtractorImpl;

import org.junit.Test;

import org.junit.runner.RunWith;
import sorcer.core.context.model.par.ParModel;
import sorcer.core.exertion.AltExertion;
import sorcer.core.exertion.OptExertion;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerRunner;
import sorcer.po.operator;
import sorcer.service.Condition;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(SorcerRunner.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
//        "org.sorcersoft.sorcer:ju-arithmetic-api"
//})
public class ConditionalTaskTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ConditionalTaskTest.class.getName());

	@Test
	public void arithmeticTaskTest() throws ExertionException,
			SignatureException, ContextException, RemoteException {
		// to test tracing of execution enable ServiceExertion.debug
		ParModel pm = new ParModel("par-model");

		Task task = task(
				"add",
				sig("add", AdderImpl.class),
				context(inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("result/y")));

		OptExertion ift = opt("ift", condition(pm,
				"{ x, y -> x > y }", "x", "y"), task);

		add(pm, ift);
		put(pm, "x", 10.0);
		put(pm, "y", 20.0);

		task = exert(ift);
		// logger.info("task: " + task);
		assertEquals(false, get(task, Condition.CONDITION_VALUE));
		assertEquals("add", get(task, Condition.CONDITION_TARGET));

		pm.putValue("x", 300.0);
		pm.putValue("y", 200.0);
		task = exert(ift);
		// logger.info("opt value: " + exert(ift));
		assertEquals(true, get(task, Condition.CONDITION_VALUE));
		assertEquals("add", get(task, Condition.CONDITION_TARGET));
		assertEquals(100.0, get(task, "result/y"));
	}

	@Test
	public void altExertionTest() throws RemoteException, ContextException, SignatureException, ExertionException {
		ParModel pm = operator.parModel("par-model");
		pm.putValue("x1", 30.0);
		pm.putValue("y1", 20.0);
		pm.putValue("x2", 50.0);
		pm.putValue("y2", 40.0);


		Task t3 = xrt("t3", sig("subtract", SubtractorImpl.class), 
				cxt("subtract", inEnt("arg/x1", 200.0), inEnt("arg/x2", 50.0),
						result("result/y")));

		Task t4 = xrt("t4", sig("multiply", MultiplierImpl.class), 
				cxt("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						result("result/y")));

		Task t5 = xrt("t5", sig("add", AdderImpl.class), 
				cxt("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("result/y")));
		
		OptExertion opt1 = opt("opt1", condition(pm,
				"{ x1, y1 -> x1 > y1 }", "x1", "y1"), t3);

		OptExertion opt2 = opt("opt2", condition(pm,
				"{ x2, y2 -> x2 >= y2 }", "x2", "y2"), t4);

		// no condition means condition(true)
		OptExertion opt3 = opt("op3", t5);

		AltExertion alt = alt("alt", opt1, opt2, opt3);
		add(pm, opt1, opt2, opt3, alt);

//		logger.info("opt1 value: " + value(opt1));
		assertEquals(150.0, value(opt1));
//		logger.info("opt2 value: " + value(opt2));
		assertEquals(500.0, value(opt2));
//		logger.info("opt3 value: " + value(opt3));
		assertEquals(100.0, value(opt3));
//		logger.info("alt value: " + value(alt));
		assertEquals(150.0, value(alt));

		pm.putValue("x1", 10.0);
		pm.putValue("y1", 20.0);
//		logger.info("opt value: " + value(alt));
		logger.info("pm context 1: " + pm);
		assertEquals(500.0, value(alt));
		
		pm.putValue("x2", 40.0);
		pm.putValue("y2", 50.0);
		logger.info("pm context 2: " + pm);
//		logger.info("opt valueX: " + value(alt));
		assertEquals(100.0, value(alt));
	}

	@Test
	public void loopExertionTest() throws RemoteException, ContextException {
//		final ParModel pm = model("par-model");
//		final Var<Double> x = var("x", 1.0);
//		Var y = var("y", groovy("x + 1", x));
//		add(pm, x);
//		add(pm, y);
//		
//		// update x and y for the loop condition (z) depends on
//		Runnable update = new Runnable() {
//			 public void run() {
//				 try {
//					while ((Double)value(pm, "x") < 25.0) {
//						 set(x, value(x) + 1.0);
//						 System.out.println("running ... " + value(pm, "x"));
//						 Thread.sleep(200);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			 }
//		};
//		
//		new Thread(update).start();
//		
//		Var vloop = loop("vloop",
//				condition(pm, "{ x -> x < 20 }", "x"), 
//				y);
//		
//		add(pm, vloop);
//		assertEquals(value(vloop), 20.0);

//		logger.info("loop value: " + value(pm, "vloop"));
	}
}
