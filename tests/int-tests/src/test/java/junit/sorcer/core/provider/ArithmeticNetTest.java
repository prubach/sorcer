/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package junit.sorcer.core.provider;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.junit.*;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Wait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.inEnt;
import static sorcer.co.operator.outEnt;
import static sorcer.eo.operator.*;


/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ju-arithmetic-api"})
@SorcerServiceConfiguration(
        { ":ju-arithmetic-cfg-all", ":ju-arithmetic-cfg-ctx" })
public class ArithmeticNetTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ArithmeticNetTest.class.getName());

	@Test
	public void arithmeticNodeTest() throws Exception {
		
		Task t5 = task(


				"t5",
				sig("add", Adder.class),
				context("add", inEnt("arg, x1", 20.0),
						inEnt("arg, x2", 80.0), result("result, y")));
		
		t5 = exert(t5);
		//logger.info("t5 dataContext: " + dataContext(t5));
		//logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", 100.0, value(t5));
	}
	
	@Test
	public void arithmeticMultiServiceTest() throws Exception {
		
		Task t5 = task(
				"t5",
				sig("add", Arithmetic.class),
				context("add", inEnt("arg, x1", 20.0),
						inEnt("arg, x2", 80.0), result("result, y")));
		
		t5 = exert(t5);
		//logger.info("t5 dataContext: " + dataContext(t5));
		logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", 100.0, get(t5));
	}
	
	@Test
	public void arithmeticSpaceTaskTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", inEnt("arg/x1", 20.0),
						inEnt("arg/x2", 80.0), outEnt("result/y", null)),
				strategy(Access.PULL, Wait.YES));
		
		logger.info("t5 init dataContext: " + context(t5));
		
		t5 = exert(t5);
		logger.info("t5 dataContext: " + context(t5));
		logger.info("t5 value: " + get(t5, "result/y"));
		assertEquals("Wrong result", 100.0, get(t5, "result/y"));
	}
	
	@Test
	public void exertJobPullParTest() throws Exception {
		Job job = createJob(Flow.PAR);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + serviceContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}
	
	@Test
	public void exertJobPullSeqTest() throws Exception {
		Job job = createJob(Flow.SEQ);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + serviceContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}
	
	// two level job composition with PULL and PAR execution
	private Job createJob(Flow flow) throws Exception {
		Task t3 = task("t3", sig("subtract", Subtractor.class), 
				context("subtract", inEnt("arg/x1", null), inEnt("arg/x2", null),
						outEnt("result/y", null)));

		Task t4 = task("t4", sig("multiply", Multiplier.class), 
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						outEnt("result/y", null)));

		Task t5 = task("t5", sig("add", Adder.class), 
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						outEnt("result/y", null)));

		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
		//Job job = job("j1",
		return job("j1", //sig("service", RemoteJobber.class),
				//job("j2", t4, t5),
				job("j2", t4, t5, strategy(flow, Access.PULL)),
				t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
	}

    @Test
    public void contexterTest() throws Exception {
        Task cxtt = task("addContext", sig("getContext", AddContext.createContext()),
                context("add", inEnt("arg/x1"), inEnt("arg/x2")));

        Context result = context(exert(cxtt));
//		logger.info("contexter context: " + result);
        assertEquals(get(result, "arg/x1"), 20.0);
        assertEquals(get(result, "arg/x2"), 80.0);

    }

    // Needs ju-arithmetic-cfg-ctx service
    @Test
    public void netContexterTaskTest() throws Exception {
        Task t5 = task("t5", sig("add", Adder.class),
                sig("getContext", Contexter.class, "Add Contexter", Signature.APD),
                context("add", inEnt("arg/x1"), inEnt("arg/x2"),
                        result("result/y")));

        Context result =  context(exert(t5));
//		logger.info("contexter context: " + result);
        assertEquals(get(result, "result/y"), 100.0);
    }

    @Test
    public void objectContexterTaskTest() throws Exception {
        Task t5 = task("t5", sig("add", AdderImpl.class),
                type(sig("getContext", AddContext.createContext()), Signature.APD),
                context("add", inEnt("arg/x1"), inEnt("arg/x2"),
                        result("result/y")));

        Context result = context(exert(t5));
//		logger.info("task context: " + result);
        assertEquals(get(result, "result/y"), 100.0);
    }
}
