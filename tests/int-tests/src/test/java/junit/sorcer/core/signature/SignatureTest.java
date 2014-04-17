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
package junit.sorcer.core.signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.eo.operator.*;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.par;
import static sorcer.po.operator.pars;

import java.util.logging.Logger;

import junit.sorcer.core.provider.AdderImpl;

import org.junit.Test;

import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.core.context.model.par.Par;
import sorcer.core.invoker.GroovyInvoker;
import sorcer.core.provider.Jobber;
import sorcer.junit.SorcerRunner;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.Signature;
import sorcer.service.SignatureException;

/**
 * @author Mike Sobolewski
 */

@RunWith(SorcerRunner.class)
public class SignatureTest {
	private final static Logger logger = Logger
			.getLogger(SignatureTest.class.getName());

	@Test
	public void providerTest() throws ExertionException, ContextException, SignatureException {
		
		Signature s1 = sig("add", new AdderImpl());
		//logger.info("provider of s1: " + provider(s1));
		assertTrue(provider(s1) instanceof  AdderImpl);
		
		Signature s2 = sig("add", AdderImpl.class);
		//logger.info("provider of s2: " + provider(s2));
		assertTrue(provider(s2) instanceof  AdderImpl);

		Signature s4 = sig(invoker("new Date()"));
		//logger.info("provider of s4: " + provider(s4));
		assertTrue(provider(s4) instanceof GroovyInvoker);
		
		Signature s6 = sig(par("x3", invoker("x3-e", "x1 - x2", pars("x1", "x2"))));
		logger.info("provider of s6: " + provider(s6));
		assertTrue(provider(s6) instanceof Par);

	}


    // netProviderTest moved to examples/ex5/ex5-req
/*
    @Ignore
	@Test
	public void netProviderTest() throws SignatureException  {
		Signature s3 = sig("add", Adder.class);
		logger.info("provider of s3: " + provider(s3));
		assertTrue(provider(s3) instanceof Adder);
	}
*/
	@Test
	public void deploySigTest() throws SignatureException  {
		Signature deploySig = sig("service", Jobber.class, "Jobber", deploy(idle(1)));
		logger.info("deploySig: " + deploySig);
		assertEquals(deploySig.getProviderName(), SorcerEnv.getActualName("Jobber"));
		assertEquals(deploySig.getSelector(), "service");
	}
	
}
