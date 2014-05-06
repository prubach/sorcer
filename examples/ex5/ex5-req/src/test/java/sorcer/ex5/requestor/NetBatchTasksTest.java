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
package sorcer.ex5.requestor;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.ex5.provider.Adder;
import sorcer.ex5.provider.Multiplier;
import sorcer.ex5.provider.Subtractor;
import sorcer.junit.*;
import sorcer.service.Direction;
import sorcer.service.Signature;
import sorcer.service.Task;


import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.from;
import static sorcer.eo.operator.*;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes" })
@Category(SorcerClient.class)
@RunWith(SorcerSuite.class)
//@SorcerServiceConfiguration(":ex6-cfg-all")
@SorcerServiceConfiguration({
        ":ex5-cfg-adder",
        ":ex5-cfg-multiplier",
        ":ex5-cfg-subtractor"
})

@ExportCodebase({
        "org.sorcersoft.sorcer:ex5-api",
        "org.sorcersoft.sorcer:sorcer-api"
})
public class NetBatchTasksTest {

	private final static Logger logger = Logger
			.getLogger(NetBatchTasksTest.class.getName());

    //@Ignore("hangs")
    // This type of tasks only work if there is a separate provider for the SRV signature and others for other sigs
    @Test
    public void batchTaskTest() throws Exception {
        // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
        // shared context with named paths
        Task batch3 = task("batch3",
                type(sig("multiply", Multiplier.class, result("subtract/x1", Direction.IN)), Signature.PRE),
                type(sig("add", Adder.class, result("subtract/x2", Direction.IN)), Signature.PRE),
                sig("subtract", Subtractor.class, result("result/y", from("subtract/x1", "subtract/x2"))),
                context(in("multiply/x1", 10.0), in("multiply/x2", 50.0),
                        in("add/x1", 20.0), in("add/x2", 80.0)));

        batch3 = exert(batch3);
        //logger.info("task result/y: " + get(batch3, "result/y"));
        assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
    }

    //@Ignore("hangs")
    // This type of tasks only work if there is a separate provider for the SRV signature and others for other sigs
    @Test
    public void batchPrefixedTaskTest() throws Exception {
        // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
        // shared context with prefixed paths
        Task batch3 = task("batch3",
                type(sig("multiply#op1", Multiplier.class, result("op3/x1", Direction.IN)), Signature.PRE),
                type(sig("add#op2", Adder.class, result("op3/x2", Direction.IN)), Signature.PRE),
                sig("subtract", Subtractor.class, result("result/y", from("op3/x1", "op3/x2"))),
                context(in("op1/x1", 10.0), in("op1/x2", 50.0),
                        in("op2/x1", 20.0), in("op2/x2", 80.0)));

        batch3 = exert(batch3);
        //logger.info("task result/y: " + get(batch3, "result/y"));
        assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
    }
}
