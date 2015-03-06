/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package junit.sorcer.core.exertion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.*;
import static sorcer.eo.operator.*;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.par;
import static sorcer.po.operator.pars;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.sorcer.core.provider.Adder;
import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.Multiply;

import org.junit.Test;

import org.junit.runner.RunWith;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.par.Par;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.ObjectSignature;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerRunner;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Wait;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(SorcerRunner.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
//        "org.sorcersoft.sorcer:ju-arithmetic-api"
//})
public class TaskTest {
	private final static Logger logger = LoggerFactory.getLogger(TaskTest.class
			.getName());

    @Test
	public void freeArithmeticTaskTest() throws ExertionException, SignatureException, ContextException {
		//to test tracing of execution enable ServiceExertion.debug 		
		Exertion task = task("add",
				sig("add"),
				context(inEnt("arg/x1"), inEnt("arg/x2"),
						result("result/y")));
		
		logger.info("get task: " + task);
		logger.info("get dataContext: " + context(task));
		
		Object val = value(task, inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
				strategy(sig("add", AdderImpl.class), Access.PUSH, Wait.YES));
		
		//logger.info("get value: " + val);
		assertEquals("Wrong value for 100", 100.0, val);
	}
	
	@Test
	public void arithmeticTaskTest() throws ExertionException, SignatureException, ContextException, RemoteException {
		Task task = task("add",
				sig("add", AdderImpl.class),
				context(inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						result("result/y")));

        //to test tracing of execution enable ControlContext.traceEnabled
        task.getControlContext().setTraceEnabled(true);
		
		task = exert(task);
//		logger.info("exerted: " + task);
//		print(exceptions(task));
//		print(trace(task));
		Double val = (Double)value(task);

		
//		logger.info("get value: " + val);
		assertTrue("Wrong value for 100.0", val == 100.0);
		//logger.info("exec trace: " + trace(task));
		//logger.info("trace  size: " + trace(task).size());
		//assertTrue(trace(task).size() == 1);
//		logger.info("exceptions: " + exceptions(task));
		assertEquals("Exception list", 0, exceptions(task).size());

		val = (Double)get(task, "result/y");
		//logger.info("get value: " + val);
		assertEquals("result/y", (Object) 100d, val);
		
		task = exert(task);
		val = (Double)get(context(task), "result/y");
		//logger.info("get value: " + val);
		assertTrue("Wrong value for 100.0", val == 100.0);
		//assertTrue(trace(task).size() == 2);
		assertTrue(exceptions(task).size() == 0);
		
		/*put(task, ent("arg/x1", 1.0), ent("arg/x2", 5.0));
		val = (Double)value(task);
		//logger.info("evaluate: " + val);
		assertTrue("Wrong value for 6.0", val == 6.0);
				
		val = (Double)value(task, ent("arg/x1", 2.0), ent("arg/x2", 10.0));
		//logger.info("evaluate: " + val);
		assertTrue("Wrong value for 12.0", val == 12.0);
		
		//logger.info("task dataContext: " + dataContext(task));
		//logger.info("get value: " + get(task));
		assertTrue("Wrong value for 12.0", get(task).equals(12.0));*/
	}
	
//	@Test
//	public void exertEvaluationTaskTest() throws ExertionException, ContextException,
//			RemoteException, TransactionException, SignatureException {
//
//		Task dateTask = new EvaluationTask(groovy("new Date()"));
////		logger.info("dateTask value: "
////				+  dateTask.exert().getDataContext().getValue());
//		assertTrue("Wrong date",
//				("" + dateTask.exert().getDataContext().getValue())
//						.equals("" + new Date()));
//		
//		VarList vl = inputVars(loop(6), "x");
//		put(vl, ent("x2", 2.0), ent("x3", 3.0));
//		Task exprTask = new EvaluationTask(groovy("x2 + x3",
//				vl.selectVars("x2", "x3")));
//		//logger.info("exprTask: " + (EvaluationTask) exprTask.exert());
//		assertEquals("Wrong value for 5",
//				((EvaluationTask) exprTask.exert()).getValue(), 5.0);
//			
//		Task dateTask2 = task(sig(groovy("new Date()")), dataContext(result("evaluation/result")));
////		logger.info("dateTask value: "
////				+ get(exert(dateTask2), "evaluation/result"));
//		assertTrue("Wrong date",
//				("" + get(exert(dateTask2), "evaluation/result"))
//						.equals("" + new Date()));
//		
//		Task exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), dataContext(result("evaluation/result")));
//		//logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 5",
//				get(exert(exprTask2), "evaluation/result"), 5.0);
//		
//		
//		// dependent var values are explicitly in the dataContext
//		exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), dataContext(ent("x2", 20.0),
//						ent("x3", 30.0), result("evaluation/result")));
//		logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 50.0",
//				get(exert(exprTask2), "evaluation/result"), 50.0);
//		
//		
//		// dependent var values as the argument array in the dataContext
//		exprTask2 = task(sig(groovy("x2 + x3",
//				selectVars(vl, "x2", "x3"))), dataContext(args(ent("x2", 20.0),
//						ent("x3", 30.0)), result("evaluation/result")));
//		logger.info("exprTask2: " + get(exert(exprTask2), "evaluation/result"));
//		assertEquals("Wrong value for 50.0",
//				get(exert(exprTask2), "evaluation/result"), 50.0);
//	}


    @Test
    public void exertObjectTaskTest() throws Exception {
        ObjectTask objTask = new ObjectTask("t4", new ObjectSignature("multiply", Multiply.class, double[].class));
        objTask.getControlContext().setTraceEnabled(true);

        ServiceContext cxt = new ServiceContext();
        Object arg = new double[] { 10.0, 50.0 };
        //cxt.setReturnPath("result/y").setArgs(new double[] {10.0, 50.0});
        cxt.setReturnPath("result/y").setArgs(arg);
        objTask.setContext(cxt);

        //logger.info("objTask value: " + value(objTask));
        assertEquals("Wrong value for 500.0", value(objTask), 500.0);

        ObjectTask objTask2 = (ObjectTask)task("t4", sig("multiply", new Multiply(), new Class[] {double[].class}),
                context(args(new double[] {10.0, 50.0}), result("result/y")));
        //logger.info("objTask2 value: " + value(objTask2));
        assertEquals("Wrong value for 500.0", value(objTask2), 500.0);
    }


    @Test
    public void t5_Test() throws Exception {
        Task t5 = task("t5",
                sig(invoker("x2 + x3", par("x2", 20.0), par("x3", 80.0))),
                context(result("result/y")));

        //logger.info("t5: " + value(t5));
        assertEquals("Wrong value for 100.0", 100.0, value(t5));
    }

//	@Test
//	public void exertVarTaskTest() throws Exception {	
//		Var<Double> x1 = var("x1", 10.0);
//		Var<Integer> x2 = var("x2", 2);		
//		Var<?> y = var("y", expression("x1 + x2", args(x1, x2)));
//		assertEquals(value(y), 12.0);
//		
//		Map<Double, Integer> map = map(ent(1.0,10), ent(2.0,20), ent(12.0,30), ent(24.0,100));
//		Var<?> x3 = var("x3", expression("x3-e1", "x1 + x2", args(x1, x2)), new MapKeyFilter("x3-f1", map));
//		//logger.info("x3 value: " + value(x3));
//		assertEquals(value(x3), 30);
//		
//		FidelityInfo eval = new FidelityInfo("x3", "x3-e1","", "x3-f1");
//		Task varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		ServiceContext cxt = new ServiceContext();
//		cxt.setReturnPath("var/result");
//		varTask.setDataContext(cxt);
//		//logger.info("varTask value: " + value(varTask));
//		assertEquals("Wrong value for 30", value(varTask), 30);
//		
//		// dataContext with values for arg vars as key/value entries
//		varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		cxt = new ServiceContext();
//		cxt.put("x1", 20.0);
//		cxt.put("x2", 4);
//		cxt.setReturnPath("var/result");		
//		varTask.setDataContext(cxt);
//		//logger.info("varTask value: " + value(varTask));
//		assertEquals("Wrong value for 100", value(varTask), 100);
//		
//		// dataContext with values for arg vars as the ARGS array
//		varTask = new VarTask("x3-t", new VarSignature(eval, x3));
//		cxt = new ServiceContext();
//		cxt.setArgs(map(ent("x1", 20.0), ent("x2", 4.0)));
//		cxt.setReturnPath("var/result");
//		varTask.setDataContext(cxt);
//		//logger.info("ARGS varTask value: " + value(varTask));
//		assertEquals("Wrong value for 100", value(varTask), 100);
//		
//		// dataContext with values for arg vars as the ARGS array
//		varTask = task("x3-t", sig(eval, x3), 
//				dataContext(args(map(ent("x1", 20.0),
//							ent("x2", 4.0)),
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
//		ft.setDataContext(cxt);
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 50", value(ft), 50);
//
//		// the initialization of the filter is "4", the fifth element from the list
//		ft = task("ft", sig(lf), 
//				dataContext(target(target), result("filter/result")));
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 50", value(ft), 50);
//		
//		// change the initialization of the filter to "3" now
//		ft = task("ft", sig(3, lf), 
//				dataContext(target(target), result("filter/result")));
//		//logger.info("ft value: " + value(ft));
//		assertEquals("Wrong value for 40", value(ft), 40);
//	}
	
//	@Test
//	public void t5_Test() throws Exception {
//		Task t5 = task("t5",
//				sig(groovy("x2 + x3", vars(var("x2", 20.0), var("x3", 80.0)))),
//				dataContext(result("result/y")));
//
//		//logger.info("t5: " + value(t5));
//		assertEquals("Wrong value for 100.0", value(t5), 100.0);
//	}

	@Test
	public void t4_TaskTest() throws Exception {
		Task t4 = task("t4", sig("multiply", new Multiply(), new Class[] {double[].class }),
				context(args(new Object[]{new double[]{10.0, 50.0}}), result("result/y")));

		//logger.info("t4: " + value(t4));
		assertEquals("result/y", 500.0, value(t4));
	}

    @Test
    public void t3_TaskTest() throws Exception {
        Par x3 = par("x3", invoker("x3-e", "x1 - x2", pars("x1", "x2")));
        Task t3 = task(
                "t3",
                sig(x3),
                context("subtract", inEnt(path("x1"), 40.0),
                        inEnt(path("x2"), 10.0), result(path("result/y"))));

        //logger.info("t3: " + value(t3));
        assertEquals("Wrong value for 30.0", value(t3), 30.0);

    }


    @Test
    public void t3_Task2Test() throws Exception {
        // testing with getValueEndsWith for vars in the context with prefixed paths
        Par x3 = par("x3", invoker("x3-e", "x1 - x2", pars("x1", "x2")));
        Task t3 = task(
                "t3",
                sig(x3),
                context("subtract", inEnt(path("arg/x1"), 40.0),
                        inEnt(path("arg/x2"), 10.0), result(path("result/y"))));

        //logger.info("t3: " + value(t3));
        assertEquals("Wrong value for 30.0", value(t3), 30.0);

    }

    @Test
    //@Ignore
    public void deployTest() throws Exception {
        Task t5 = task("f5",
                sig("add", Adder.class,
                        deploy(configuration("${sys.sorcer.home}/configs/int-tests/deployment/AdderProviderConfig.groovy"))),
                context("add", inEnt("arg/x3", 20.0d), inEnt("arg/x4", 80.0d),
                        outEnt("result/y")),
                strategy(Strategy.Provision.YES));
        logger.info("t5 is provisionable: " + t5.isProvisionable());
        assertTrue(t5.isProvisionable());
    }


}
	
