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
package junit.sorcer.core.context;

import org.junit.Test;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;

import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.entry;
import static sorcer.co.operator.map;
import static sorcer.eo.operator.*;

/**
 * @author Mike Sobolewski
 */

public class ContextTest {
	private final static Logger logger = Logger
			.getLogger(ContextTest.class.getName());

	
	@SuppressWarnings("unchecked")
	@Test
	public void contextCreationTest() throws ExertionException, ContextException {
		Map<String, Double> m = map(entry("k1", 1.0), entry("k2", 2.0));
		//logger.info("map m:  " + m);
		assertTrue("Wrong value for k1=1.0", m.get("k1").equals(1.0));

		Context<?> cxt = context(in("k1", 1.0), in("k2", 2.0), in("k3", 3.0), out("k4", 4.0));
		logger.info("in/out dataContext: " + cxt);
		assertEquals(1.0, get(cxt, "k1"));
		assertEquals(2.0, get(cxt, "k2"));
		assertEquals(3.0, get(cxt, "k3"));
		assertEquals(4.0, get(cxt, "k4"));
		assertEquals(1.0, get(cxt, 1));
		assertEquals(2.0, get(cxt, 2));
		assertEquals(3.0, get(cxt, 3));
		assertEquals(4.0, get(cxt, 4));
		
//		assertEquals(4, ((PositionalContext)cxt).getTally());
//		//logger.info("tally: " + ((PositionalContext)cxt).getTally());
//		put(cxt, entry("k4", var("x1", 50.0)));
//		logger.info("tally after k4: " + ((PositionalContext)cxt).getTally());
//		assertEquals(4, ((PositionalContext)cxt).getTally());
//		logger.info("value k4: " + get(cxt, "k4"));
//		assertEquals(50.0, revalue(cxt, "k4"));
//		assertEquals(name(get(cxt, "k4")), "x1");
		
//		put(cxt, entry("k5", var("x2", 100.0)));
//		logger.info("tally after k5: " + ((PositionalContext)cxt).getTally());
//		assertEquals(5, ((PositionalContext)cxt).getTally());
//		logger.info("value k5: " + get(cxt, "k5"));
//		assertEquals(100.0, revalue(cxt, "k5"));
	
		cxt = context(entry("k1", 1.0), entry("k2", 2.0), entry("k3", 3.0));
		logger.info("dataContext cxt:  " + cxt.getClass());
		//logger.info("entry dataContext cxt:  " + cxt);
		assertEquals(2.0, get(cxt, "k2"));
		assertEquals(3.0, get(cxt, "k3"));
	}
	
//	@Test
//	public void contextClosureTest() throws ExertionException, ContextException, RemoteException {
//		Context<?> cxt = dataContext(in("x1"), in("x2"),
//				in(var("y", evaluators(expr("e1", "x1 * x2", vars("x1", "x2"))))));
//		revaluable(cxt);
//		//logger.info("cxt value:  " + value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
//		assertEquals(500.0, value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
//	}
	
//	@Test
//	public void evaluatedContextTest() throws ExertionException, ContextException {
//		Context<?> cxt = dataContext(in(var("x1")), in(var("x2")),
//				in(var("y", evaluators(expr("e1", "x1 * x2", vars("x1", "x2"))))));
//		revaluable(cxt);
//		logger.info("cxt: " + cxt);
//
//		//logger.info("cxt value:  " + value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
//		assertEquals(500.0, value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
//	}
	
//	@Test
//	public void evaluatedContextWithResultTest() throws ExertionException, ContextException {
//		Context<?> cxt = dataContext(in(var("x1")), in(var("x2")),
//				in(var("y", evaluators(expr("e1", "x1 * x2", vars("x1", "x2"))))),
//				result("y"));
//		logger.info("cxt: " + cxt);
//		logger.info("return path: " + cxt.getReturnPath());
//		revaluable(cxt);
//		logger.info("cxt2: " + cxt);
//		logger.info("cxt value:  " + value(cxt, entry("x1", 10.0), entry("x2", 50.0)));
//		
//		// No path for the evaluation is specified in the dataContext cxt
//		assertEquals(500.0, value(cxt, entry("x1", 10.0), entry("x2", 50.0)));
//	}
	
//	@Test
//	public void evaluateAcrossContextsTest() throws ExertionException, ContextException {
//		Context<?> cxt = dataContext(in(var("x1")), in(var("x2")),
//				in(var("y", evaluators(expr("e1", "x1 * x2", vars("x1", "x2"))))),
//				result("y"));
//		revaluable(cxt);
//		Context<?> cxt0 = dataContext(in(var("x11", 10.0)), in(var("x21", 50.0)));
////		logger.info("x11: " + value(var("x11", cxt0)));
////		logger.info("x21: " + value(var("x21", cxt0)));
//		
//		//logger.info("cxt value:  " + value(cxt, entry("x1", var("x11", cxt0)), entry("x2", var("x21", cxt0))));
//		assertEquals(500.0, value(cxt, entry("x1", var("x11", cxt0)), entry("x2", var("x21", cxt0))));
//	}
}