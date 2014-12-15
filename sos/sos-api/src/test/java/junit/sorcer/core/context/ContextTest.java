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
import sorcer.core.context.ContextLink;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;

import java.rmi.RemoteException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.co.operator.entry;
import static sorcer.co.operator.map;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.value;
import static sorcer.po.operator.in;
import static sorcer.po.operator.*;

/**
 * @author Mike Sobolewski
 */

public class ContextTest {
	private final static Logger logger = LoggerFactory
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

    @Test
    public void contextClosureTest() throws ExertionException, ContextException, RemoteException {
        Context<?> cxt = context(in("x1"), in("x2"),
                in(par("y", invoker("e1", "x1 * x2", pars("x1", "x2")))));
        model(cxt);

//		logger.info("x1 value: " + value(cxt, "x1", entry("x1", 10.0), entry("x2", 50.0)));
//		logger.info("x2 value: " + value(cxt, "x2"));
//		logger.info("y value: " + value(cxt, "y"));

        logger.info("cxt value:  " + value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
        assertEquals(500.0, value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
    }

    @Test
    public void evaluatedContextTest() throws ExertionException, ContextException {
        Context<?> cxt = context(in(par("x1")), in(par("x2")),
                in(par("y", invoker("e1", "x1 * x2", pars("x1", "x2")))));
        model(cxt);
//		logger.info("cxt: " + cxt);

        //logger.info("cxt value:  " + value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
        assertEquals(500.0, value(cxt, "y", entry("x1", 10.0), entry("x2", 50.0)));
    }

    @Test
    public void evaluatedContextWithResultTest() throws ExertionException, ContextException {
        Context<?> cxt = context(in(par("x1")), in(par("x2")),
                in(par("y", invoker("e1", "x1 * x2", pars("x1", "x2")))),
                result("y"));
		logger.info("cxt: " + cxt);
		logger.info("return path: " + cxt.getReturnPath());
        model(cxt);
//		logger.info("cxt2: " + cxt);
//		logger.info("cxt value:  " + value(cxt, entry("x1", 10.0), entry("x2", 50.0)));

        // No path for the evaluation is specified in the context cxt
        assertEquals(500.0, value(cxt, entry("x1", 10.0), entry("x2", 50.0)));
    }

    @Test
    public void evaluateAcrossContextsTest() throws ExertionException, ContextException {
        Context<?> cxt = context(in(par("x1")), in(par("x2")),
                in(par("y", invoker("e1", "x1 * x2", pars("x1", "x2")))),
                result("y"));
        model(cxt);
        Context<?> cxt0 = context(in(par("x11", 10.0)), in(par("x21", 50.0)));
        logger.info("x11: " + value(cxt0, "x11"));
        logger.info("x21: " + value(cxt0,"x21"));

//		logger.info("cxt value:  " + value(cxt, entry("x1", value(cxt0, "x11")), entry("x2", value(cxt0,"x21"))));
        assertEquals(500.0, value(cxt, entry("x1", value(cxt0, "x11")), entry("x2", value(cxt0,"x21"))));
    }

    @Test
    public void linkedContext() throws Exception {
        Context addContext = new PositionalContext("add");
        addContext.putInValue("arg1/value", 90.0);
        addContext.putInValue("arg2/value", 110.0);

        Context multiplyContext = new PositionalContext("multiply");
        multiplyContext.putInValue("arg1/value", 10.0);
        multiplyContext.putInValue("arg2/value", 70.0);

        ServiceContext invokeContext = new ServiceContext("invoke");
//		add additional tests with offset
//		invokeContext.putLink("add", addContext, "offset");
//		invokeContext.putLink("multiply", multiplyContext, "offset");

        invokeContext.putLink("add", addContext);
        invokeContext.putLink("multiply", multiplyContext);

        ContextLink addLink = (ContextLink)invokeContext.getLink("add");
        ContextLink multiplyLink = (ContextLink)invokeContext.getLink("multiply");

//		logger.info("invoke context: " + invokeContext);

//		logger.info("path arg1/value: " + addLink.getContext().getValue("arg1/value"));
        assertEquals(90.0, addLink.getContext().getValue("arg1/value"));
//		logger.info("path arg2/value: " + multiplyLink.getContext().getValue("arg2/value"));
        assertEquals(70.0, multiplyLink.getContext().getValue("arg2/value"));
//		logger.info("path add/arg1/value: " + invokeContext.getValue("add/arg1/value"));
        assertEquals(90.0, invokeContext.getValue("add/arg1/value"));
//		logger.info("path multiply/arg2/value: " + invokeContext.getValue("multiply/arg2/value"));
        assertEquals(70.0, invokeContext.getValue("multiply/arg2/value"));

    }

    @Test
    public void weakValueTest() throws Exception {
        Context cxt = context("add", in("arg/x1", 20.0), in("arg/x2", 80.0));

//		logger.info("arg/x1 = " + cxt.getValue("arg/x1"));
        assertEquals(20.0, cxt.getValue("arg/x1"));
//		logger.info("val x1 = " + cxt.getValue("x1"));
        assertEquals(null, cxt.getValue("x1"));
//		logger.info("weak x1 = " + cxt.getSoftValue("arg/var/x1"));
        assertEquals(20.0, cxt.getSoftValue("arg/var/x1"));
    }

}
