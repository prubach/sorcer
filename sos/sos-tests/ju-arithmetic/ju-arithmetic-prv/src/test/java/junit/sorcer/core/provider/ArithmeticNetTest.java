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

import sorcer.core.SorcerConstants;
import sorcer.service.Job;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Monitor;
import sorcer.service.Strategy.Wait;
import sorcer.service.Task;
import sorcer.util.ProviderAccessor;
import sorcer.util.Sorcer;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.input;
import static sorcer.eo.operator.job;
import static sorcer.eo.operator.jobContext;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.output;
import static sorcer.eo.operator.pipe;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.strategy;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ArithmeticNetTest implements SorcerConstants {
	private final static Logger logger = Logger
			.getLogger(ArithmeticNetTest.class.getName());

	static {
		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
				+ "/configs/sorcer.policy");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBaseByArtifacts(new String[]{
				"org.sorcersoft.sorcer:sos-platform",
				"org.sorcersoft.sorcer:ju-arithmetic-api"});
		System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
		System.out.println("Webster:" + Sorcer.getWebsterUrl());
		System.out.println("Codebase:" + System.getProperty("java.rmi.server.codebase"));
	}

	public static void waitForServices() throws InterruptedException {
		int tries = 0;
		while (tries < 8) {
			Object subtractor = ProviderAccessor.getService(null, Subtractor.class);
			if (subtractor != null)
				return;
			Thread.sleep(1000);
			tries++;
		}
	}

	public void arithmeticProviderExertTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg/x1", 20.0),
						in("arg/x2", 80.0)));

		t5 = exert(t5);
		logger.info("t5 dataContext: " + context(t5));
		assertEquals("Wrong value for 100.0", 100.0, value(context(t5), "result/value"));
	}

	public void arithmeticProviderGetTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg/x1", 20.0),
						in("arg/x2", 80.0), result("result/y")));
		t5 = exert(t5);
		assertEquals("Wrong value for 100.0", 100.0, get(t5));
	}

	public void arithmeticProviderValueTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg/x1", 20.0),
						in("arg/x2", 80.0), result("result/y")));

		assertEquals("Wrong value for 100.0", 100.0, value(t5));
	}

	public void arithmeticSpaceTaskTest() throws Exception {
		Task t5 = task(
				"t5",
				sig("add", Adder.class),
				context("add", in("arg/x1", 20.0),
						in("arg/x2", 80.0), out("result/y")),
				strategy(Access.PULL, Wait.YES));

		t5 = exert(t5);
//		logger.info("t5 dataContext: " + dataContext(t5));
//		logger.info("t5 value: " + get(t5, "result/y"));
		assertEquals("Wrong value for 100.0", 100.0, get(t5, "result/y"));
	}

	public void exertJobPushParTest() throws Exception {
		Job job = createJob(Flow.PAR, Access.PUSH);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}

	public void exertJobPushSeqTest() throws Exception {
		Job job = createJob(Flow.SEQ, Access.PUSH);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}

	public void exertJobPullParTest() throws Exception {
		Job job = createJob(Flow.PAR, Access.PULL);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}

	public void exertJobPullSeqTest() throws Exception {
		Job job = createJob(Flow.SEQ, Access.PULL);
		job = exert(job);
		//logger.info("job j1: " + job);
		//logger.info("job j1 job dataContext: " + dataContext(job));
		logger.info("job j1 job dataContext: " + jobContext(job));
		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
		assertEquals(400.00, get(job, "j1/t3/result/y"));
	}

	// two level job composition with PULL and PAR execution
	private Job createJob(Flow flow, Access access) throws Exception {
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

		return job("j1", //sig("service", RemoteJobber.class),
				//job("j2", t4, t5),
				job("j2", t4, t5, strategy(flow, access)),
				t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
	}

	public void asyncTaskTest() throws Exception {
		//TODO get result from monitor
		task("f5",
				sig("add", Adder.class),
				context("add", input("arg/x1", 20.0), input("arg/x2", 80.0),
						output("result/y", null)), strategy(Monitor.YES, Wait.NO));
	}

	public static void main(String[] args) throws Exception {
		waitForServices();
		ArithmeticNetTest ant = new ArithmeticNetTest();
		ant.arithmeticProviderExertTest();
		ant.arithmeticProviderGetTest();
		ant.arithmeticProviderValueTest();
		ant.arithmeticSpaceTaskTest();
		ant.asyncTaskTest();
		ant.exertJobPullParTest();
		ant.exertJobPullSeqTest();
		ant.exertJobPushParTest();
		ant.exertJobPushSeqTest();
	}
}
