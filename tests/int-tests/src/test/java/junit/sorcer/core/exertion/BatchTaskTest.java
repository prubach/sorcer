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
package junit.sorcer.core.exertion;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.from;
import static sorcer.co.operator.inEnt;
import static sorcer.eo.operator.*;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.pars;

import java.rmi.RMISecurityManager;

import junit.sorcer.core.provider.AdderImpl;
import junit.sorcer.core.provider.MultiplierImpl;
import junit.sorcer.core.provider.SubtractorImpl;

import org.junit.Ignore;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.service.Signature;
import sorcer.service.Signature.Direction;
import sorcer.service.Task;


/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerRunner.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
//        "org.sorcersoft.sorcer:ju-arithmetic-api"
//})
public class BatchTaskTest {

    @Test
    public void batchTask1aTest() throws Exception {
        // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
        // testing with getValueEndsWith for vars in the context with prefixed paths
        Task batch1 = task(
                "batch1",
                type(sig(invoker("x1 * x2", pars("x1", "x2")), result("x5")), Signature.PRE),
                type(sig(invoker("x3 + x4", pars("x3","x4")), result("x6")), Signature.PRE),
                type(sig(invoker("x5 - x6", pars("x5", "x6")), result("result/y")), Signature.SRV),
                context(inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
                        inEnt("arg/x3", 20.0), inEnt("arg/x4", 80.0)));

        //logger.info("task batch1: " + batch1.getClass());

        //logger.info("task t: " + value(batch1));
        assertEquals("Wrong value for 400.0", 400.0, value(batch1));
    }

    @Test
    public void batchTask1bTest() throws Exception {
        // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
        // testing with getValueEndsWith for vars in the context with prefixed paths
        Task batch1 = task(
                "batch1",
                sig(invoker("x1 * x2", pars("x1", "x2")), result("x5")),
                sig(invoker("x3 + x4", pars("x3","x4")), result("x6")),
                sig(invoker("x5 - x6", pars("x5", "x6")), result("result/y")),
                context(inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
                        inEnt("arg/x3", 20.0), inEnt("arg/x4", 80.0)));

        //logger.info("task t: " + value(batch1));
        assertEquals("Wrong value for 400.0", 400.0, value(batch1));
    }

    @Test
    public void batchTask2Test() throws Exception {
        // PREPROCESS, POSTPROCESS with SERVICE signatures and with invoker, evaluation tasks
        Task batch2 = task(
                "batch2",
                type(sig(invoker("x1 * x2", pars("x1", "x2")), result("x5")), Signature.PRE),
                type(sig(invoker("x4 + x3", pars("x3","x4")), result("x6")), Signature.PRE),
                sig(invoker("x5 - x6", pars("x5", "x6")), result("result/y", Direction.IN)),
                type(sig("add", AdderImpl.class, result("result/z")), Signature.POST),
                context(inEnt("arg/x1", 10.0), inEnt("arg/x2", 50.0),
                        inEnt("arg/x3", 20.0), inEnt("arg/x4", 80.0)));

        batch2 = exert(batch2);
        //logger.info("task result/y: " + get(batch2, "result/y"));
        assertEquals("Wrong value for 400.0", 400.0, get(batch2, "result/y"));

        // sums up all inputs and the return value of y: [400.0, 80.0, 20.0, 50.0, 10.0]]
        //logger.info("task result/z: " + get(batch2, "result/z"));
        assertEquals("Wrong value for 560.0", 560.0, get(batch2, "result/z"));
    }

    @Test
	public void batchTask3Test() throws Exception {
		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
		// shared dataContext with named paths
		Task batch3 = task("batch3",
				type(sig("multiply", MultiplierImpl.class, result("subtract/x1", Direction.IN)), Signature.PRE),
				type(sig("add", AdderImpl.class, result("subtract/x2", Direction.IN)), Signature.PRE),
				sig("subtract", SubtractorImpl.class, result("result/y", from("subtract/x1", "subtract/x2"))),
				context(inEnt("multiply/x1", 10.0), inEnt("multiply/x2", 50.0), 
						inEnt("add/x1", 20.0), inEnt("add/x2", 80.0)));
		
		batch3 = exert(batch3);
		//logger.info("task result/y: " + get(batch3, "result/y"));
		assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
	}
	
	
	@Test
	public void batchTask4Test() throws Exception {
		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
		// shared dataContext with prefixed paths
		Task batch3 = task("batch3",
				type(sig("multiply#op1", MultiplierImpl.class, result("op3/x1", Direction.IN)), Signature.PRE),
				type(sig("add#op2", AdderImpl.class, result("op3/x2", Direction.IN)), Signature.PRE),
				sig("subtract", SubtractorImpl.class, result("result/y", from("op3/x1", "op3/x2"))),
				context(inEnt("op1/x1", 10.0), inEnt("op1/x2", 50.0), 
						inEnt("op2/x1", 20.0), inEnt("op2/x2", 80.0)));
		
		batch3 = exert(batch3);
		//logger.info("task result/y: " + get(batch3, "result/y"));
		assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
	}
}
	
