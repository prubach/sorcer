package junit.sorcer.core.provider;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.entry;
import static sorcer.co.operator.inEnt;
import static sorcer.eo.operator.alt;
import static sorcer.eo.operator.block;
import static sorcer.eo.operator.condition;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.exertions;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.loop;
import static sorcer.eo.operator.opt;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.par;
import static sorcer.po.operator.pars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;

import org.junit.runner.RunWith;
import sorcer.co.operator;
import sorcer.core.SorcerConstants;
import sorcer.core.provider.rendezvous.ServiceConcatenator;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerRunner;
import sorcer.service.Block;
import sorcer.service.Task;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerRunner.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
//        "org.sorcersoft.sorcer:ju-arithmetic-api"
//})
public class ArithmeticNoNetBlockTest implements SorcerConstants {

	private final static Logger logger = LoggerFactory
			.getLogger(ArithmeticNoNetTest.class.getName());

	@Test
	public void contextAltTest() throws Exception {
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), 
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						result("block/result")));

		Task t5 = task("t5", sig("add", AdderImpl.class), 
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("block/result")));
		
		Block block = block("block", sig("execute", ServiceConcatenator.class), 
				context(operator.ent("y1", 100), operator.ent("y2", 200)),
				alt(opt(condition("{ y1, y2 -> y1 > y2 }", "y1", "y2"), t4), 
					opt(condition("{ y1, y2 -> y1 <= y2 }", "y1", "y2"), t5)));
		
		block = exert(block);
//		logger.info("block context: " + context(block));
//		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 100.00);

		block = exert(block, operator.ent("y1", 200.0), operator.ent("y2", 100.0));
//		logger.info("block context: " + context(block));
//		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 500.0);
	}
	
	@Test
	public void taskAltBlocTest() throws Exception {
		Task t3 = task("t3", sig("subtract", SubtractorImpl.class), 
				context("subtract", inEnt("arg/t4"), inEnt("arg/t5"),
						result("block/result")));

		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), 
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						result("arg/t4")));

		Task t5 = task("t5", sig("add", AdderImpl.class), 
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("arg/t5")));
		
		Task t6 = task("t6", sig("average", AveragerImpl.class), 
				context("average", inEnt("arg/t4"), inEnt("arg/t5"),
						result("block/result")));
		
		Block block = block("block", sig("execute", ServiceConcatenator.class), t4, t5, alt(
				opt(condition("{ t4, t5 -> t4 > t5 }", "t4", "t5"), t3), 
				opt(condition("{ t4, t5 -> t4 <= t5 }", "t4", "t5"), t6)));
		
//		logger.info("block: " + block);
//		logger.info("exertions: " + exertions(block));
//		logger.info("block context: " + context(block));

		block = exert(block);
//		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 400.00);
		
		block = exert(block, operator.ent("block/t5/arg/x1", 200.0), operator.ent("block/t5/arg/x2", 800.0));
//		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 750.00);
	}
	
	@Test
	public void optBlockTest() throws Exception {
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), 
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						result("out")));
		
		Task t5 = task("t5", sig("add", AdderImpl.class), 
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("out")));
		
		Block block = block("block", sig("execute", ServiceConcatenator.class), t4,
				opt(condition("{ out -> out > 600 }", "out"), t5));
		
		block = exert(block);
		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "out"));
		assertEquals(value(context(block), "out"), 500.0);
		
		block = exert(block, operator.ent("block/t4/arg/x1", 200.0), operator.ent("block/t4/arg/x2", 800.0));
		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "out"));
		assertEquals(value(context(block), "out"), 100.0);
	}
	
	@Test
	public void parBlockTest() throws Exception {
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), 
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						result("block/result")));

		Task t5 = task("t5", sig("add", AdderImpl.class), 
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("block/result")));
				 
		Block block = block("block", sig("execute", ServiceConcatenator.class), 
				context(operator.ent("x1", 4), operator.ent("x2", 5)),
				task(par("y", invoker("x1 * x2", pars("x1", "x2")))), 
				alt(opt(condition("{ y -> y > 50 }", "y"), t4), 
				    opt(condition("{ y -> y <= 50 }", "y"), t5)));
		
		logger.info("block: " + block);
		logger.info("exertions: " + exertions(block));
		logger.info("block context: " + context(block));

		block = exert(block);
		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 100.00);
		
		block = exert(block, operator.ent("block/x1", 10.0), operator.ent("block/x2", 6.0));
		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "block/result"));
		assertEquals(value(context(block), "block/result"), 500.00);
	}
	
	@Test
	public void loopBlockTest() throws Exception {
		Block block = block("block", sig("execute", ServiceConcatenator.class), 
				context(operator.ent("x1", 10.0), operator.ent("x2", 20.0), operator.ent("z", 100.0)),
				loop(condition("{ x1, x2, z -> x1 + x2 < z }", "x1", "x2", "z"), 
						task(par("x1", invoker("x1 + 3", par("x1"))))));
		
		block = exert(block);
		logger.info("block context: " + context(block));
		logger.info("result: " + value(context(block), "x1"));
		assertEquals(value(context(block), "x1"), 82.00);
	}
}
