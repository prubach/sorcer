package junit.sorcer.core.invoker;

import static org.junit.Assert.*;
import static sorcer.co.operator.entry;
import static sorcer.eo.operator.condition;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.job;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.pipe;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
import static sorcer.po.operator.add;
import static sorcer.po.operator.alt;
import static sorcer.po.operator.asis;
import static sorcer.po.operator.cmdInvoker;
import static sorcer.po.operator.exertInvoker;
import static sorcer.po.operator.get;
import static sorcer.po.operator.inc;
import static sorcer.po.operator.invoke;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.loop;
import static sorcer.po.operator.methodInvoker;
import static sorcer.po.operator.model;
import static sorcer.po.operator.next;
import static sorcer.po.operator.opt;
import static sorcer.po.operator.par;
import static sorcer.po.operator.pars;
import static sorcer.po.operator.put;
import static sorcer.po.operator.runnableInvoker;
import static sorcer.po.operator.set;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;

import junit.sorcer.core.invoker.service.Volume;
import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.MultiplierImpl;
import junit.sorcer.core.provider.SubtractorImpl;
import net.jini.core.transaction.TransactionException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import sorcer.core.SorcerEnv;
import sorcer.core.context.model.par.Par;
import sorcer.core.context.model.par.ParModel;
import sorcer.core.invoker.AltInvoker;
import sorcer.core.invoker.Invocable;
import sorcer.core.invoker.Invoker;
import sorcer.core.invoker.OptInvoker;
import sorcer.core.provider.jobber.ServiceJobber;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.resolver.Resolver;
import sorcer.service.Condition;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.StringUtils;
import sorcer.util.exec.ExecUtils.CmdResult;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerRunner.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ju-arithmetic-api"
})
public class InvokerTest {
	private final static Logger logger = Logger.getLogger(InvokerTest.class
			.getName());

	// member subclass of Invocable with Context parameter used below with
	// contextMethodAttachmentWithArgs()
	// there are constructor's context and invoke metod's context as parameters
	ParModel pm = new ParModel();
	Par<Double> x = par("x", 10.0);
	Par<Double> y = par("y", 20.0);
	Par z = par("z", invoker("x - y", x, y));

	public class Update extends Invocable {
		public Update(Context context) {
			super(context);
		}

		public Double invoke(Context arg) throws Exception {
			x.setValue((Double) arg.getValue("x"));
			y.setValue((Double) context.getValue("y"));
			// x set from 'arg'
			Assert.assertEquals((Double)200.0d, value(x));
			// y set from construtor's context 'in'
			Assert.assertEquals((Double) 30.0, value(y));
			Assert.assertEquals((Double)170.0,  value(z));
			return value(x) + value(y) + (Double) value(pm, "z");
		}
	};

	@Test
	public void methodInvokerTest() throws RemoteException, ContextException {
		set(x, 10.0);
		set(y, 20.0);
		add(pm, x, y, z);

//		logger.info("x:" + value(pm, "x"));
//		logger.info("y:" + value(pm, "y"));
//		logger.info("y:" + value(pm, "z"));

		Context in = context(entry("x", 20.0), entry("y", 30.0));
		Context arg = context(entry("x", 200.0), entry("y", 300.0));
		add(pm, methodInvoker("invoke", new Update(in), arg));
		logger.info("call value:" + invoke(pm, "invoke"));
		assertEquals(400.0, invoke(pm, "invoke"));
	}

	@Test
	public void groovyInvokerTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {
		ParModel pm = model("par-model");
		add(pm, par("x", 10.0), par("y", 20.0));
		add(pm, invoker("expr", "x + y + 30", pars("x", "y")));
		logger.info("invoke value: " + invoke(pm, "expr"));
		assertEquals(60.0, invoke(pm, "expr"));
		logger.info("get value: " + value(pm, "expr"));
		assertEquals(60.0, value(pm, "expr"));
	}

