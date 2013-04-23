package junit.sorcer.core.exertion;

//import com.gargoylesoftware,base,testing,TestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.entry;
import static sorcer.co.operator.list;
//import static sorcer.co.operator.loop;
import static sorcer.co.operator.map;
import static sorcer.eo.operator.args;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.exceptions;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.name;
import static sorcer.eo.operator.path;
import static sorcer.eo.operator.print;
import static sorcer.eo.operator.put;
import static sorcer.eo.operator.result;
//import static sorcer.eo.operator.selectVars;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.strategy;
import static sorcer.eo.operator.target;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.trace;
import static sorcer.eo.operator.value;
//import static sorcer.vo.operator.args;
//import static sorcer.vo.operator.expression;
//import static sorcer.vo.operator.groovy;
//import static sorcer.vo.operator.inputVars;
//import static sorcer.vo.operator.var;
//import static sorcer.vo.operator.vars;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.Multiply;
import net.jini.core.transaction.TransactionException;

import org.junit.Test;

import sorcer.core.context.ServiceContext;
//import sorcer.core.context.model.FidelityInfo;
import sorcer.core.exertion.EvaluationTask;
//import sorcer.core.exertion.FilterTask;
import sorcer.core.exertion.ObjectTask;
//import sorcer.core.exertion.VarTask;
//import sorcer.core.signature.FilterSignature;
import sorcer.core.signature.ObjectSignature;
//import sorcer.core.signature.VarSignature;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Wait;
import sorcer.service.Task;
import sorcer.util.Sorcer;
//import sorcer.vfe.Filter;
//import sorcer.vfe.Var;
//import sorcer.vfe.filter.ListFilter;
//import sorcer.vfe.filter.MapKeyFilter;
//import sorcer.vfe.util.VarList;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TaskTest {
	private final static Logger logger = Logger.getLogger(TaskTest.class
			.getName());

	static {
		ServiceExertion.debug = true;
		System.setProperty("java.util.logging.config.file",
				System.getenv("SORCER_HOME") + "/configs/sorcer.logging");
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/sorcer.policy");
		System.setSecurityManager(new RMISecurityManager());
		//Sorcer.setCodeBaseByArtifacts(new String[] { 
		//		"org.sorcersoft.sorcer:ju-arithmetic-api", 
		//		"org.sorcersoft.sorcer:ju-arithmetic-service" });
	}

	@Test
	public void freeArithmeticTaskTest() throws ExertionException, SignatureException, ContextException {
		//to test tracing of execution enable ServiceExertion.debug 		
		Exertion task = task("add",
				sig("add"),
				context(in("arg/x1"), in("arg/x2"),
						result("result/y")));
		
		logger.info("get task: " + task);
		logger.info("get context: " + context(task));
		
		Object val = value(task, in("arg/x1", 20.0), in("arg/x2", 80.0),
				strategy(sig("add", AdderImpl.class), Access.PUSH, Wait.YES));
		
		//logger.info("get value: " + val);
		assertEquals("Wrong value for 100", val, 100.0);
	}
	
	@Test
	public void arithmeticTaskTest() throws ExertionException, SignatureException, ContextException {
		//to test tracing of execution enable ServiceExertion.debug 
		ServiceExertion.debug = true;
		
		Task task = task("add",
				sig("add", AdderImpl.class),
				context(in("arg/x1", 20.0), in("arg/x2", 80.0),
						result("result/y")));
		
		task = exert(task);
//		logger.info("exerted: " + task);
//		print(exceptions(task));
//		print(trace(task));
		double val = (Double)value(task);

		
//		logger.info("get value: " + val);
		assertTrue("Wrong value for 100.0", val == 100.0);
		//logger.info("exec trace: " + trace(task));
		//logger.info("trace  size: " + trace(task).size());
		//assertTrue(trace(task).size() == 1);
		ServiceExertion.debug = true;
//		logger.info("exceptions: " + exceptions(task));
		assertTrue(exceptions(task).size() == 0);

		val = (Double)get(task, "result/y");
		//logger.info("get value: " + val);
		assertTrue("Wrong value for 100.0", val == 100.0);
		
		task = exert(task);
		val = (Double)get(context(task), "result/y");
		//logger.info("get value: " + val);
		assertTrue("Wrong value for 100.0", val == 100.0);
		//assertTrue(trace(task).size() == 2);
		assertTrue(exceptions(task).size() == 0);
		
		put(task, entry("arg/x1", 1.0), entry("arg/x2", 5.0));
		val = (Double)value(task);
		//logger.info("evaluate: " + val);
		assertTrue("Wrong value for 6.0", val == 6.0);
				
		val = (Double)value(task, entry("arg/x1", 2.0), entry("arg/x2", 10.0));
		//logger.info("evaluate: " + val);
		assertTrue("Wrong value for 12.0", val == 12.0);
		
		//logger.info("task context: " + context(task));
		//logger.info("get value: " + get(task));
		assertTrue("Wrong value for 12.0", get(task).equals(12.0));
	}
	
