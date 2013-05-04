package junit.sorcer.core.provider;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.entry;
import static sorcer.co.operator.from;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.cxt;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.job;
import static sorcer.eo.operator.jobContext;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.pipe;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.srv;
import static sorcer.eo.operator.strategy;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
//import static sorcer.vo.operator.args;
//import static sorcer.vo.operator.expr;
//import static sorcer.vo.operator.expression;
//import static sorcer.vo.operator.inputVars;
//import static sorcer.vo.operator.outputVars;
//import static sorcer.vo.operator.put;
//import static sorcer.vo.operator.var;
//import static sorcer.vo.operator.varModel;
//import static sorcer.vo.operator.vars;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import sorcer.core.SorcerConstants;
//import sorcer.core.dataContext.model.VarModel;
import sorcer.core.provider.jobber.ServiceJobber;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Wait;
import sorcer.service.Task;
import sorcer.util.Sorcer;
//import sorcer.vfe.Var;

/**
 * @author Mike Sobolewski
 */

public class ArithmeticNoNetTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(ArithmeticNetTest.class.getName());
	
	static {
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/sorcer.policy");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBaseByArtifacts(new String[] { 
				"org.sorcersoft.sorcer:sos-platform", 
				"org.sorcersoft.sorcer:ju-arithmetic-api" }); 
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Test
//	public void arithmeticVars() throws Exception {
//		Var y = var("y", expr("(x1 * x2) - (x3 + x4)", args("x1", "x2", "x3", "x4"))); 
//		Object val = value(y, entry("x1", 10.0), entry("x2", 50.0), entry("x3", 20.0), entry("x4", 80.0));
//		//logger.info("y value: " + val);
//		assertEquals(val, 400.0);
//
//	}
	
//	@Test
//	public void createJobModel() throws Exception {
//		
//		VarModel vm = varModel("Hello Arithmetic #1", 
//				inputVars(var("x1"), var("x2"), var("x3", 20.0), var("x4", 80.0)),
//				outputVars(var("t4", expression("x1 * x2", args(vars("x1", "x2")))), 
//						var("t5", expression("x3 + x4", args(vars("x3", "x4")))),
//						var("j1", expression("t4 - t5", args(vars("t4", "t5"))))));
//				
//		//logger.info("t4 value: " + value(var(vm, "t4")));				
//		assertEquals(value(var(vm, "t4")), null);
//
//		//logger.info("t5 value: " + value(var(vm, "t5")));
//		assertEquals(value(var(vm, "t5")), 100.0);
//		
//		//logger.info("j1 value: " + value(var(vm, "j1")));
//		assertEquals(value(var(vm, "j1")), null);
//		
//		//logger.info("j1 value: " + value(var(put(vm, entry("x1", 10.0), entry("x2", 50.0)), "j1")));
//		assertEquals(value(var(put(vm, entry("x1", 10.0), entry("x2", 50.0)), "j1")), 400.0);
//		//logger.info("j1 value: " + value(var(vm, "j1")));
//		assertEquals(value(var(vm, "j1")), 400.0);
//
//	}
	