	@Test
	public void invokeTaskTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {

		Task t4 = task(
				"t4",
				sig("multiply", MultiplierImpl.class),
				context("multiply", in("arg/x1", 50.0), in("arg/x2", 10.0),
						result("result/y")));

		// logger.info("invoke value:" + invoke(t4));
		assertEquals(500.0, invoke(t4));
	}

    @Test
    //@Ignore
	public void invokeJobTest() throws RemoteException, ContextException,
			SignatureException, ExertionException, TransactionException {
		Context c4 = context("multiply", in("arg/x1", 50.0),
				in("arg/x2", 10.0), result("result/y"));
		Context c5 = context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
				result("result/y"));

		// exertions
		Task t3 = task(
				"t3",
				sig("subtract", SubtractorImpl.class),
				context("subtract", in("arg/x1"), in("arg/x2"), out("result/y")));
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), c4);
		Task t5 = task("t5", sig("add", AdderImpl.class), c5);

		Job j1 = job("j1", sig("service", ServiceJobber.class),
					job("j2", t4, t5, sig("service", ServiceJobber.class)), t3,
					pipe(out(t4, "result/y"), in(t3, "arg/x1")),
					pipe(out(t5, "result/y"), in(t3, "arg/x2")),
					result("j1/t3/result/y"));

        Object result = invoke(j1);
		logger.info("invoke value:" + result.toString());
		assertEquals(400.0, result);
	}

    @Test
	public void invokeParJobTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {
		Context c4 = context("multiply", in("arg/x1"), in("arg/x2"),
				result("result/y"));
		Context c5 = context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
				result("result/y"));

		// exertions
		Task t3 = task(
				"t3",
				sig("subtract", SubtractorImpl.class),
				context("subtract", in("arg/x1"), in("arg/x2"), out("result/y")));
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), c4);
		Task t5 = task("t5", sig("add", AdderImpl.class), c5);

		Job j1 = job("j1", sig("service", ServiceJobber.class),
					job("j2", t4, t5, sig("service", ServiceJobber.class)), t3,
					pipe(out(t4, "result/y"), in(t3, "arg/x1")),
					pipe(out(t5, "result/y"), in(t3, "arg/x2")),
					result("j1/t3/result/y"));

		// logger.info("return path:" + j1.getReturnJobPath());
        assertNotNull(j1.getReturnPath());
		assertEquals("j1/t3/result/y", j1.getReturnPath().path);

		ParModel pm = model("par-model");
		add(pm, par("x1p", "arg/x1", c4), par("x2p", "arg/x2", c4), j1);
		// setting context parameters in a job
		set(pm, "x1p", 10.0);
		set(pm, "x2p", 50.0);

		add(pm, j1);
		// logger.info("call value:" + invoke(pm, "j1"));
		assertEquals(400.0, invoke(pm, "j1"));
	}

	@Test
	public void invokeVarTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {

		Par<Double> x1 = par("x1", 1.0);

		// logger.info("invoke value:" + invoke(x1));
		assertEquals(1.0, invoke(x1));
	}

	@Test
	public void substituteArgsTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {
		Par x1, x2, y;

		x1 = par("x1", 1.0);
		x2 = par("x2", 2.0);
		y = par("y", invoker("x1 + x2", x1, x2));
		
		logger.info("y: " + value(y));
		assertEquals(3.0, value(y));

		Object val = invoke(y, entry("x1", 10.0), entry ("x2", 20.0));
		logger.info("y: " + val);

		logger.info("y: " + value(y));
		assertEquals(30.0, value(y));
	}

	@Test
	public void exertionInvokerTest() throws RemoteException, ContextException,
			SignatureException, ExertionException {
		Context c4 = context("multiply", in("arg/x1"), in("arg/x2"),
				result("result/y"));
		Context c5 = context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
				result("result/y"));

		// exertions
		Task t3 = task(
				"t3",
				sig("subtract", SubtractorImpl.class),
				context("subtract", in("arg/x1"), in("arg/x2"), out("result/y")));
		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), c4);
		Task t5 = task("t5", sig("add", AdderImpl.class), c5);

		Job j1 = job("j1", sig("service", ServiceJobber.class),
				job("j2", t4, t5, sig("service", ServiceJobber.class)), t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));

		ParModel pm = model("par-model");
		add(pm, par("x1p", "arg/x1", c4), par("x2p", "arg/x2", c4), j1);
		// setting context parameters in a job
		set(pm, "x1p", 10.0);
		set(pm, "x2p", 50.0);

		add(pm, exertInvoker("invoke j1", j1, "j1/t3/result/y"));
		// logger.info("call value:" + invoke(pm, "invoke j1"));
		assertEquals(400.0, invoke(pm, "invoke j1"));
	}

    @Test
	public void cmdInvokerTest() throws SignatureException, ExertionException,
            ContextException, IOException, ResolverException {
        String[] cps = ResolverHelper.getResolver().getClassPathFor("org.sorcersoft.sorcer:model-beans:" + SorcerEnv.getSorcerVersion());
        //String cp = SorcerEnv.getHomeDir() + "/tests/int-tests/target/test-classes" + File.pathSeparator
        String cp = //Resolver.resolveAbsolute("org.sorcersoft.sorcer:model-beans") + File.pathSeparator
                Resolver.resolveAbsolute("com.sorcersoft.river:jsk-platform") + File.pathSeparator + StringUtils.join(cps, File.pathSeparator);

        String cmdToInvoke = "java -cp  " + cp + " " + Volume.class.getName() + " cylinder";
        logger.info("To invoke: " + cmdToInvoke);

        Invoker cmd = cmdInvoker("volume", cmdToInvoke);

		par("multiply", invoker("x * y", pars("x", "y")));
		ParModel pm = add(model(), par("x", 10.0), par("y"), par(cmd),
				par("add", invoker("x + y", pars("x", "y"))));

		CmdResult result = (CmdResult) invoke(pm, "volume");
		// get from the result the volume of cylinder and assign to y parameter
		if (result.getExitValue() != 0) {
			logger.info("cmd result: " + result);
			throw new RuntimeException();
		}
		Properties props = new Properties();
		props.load(new StringReader(result.getOut()));
		set(pm, "y", new Double(props.getProperty("cylinder/volume")));

		logger.info("x value:" + value(pm, "x"));
		logger.info("y value:" + value(pm, "y"));
		logger.info("multiply value:" + value(pm, "add"));
		assertEquals(47.69911184307752, value(pm, "add"));
	}

	@Test
	public void conditionalInvoker() throws RemoteException, ContextException {
		final ParModel pm = new ParModel("par-model");
		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("condition", invoker("x > y", pars("x", "y")));

		assertEquals(pm.getValue("x"), 10.0);
		assertEquals(pm.getValue("y"), 20.0);
		// logger.info("condition value: " + pm.getValue("condition"));
		assertEquals(pm.getValue("condition"), false);

		pm.putValue("x", 300.0);
		pm.putValue("y", 200.0);
		 logger.info("condition value: " + pm.getValue("condition"));
		assertEquals(pm.getValue("condition"), true);

		// enclosing class conditional context
		Condition c = new Condition() {
			@Override
			public boolean isTrue() throws ContextException {
				return (Boolean) pm.getValue("condition");
			}
		};
		assertEquals(c.isTrue(), true);

		// provided conditional context
		Condition eval = new Condition(pm) {
			@Override
			public boolean isTrue() throws ContextException {
				return (Boolean) conditionalContext.getValue("condition");
			}
		};
		assertEquals(true, eval.getValue());
	}

	@Test
	public void optInvokerTest() throws RemoteException, ContextException {
		ParModel pm = new ParModel("par-model");

		OptInvoker opt = new OptInvoker("opt", new Condition(pm,
				"{ x, y -> x > y }", "x", "y"), 
					invoker("x + y", pars("x", "y")));

		pm.add(opt);
		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);

		logger.info("x: " + value(pm, "x"));
		logger.info("y: " + value(pm, "y"));
		logger.info("opt" + value(pm, "opt"));
		