//	@Test
//	public void exertEvaluationTaskTest() throws ExertionException, ContextException,
//			RemoteException, TransactionException, SignatureException {
//
//		Task dateTask = new EvaluationTask(groovy("new Date()"));
////		logger.info("dateTask value: "
////				+  dateTask.exert().getContext().getValue());
//		assertTrue("Wrong date",
//				("" + dateTask.exert().getContext().getValue())
//						.equals("" + new Date()));
//		
//		VarList vl = inputVars(loop(6), "x");
//		put(vl, entry("x2", 2.0), entry("x3", 3.0));
//		Task exprTask = new EvaluationTask(groovy("x2 + x3",
//				vl.selectVars("x2", "x3")));
//		//logger.info("exprTask: " + (EvaluationTask) exprTask.exert());
//		assertEquals("Wrong value for 5",
//				((EvaluationTask) exprTask.exert()).getValue(), 5.0);
//			
//		Task dateTask2 = task(sig(groovy("new Date()")), context(result("evaluation/result")));
////		logger.info("dateTask value: "
////				+ get(exert(dateTask2), "evaluation/result"));
//		assertTrue("Wrong date",
//				("" + get(exert(dateTask2), "evaluation/result"))
//						.equals("" + new Date()));
//		
//		Task exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), context(result("evaluation/result")));
//		//logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 5",
//				get(exert(exprTask2), "evaluation/result"), 5.0);
//		
//		
//		// dependent var values are explicitly in the context
//		exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), context(entry("x2", 20.0), 
//						entry("x3", 30.0), result("evaluation/result")));
//		logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 50.0",
//				get(exert(exprTask2), "evaluation/result"), 50.0);
//		
//		
//		// dependent var values as the argument array in the context
//		exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), context(args(entry("x2", 20.0), 
//						entry("x3", 30.0)), result("evaluation/result")));
//		logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 50.0",
//				get(exert(exprTask2), "evaluation/result"), 50.0);
//	}

	@Test
	public void exertObjectTaskTest() throws Exception {
		ServiceExertion.debug = true;
		ObjectTask objTask = new ObjectTask("t4", new ObjectSignature("multiply", Multiply.class, double[].class));
		ServiceContext cxt = new ServiceContext();
		Object arg = new double[] { 10.0, 50.0 };
		//cxt.setReturnPath("result/y").setArgs(new double[] {10.0, 50.0});
		cxt.setReturnPath("result/y").setArgs(arg);
		objTask.setContext(cxt);
		
		//logger.info("objTask value: " + value(objTask));
		assertEquals("Wrong value for 500.0", value(objTask), 500.0);
		
		ObjectTask objTask2 = task(sig("multiply", new Multiply(), double[].class), 
				context(args(new double[] {10.0, 50.0}), result("result/y")));
		name("t4", objTask2);
		//logger.info("objTask2 value: " + value(objTask2));
		assertEquals("Wrong value for 500.0", value(objTask2), 500.0);
	}
	
