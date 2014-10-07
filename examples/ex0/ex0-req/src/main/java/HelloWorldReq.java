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

import sorcer.core.requestor.ServiceRequestor;
import sorcer.ex0.HelloWorld;
import sorcer.service.*;

import static sorcer.eo.operator.*;

public class HelloWorldReq {

    public static void main(String[] args) throws Exception {
        ServiceRequestor.prepareEnvironment();
        ServiceRequestor.prepareCodebase(new String[]{"org.sorcersoft.sorcer:ex0-dl:pom"});

        System.out.println("Starting HelloWorldRequestor");

        Task t1 = task("hello", sig("sayHelloWorld", HelloWorld.class),
                context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

        System.out.println("Task t1 prepared: " + t1);
        Exertion out = exert(t1);

        System.out.println("Got result: " + get(out, "out/value"));
        System.out.println("----------------------------------------------------------------");
        System.out.println("Task t1 trace: " + trace(out));
    }
}



