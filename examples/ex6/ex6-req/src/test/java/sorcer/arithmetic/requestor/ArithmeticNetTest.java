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
package sorcer.arithmetic.requestor;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.arithmetic.provider.Adder;
import sorcer.arithmetic.provider.Arithmetic;
import sorcer.arithmetic.provider.Multiplier;
import sorcer.arithmetic.provider.Subtractor;
import sorcer.core.SorcerConstants;
import sorcer.junit.SorcerServiceConfiguration;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Wait;
import sorcer.service.Task;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;


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
                 "org.sorcersoft.sorcer:sos-platform",
                "org.sorcersoft.sorcer:ex6-api"})
@SorcerServiceConfiguration("org.sorcersoft.sorcer:ex6-cfg-all:" + SorcerConstants.SORCER_VERSION)
public class ArithmeticNetTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ArithmeticNetTest.class.getName());

	@Test
	public void netTaskTest() throws Exception {

		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", inEnt("arg, x1", 20.0), inEnt("arg, x2", 80.0),
						result("result, y")));
		t5 = exert(t5);
		// logger.info("t5 context: " + context(t5));
		// logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", value(t5), 100.0);
	}

	@Test
	public void multiServiceProviderTest() throws Exception {

		Task t5 = task(
				"t5",
				sig("add", Arithmetic.class),
				context("add", inEnt("arg, x1", 20.0), inEnt("arg, x2", 80.0),
						result("result, y")));

		t5 = exert(t5);
		// logger.info("t5 context: " + context(t5));
		// logger.info("t5 value: " + get(t5));
		assertEquals("Wrong value for 100.0", 100.0, value(t5));
	}

	@Test
	public void spaceTaskTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						outEnt("result/y")), strategy(Access.PULL, Wait.YES));

		t5 = exert(t5);
		logger.info("t5 context: " + context(t5));
		logger.info("t5 value: " + get(t5, "result/y"));
		assertEquals("Wrong value for 100.0", get(t5, "result/y"), 100.0);
	}

	@Test
	public void pushParJobTest() throws Exception {
		Job job = createJob(Flow.PAR, Access.PUSH);
		job = exert(job);
		// logger.info("job j1: " + job);
		// logger.info("job j1 job context: " + context(job));
		logger.info("job j1 job context: " + serviceContext(job));
		// logger.info("job j1 value @ j1/t3/result/y = " + get(job,
		// "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}

	@Test
	public void pushSeqJobTest() throws Exception {
		Job job = createJob(Flow.SEQ, Access.PUSH);
		job = exert(job);
		// logger.info("job j1: " + job);
		// logger.info("job j1 job context: " + context(job));
		logger.info("job j1 job context: " + serviceContext(job));
		// logger.info("job j1 value @ j1/t3/result/y = " + get(job,
		// "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}

	@Test
	public void pullParJobTest() throws Exception {
		Job job = createJob(Flow.PAR, Access.PULL);
		job = exert(job);
		// logger.info("job j1: " + job);
		// logger.info("job j1 job context: " + context(job));
		logger.info("job j1 job context: " + serviceContext(job));
		// logger.info("job j1 value @ j1/t3/result/y = " + get(job,
		// "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}

	public void pullSeqJobTest() throws Exception {
		Job job = createJob(Flow.SEQ, Access.PULL);
		job = exert(job);
		// logger.info("job j1: " + job);
		// logger.info("job j1 job context: " + context(job));
		logger.info("job j1 job context: " + serviceContext(job));
		// logger.info("job j1 value @ j1/t3/result/y = " + get(job,
		// "j1/t3/result/y"));
		assertEquals(get(job, "j1/t3/result/y"), 400.00);
	}

	// two level job composition with PULL and PAR execution
	private Job createJob(Flow flow, Access access) throws Exception {
		Task t3 = task(
				"t3",
				sig("subtract", Subtractor.class),
				context("subtract", inEnt("arg/x1", null), inEnt("arg/x2", null),
						outEnt("result/y", null)));
		Task t4 = task("t4",
				sig("multiply", Multiplier.class),
				context("multiply", inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
						outEnt("result/y", null)));
		Task t5 = task("t5",
				sig("add", Adder.class),
				context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0),
						outEnt("result/y", null)));

		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
		Job j1 = job("j1", // sig("service", Jobber.class),
				job("j2", t4, t5, strategy(flow, access)), t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));

		return j1;
	}

}