//	@Test
//	public void createMogJob() throws Exception {
//		
//		VarModel vm = varModel("Hello Arithmetic #2", 
//			inputVars(var("x1"), var("x2"), var("x3", 20.0), var("x4")),
//			outputVars(var("t4", expression("x1 * x2", args(vars("x1", "x2")))), 
//				var("t5", task("t5", sig("add", AdderImpl.class), 
//					cxt("add", in("arg/x3", var("x3")), in("arg/x4", var("x4")), result("result/y")))),
//				var("j1", expression("t4 - t5", args(vars("t4", "t5"))))));
//		
//		assertEquals(value(var(put(vm, entry("x1", 10.0), entry("x2", 50.0), 
//				entry("x4", 80.0)), "j1")), 400.0);
//		//logger.info("j1 value: " + value(var(vm, "j1")));
//		assertEquals(value(var(vm, "j1")), 400.0);
//	}

	@Test
	public void exertSrvTest() throws Exception {
		Job srv = createSrv();
		logger.info("srv job dataContext: " + jobContext(srv));
		logger.info("srv j1/t3 dataContext: " + context(srv, "j1/t3"));
		logger.info("srv j1/j2/t4 dataContext: " + context(srv, "j1/j2/t4"));
		logger.info("srv j1/j2/t5 dataContext: " + context(srv, "j1/j2/t5"));
		
		srv = exert(srv);
		logger.info("srv job dataContext: " + jobContext(srv));
		
		//logger.info("srv value @  t3/arg/x2 = " + get(srv, "j1/t3/arg/x2"));
		assertEquals(get(srv, "j1/t3/arg/x2"), 100.0);
	}
	
	// two level job composition
	@SuppressWarnings("unchecked")
	private Job createSrv() throws Exception {
		Task t3 = srv("t3", sig("subtract", SubtractorImpl.class), 
				cxt("subtract", in("arg/x1"), in("arg/x2"),
						out("result/y")));

		Task t4 = srv("t4", sig("multiply", MultiplierImpl.class), 
				//cxt("multiply", in("super/arg/x1"), in("arg/x2", 50.0),
				cxt("multiply", in("arg/x1", 10.0), in("arg/x2", 50.0),
						out("result/y")));

		Task t5 = srv("t5", sig("add", AdderImpl.class), 
				cxt("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
						out("result/y")));

		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
		//Job j1= job("j1", job("j2", t4, t5, strategy(Flow.PARALLEL, Access.PULL)), t3,
		Job job = srv("j1", sig("execute", ServiceJobber.class),
					cxt(in("arg/x1", 10.0), result("job/result", from("j1/t3/result/y"))), 
				srv("j2", sig("execute", ServiceJobber.class), t4, t5),
				t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
				
		return job;
	}
	
	@Ignore
	@Test
	public void arithmeticNodeTest() throws Exception {
		
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg, x1", 20.0),
						in("arg, x2", 80.0), result("result, y")));
		
		t5 = exert(t5);
		//logger.info("t5 dataContext: " + dataContext(t5));
		//logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", value(t5), 100.0);
	}
	
	@Ignore
	@Test
	public void arithmeticMultiServiceTest() throws Exception {
		
		Task t5 = task(
				"t5",
				sig("add", Arithmetic.class),
				context("add", in("arg, x1", 20.0),
						in("arg, x2", 80.0), result("result, y")));
		
		t5 = exert(t5);
		//logger.info("t5 dataContext: " + dataContext(t5));
		logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", get(t5), 100.0);
	}
	
	@Ignore
	@Test
	public void arithmeticSpaceTaskTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg/x1", 20.0),
						in("arg/x2", 80.0), out("result/y", null)),
				strategy(Access.PULL, Wait.YES));
		
		logger.info("t5 init dataContext: " + context(t5));
		
		t5 = exert(t5);
		logger.info("t5 dataContext: " + context(t5));
		logger.info("t5 value: " + get(t5, "result/y"));
		assertEquals("Wrong value for 100.0", get(t5, "result/y"), 100.0);
	}
	
	@Ignore
	@Test
	public void exertJobPullParTest() throws Exception {
		Job job = createJob(Flow.PAR);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}
	
	@Ignore
	@Test
	public void exertJobPullSeqTest() throws Exception {
		Job job = createJob(Flow.SEQ);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}
	
	// two level job composition with PULL and PAR execution
	private Job createJob(Flow flow) throws Exception {
		Task t3 = task("t3", sig("subtract", Subtractor.class), 
				context("subtract", in("arg/x1", null), in("arg/x2", null),
						out("result/y", null)));

		Task t4 = task("t4", sig("multiply", Multiplier.class), 
				context("multiply", in("arg/x1", 10.0), in("arg/x2", 50.0),
						out("result/y", null)));

		Task t5 = task("t5", sig("add", Adder.class), 
				context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
						out("result/y", null)));

		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
		//Job job = job("j1",
		Job job = job("j1", //sig("service", RemoteJobber.class), 
				//job("j2", t4, t5),
				job("j2", t4, t5, strategy(flow, Access.PULL)), 
				t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
				
		return job;
	}
}
