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
package sorcer.ex6.requestor;

import org.junit.Test;
import sorcer.core.SorcerConstants;
import sorcer.core.context.PositionalContext;
import sorcer.core.exertion.ObjectJob;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.ObjectSignature;
import sorcer.ex5.provider.AdderImpl;
import sorcer.ex5.provider.MultiplierImpl;
import sorcer.ex5.provider.SubtractorImpl;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.util.Sorcer;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ObjectArithmeticReqTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(ObjectArithmeticReqTest.class.getName());

	static {
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                + "/configs/sorcer.policy");
        System.setSecurityManager(new RMISecurityManager());
        Sorcer.setCodeBaseByArtifacts(new String[]{
                "org.sorcersoft.sorcer:sos-platform",
                "org.sorcersoft.sorcer:ex6-prv",
                "org.sorcersoft.sorcer:ex6-api"});
        System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
	}

    @Test
    public void exertImpl() throws Exception {
        Task f5 = task(
                "f5",
                sig("add", AdderImpl.class),
                context("add", in("arg/x1", 20.0),
                        in("arg/x2", 80.0), out("result/y", null)));

        Exertion out = null;
        long start = System.currentTimeMillis();
        out = exert(f5);
        long end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end-start) + " ms.");
        logger.info("task f5 dataContext: " + context(out));
        logger.info("task f5 result/y: " + get(context(out), "result/y"));

        assertEquals(get(context(out), 100.0);
    }

}
