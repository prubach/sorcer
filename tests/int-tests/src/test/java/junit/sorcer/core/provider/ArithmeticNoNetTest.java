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

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.from;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.input;

import java.util.logging.Logger;

import org.junit.Test;

import org.junit.runner.RunWith;
import sorcer.core.provider.rendezvous.ServiceJobber;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerRunner;
import sorcer.service.*;


/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerRunner.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ju-arithmetic-dl:pom"
})
public class ArithmeticNoNetTest {

	private final static Logger logger = Logger
			.getLogger(ArithmeticNoNetTest.class.getName());

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
		assertEquals(100.0, get(srv, "j1/t3/arg/x2"));
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
		return srv("j1", sig("execute", ServiceJobber.class),
					cxt(in("arg/x1", 10.0), result("job/result", from("j1/t3/result/y"))),
				srv("j2", sig("execute", ServiceJobber.class), t4, t5),
				t3,
				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
	}

    // TODO - Problem with context value mappings
    //@Ignore
    @Test
    public void contexterTest() throws Exception {
        Task cxtt = task("addContext", sig("getContext", createContext()),
                context("add", input("arg/x1"), input("arg/x2")));

        Context result = context(exert(cxtt));
//		logger.info("contexter context: " + result);
        assertEquals(get(result, "arg/x1"), 20.0);
        assertEquals(get(result, "arg/x2"), 80.0);

    }

    // TODO - Problem with context value mappings
    //@Ignore
    @Test
    public void objectContexterTaskTest() throws Exception {
        Task t5 = task("t5", sig("add", AdderImpl.class),
                type(sig("getContext", createContext()), Signature.APD),
                context("add", in("arg/x1"), in("arg/x2"),
                        result("result/y")));

        Context result = context(exert(t5));
//		logger.info("task context: " + result);
        assertEquals(get(result, "result/y"), 100.0);
    }

    public static Context createContext() throws Exception {
        Context cxt = context("add", input("arg/x1", 20.0), input("arg/x2", 80.0));
        return  cxt;
    }

}
