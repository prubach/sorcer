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
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Exerter;
import sorcer.service.Job;
import sorcer.service.Task;

import static org.junit.Assert.assertEquals;

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
}