//		assertEquals(opt.getValue(), null);
//
//		pm.putValue("x", 300.0);
//		pm.putValue("y", 200.0);
//		logger.info("opt value: " + opt.getValue());
//		assertEquals(opt.getValue(), 500.0);
	}

	@Test
	public void polOptInvokerTest() throws RemoteException, ContextException {
		ParModel pm = model("par-model");
		add(pm,
				par("x", 10.0),
				par("y", 20.0),
				opt("opt", condition(pm, "{ x, y -> x > y }", "x", "y"),
						invoker("x + y", pars("x", "y"))));

		logger.info("opt value: " + value(pm, "opt"));
		assertNull(value(pm, "opt"));

		put(pm, "x", 300.0);
		put(pm, "y", 200.0);
		logger.info("opt value: " + value(pm, "opt"));
		assertEquals(500.0, value(pm, "opt"));
	}

	@Test
	public void altInvokerTest() throws RemoteException, ContextException {
		ParModel pm = new ParModel("par-model");
		pm.putValue("x", 30.0);
		pm.putValue("y", 20.0);
		pm.putValue("x2", 50.0);
		pm.putValue("y2", 40.0);
		pm.putValue("x3", 70.0);
		pm.putValue("y3", 60.0);

		OptInvoker opt1 = new OptInvoker("opt1", condition(pm,
				"{ x, y -> x > y }", "x", "y"), invoker("x + y + 10",
						pars("x", "y")));

		OptInvoker opt2 = new OptInvoker("opt2", condition(pm,
				"{ x2, y2 -> x2 > y2 }", "x2", "y2"), invoker(
				"x + y + 20", pars("x", "y")));

		OptInvoker opt3 = new OptInvoker("op3", condition(pm,
				"{ x3, y3 -> x3 > y3 }", "x3", "y3"), invoker(
				"x + y + 30", pars("x", "y")));

		// no condition means condition(true)
		OptInvoker opt4 = new OptInvoker("opt4", invoker("x + y + 40",
				pars("x", "y")));

		AltInvoker alt = new AltInvoker("alt", opt1, opt2, opt3, opt4);
		add(pm, opt1, opt2, opt3, opt4, alt);

		logger.info("opt1 value: " + value(opt1));
		assertEquals(60.0, value(opt1));
		logger.info("opt2 value: " + value(opt2));
		assertEquals(70.0, value(opt2));
		logger.info("opt3 value: " + value(opt3));
		assertEquals(80.0, value(opt3));
		logger.info("opt4 value: " + value(opt4));
		assertEquals(90.0, value(opt4));
		logger.info("alt value: " + value(alt));
		assertEquals(60.0, value(alt));

		pm.putValue("x", 300.0);
		pm.putValue("y", 200.0);
		logger.info("opt value: " + value(alt));
		assertEquals(510.0, value(alt));

		pm.putValue("x", 10.0);
		pm.putValue("y", 20.0);
		pm.putValue("x2", 40.0);
		pm.putValue("y2", 50.0);
		pm.putValue("x3", 50.0);
		pm.putValue("y3", 60.0);
		logger.info("opt value: " + alt.invoke());
		assertEquals(70.0, value(alt));

		pm.putValue("x2", 50.0);
		pm.putValue("y2", 40.0);
		logger.info("opt value: " + alt.invoke());
		assertEquals(50.0, value(alt));
	}

	@Test
	public void polAltInvokerTest() throws RemoteException, ContextException {
		ParModel pm = model("par-model");
		// add(pm, entry("x", 10.0), entry("y", 20.0), par("x2", 50.0),
		// par("y2", 40.0), par("x3", 50.0), par("y3", 60.0));
		add(pm, par("x", 10.0), par("y", 20.0), par("x2", 50.0),
				par("y2", 40.0), par("x3", 50.0), par("y3", 60.0));

		AltInvoker alt = alt(
				"alt",
				opt("opt1", condition(pm, "{ x, y -> x > y }", "x", "y"),
						invoker("x + y + 10", pars("x", "y"))),
				opt("opt2", condition(pm, "{ x2, y2 -> x2 > y2 }", "x2", "y2"),
						invoker("x + y + 20", pars("x", "y"))),
				opt("opt3", condition(pm, "{ x3, y3 -> x3 > y3 }", "x3", "y3"),
						invoker("x + y + 30", pars("x", "y"))),
				opt("opt4", invoker("x + y + 40", pars("x", "y"))));

		add(pm, alt, get(alt, 0), get(alt, 1), get(alt, 2), get(alt, 3));

		logger.info("opt1 value : " + value(pm, "opt1"));
		assertNull(value(pm, "opt1"));
		logger.info("opt2 value: " + value(pm, "opt2"));
		assertEquals(50.0, value(pm, "opt2"));
		logger.info("opt3 value: " + value(pm, "opt3"));
		assertNull(value(pm, "opt3"));
		logger.info("opt4 value: " + value(pm, "opt4"));
		assertEquals(70.0, value(pm, "opt4"));
		logger.info("alt value: " + value(alt));
		assertEquals(50.0, value(alt));

		put(pm, entry("x", 300.0), entry("y", 200.0));
		logger.info("alt value: " + value(alt));
		assertEquals(510.0, value(alt));

		put(pm, entry("x", 10.0), entry("y", 20.0), entry("x2", 40.0),
				entry("y2", 50.0), entry("x3", 50.0), entry("y3", 60.0));
		logger.info("alt value: " + value(alt));
		assertEquals(70.0, value(alt));
	}


    @Ignore("Ignored due to stange errors - probably race condition")
	@Test
	public void loopInvokerTest() throws RemoteException, ContextException {
		final ParModel pm = model("par-model");
		add(pm, entry("x", 1));
		add(pm, par("y", invoker("x + 1", pars("x"))));

		// update x and y for the loop condition (z) depends on
		Runnable update = new Runnable() {
			public void run() {
				try {
					while ((Integer) value(pm, "x") < 30) {
						set(pm, "x", (Integer) value(pm, "x") + 1);
						 System.out.println("running ... " + value(pm, "x"));
						Thread.sleep(100);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		add(pm, runnableInvoker("update", update));
		invoke(pm, "update");

		add(pm,
				loop("loop", condition(pm, "{ x -> x < 20 }", "x"),
						(Invoker) asis((Par) asis(pm, "y"))));


		logger.info("loop value: " + value(pm, "loop"));
		assertEquals(new Integer(20), (Integer) value(pm, "loop"));
	}

	@Test
	public void incrementorBy1Test() throws RemoteException, ContextException {
		ParModel pm = model("par-model");
		add(pm, entry("x", 1));
		add(pm, par("y", invoker("x + 1", pars("x"))));
		add(pm, inc("y++", invoker(pm, "y")));

		for (int i = 0; i < 10; i++) {
			logger.info("" + value(pm, "y++"));
		}
		assertEquals(13, value(pm, "y++"));
	}

	@Test
	public void incrementorBy2Test() throws RemoteException, ContextException {
		ParModel pm = model("par-model");
		add(pm, entry("x", 1));
		add(pm, par("y", invoker("x + 1", pars("x"))));
		add(pm, inc("y++2", invoker(pm, "y"), 2));

		for (int i = 0; i < 10; i++) {
			logger.info("" + value(pm, "y++2"));
		}
		assertEquals(24, value(pm, "y++2"));
	}

	@Test
	public void incrementorDoubleTest() throws RemoteException,
			ContextException {
		ParModel pm = model("par-model");
		add(pm, entry("x", 1.0));
		add(pm, par("y", invoker("x + 1.2", pars("x"))));
		add(pm, inc("y++2.1", invoker(pm, "y"), 2.1));

		for (int i = 0; i < 10; i++) {
			logger.info("" + next(pm, "y++2.1"));
		}
		// logger.info("" + value(pm,"y++2.1"));
		assertEquals(25.300000000000004, value(pm, "y++2.1"));
	}
}
