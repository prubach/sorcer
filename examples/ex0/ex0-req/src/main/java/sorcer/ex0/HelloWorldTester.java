/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.ex0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.service.*;

import java.rmi.RMISecurityManager;

import static sorcer.eo.operator.*;

public class HelloWorldTester extends ServiceRequestor {

    private static Logger logger = LoggerFactory.getLogger(HelloWorldTester.class);

    public Exertion getExertion(String... args) throws ExertionException, ContextException, SignatureException {
        System.setSecurityManager(new RMISecurityManager());
        logger.info("Starting HelloWorldTester");

        Task t1 = task("hello", sig("sayHelloWorld", HelloWorld.class),
                context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);

        logger.info("Got result: {}", get(out, "out/value"));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: " + trace(out));
        return out;
    }
}



