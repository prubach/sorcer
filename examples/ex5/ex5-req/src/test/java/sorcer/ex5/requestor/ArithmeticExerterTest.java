/**
 *
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
package sorcer.ex5.requestor;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.ex5.provider.Adder;
import sorcer.junit.*;
import sorcer.service.*;
import sorcer.core.provider.Exerter;

import static org.junit.Assert.assertEquals;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.get;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
@RunWith(SorcerSuite.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex5-api"
})
@SorcerServiceConfigurations({
        @SorcerServiceConfiguration({
                ":ex5-cfg-adder",
                ":ex5-cfg-multiplier",
                ":ex5-cfg-subtractor",
                ":ex5-cfg-divider",
                ":ex5-job"
        }),
        @SorcerServiceConfiguration({
                ":ex5-cfg-all",
                ":ex5-job"
        }),
        @SorcerServiceConfiguration({
                ":ex5-cfg-one-bean",
                ":ex5-job"
        })
})
public class ArithmeticExerterTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ArithmeticExerterTest.class);

	@Test
	public void exertExerter() throws Exception {
		Job exertion = NetArithmeticReqTest.getJobComposition();
		Task task = new NetTask("exert", new NetSignature("exert",
				Exerter.class),
				new ServiceContext(exertion));
		Task result = (Task) task.exert();
		// logger.info("result: " + result);
		// logger.info("return value: " + result.getReturnValue());
	
		Context out = result.getContext();
//		logger.info("out context: " + out);
		logger.info("1job1task/subtract/result/value: "
				+ out.getValue(
						"1job1task/subtract/result/value"));
		assertEquals(
				out.getValue("1job1task/subtract/result/value"),
				400.0);
	}

    @Test
    public void exerterTest() throws Exception {
        System.out.println("========== exerterTest ==========");
        Task f5 = task(
                "f5",
                sig("add", Adder.class),
                context("add", in("arg/x1", 20.0),
                        in("arg/x2", 80.0), out("result/y", null)),
                strategy(Strategy.Monitor.NO, Strategy.Wait.YES));

        Exertion out = null;
        Exerter exerter = (Exerter) Accessor.getService(new NetSignature(Exerter.class));
    	logger.info("got exerter: " + exerter);

        out = exerter.exert(f5);

        logger.info("task f5 context: " + context(out));
        logger.info("task f5 result/y: " + get(context(out), "result/y"));
        assertEquals(get(out, "result/y"), 100.00);
    }
}