//	@Test
//	public void exertVarTaskTest() throws Exception {	
//		Var<Double> x1 = var("x1", 10.0);
//		Var<Integer> x2 = var("x2", 2);		
//		Var<?> y = var("y", expression("x1 + x2", args(x1, x2)));
//		assertEquals(value(y), 12.0);
//		
//		Map<Double, Integer> map = map(entry(1.0,10), entry(2.0,20), entry(12.0,30), entry(24.0,100));
//		Var<?> x3 = var("x3", expression("x3-e1", "x1 + x2", args(x1, x2)), new MapKeyFilter("x3-f1", map));
//		//logger.info("x3 value: " + value(x3));
//		assertEquals(value(x3), 30);
//		
//		FidelityInfo eval = new FidelityInfo("x3", "x3-e1","", "x3-f1");
//		Task varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		ServiceContext cxt = new ServiceContext();
//		cxt.setReturnPath("var/result");
//		varTask.setContext(cxt);
//		//logger.info("varTask value: " + value(varTask));
//		assertEquals("Wrong value for 30", value(varTask), 30);
//		
//		// context with values for arg vars as key/value entries
//		varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		cxt = new ServiceContext();
//		cxt.put("x1", 20.0);
//		cxt.put("x2", 4);
//		cxt.setReturnPath("var/result");		
//		varTask.setContext(cxt);
//		//logger.info("varTask value: " + value(varTask));
//		assertEquals("Wrong value for 100", value(varTask), 100);
//		
//		// context with values for arg vars as the ARGS array
//		varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		cxt = new ServiceContext();
//		cxt.setArgs(map(entry("x1", 20.0), entry("x2", 4.0)));
//		cxt.setReturnPath("var/result");
//		varTask.setContext(cxt);
//		//logger.info("ARGS varTask value: " + value(varTask));
//		assertEquals("Wrong value for 100", value(varTask), 100);
//		
//		// context with values for arg vars as the ARGS array
//		varTask = task("x3-t", sig(eval, x3), 
//				context(args(map(entry("x1", 20.0), 
//							entry("x2", 4.0)), 
//							result("var/result"))));
//		//logger.info("EOL ARGS varTask value: " + value(varTask));
//		assertEquals("Wrong value for 100", value(varTask), 100);
//	}
	
//	@Test
//	public void exertFilterTaskTest() throws Exception {	
//		Filter lf = new ListFilter(4);
//		List<Integer> target = list(10, 20, 30, 40, 50, 60);
//		ServiceContext cxt = new ServiceContext();
//		cxt.setReturnPath("filter/result");
//		cxt.setTarget(target);
//		Task ft = new FilterTask(new FilterSignature(lf));
//		ft.setContext(cxt);
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 50", value(ft), 50);
//
//		// the initialization of the filter is "4", the fifth element from the list
//		ft = task("ft", sig(lf), 
//				context(target(target), result("filter/result")));
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 50", value(ft), 50);
//		
//		// change the initialization of the filter to "3" now
//		ft = task("ft", sig(3, lf), 
//				context(target(target), result("filter/result")));
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 40", value(ft), 40);
//	}
	
//	@Test
//	public void t5_Test() throws Exception {
//		Task t5 = task("t5",
//				sig(groovy("x2 + x3", vars(var("x2", 20.0), var("x3", 80.0)))),
//				context(result("result/y")));
//
//		//logger.info("t5: " + value(t5));
//		assertEquals("Wrong value for 100.0", value(t5), 100.0);
//	}

	@Test
	public void t4_TaskTest() throws Exception {
		Task t4 = task("t4", sig("multiply", new Multiply(), double[].class),
				context(args(new double[] { 10.0, 50.0 }), result("result/y")));

		//logger.info("t4: " + value(t4));
		assertEquals("Wrong value for 500.0", value(t4), 500.0);
	}

//	@Test
//	public void t3_TaskTest() throws Exception {
//		Var<?> x3 = var("x3", expression("x3-e", "x1 - x2", vars("x1", "x2")));
//		Task t3 = task(
//				"t3",
//				sig(x3),
//				context("subtract", in(path("x1"), 40.0),
//						in(path("x2"), 10.0), result(path("result/y"))));
//		
//		//logger.info("t3: " + value(t3));
//		assertEquals("Wrong value for 30.0", value(t3), 30.0);
//
//	}
	
//	@Test
//	public void t3_Task2Test() throws Exception {
//		// testing with getValueEndsWith for vars in the context with prefixed paths
//		Var<?> x3 = var("x3", expression("x3-e", "x1 - x2", vars("x1", "x2")));
//		Task t3 = task(
//				"t3",
//				sig(x3),
//				context("subtract", in(path("arg/x1"), 40.0),
//						in(path("arg/x2"), 10.0), result(path("result/y"))));
//		
//		//logger.info("t3: " + value(t3));
//		assertEquals("Wrong value for 30.0", value(t3), 30.0);
//
//	}
	
}
	
