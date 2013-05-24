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
package sorcer.ex2.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.Log;

/**
 * @author Mike Sobolewski
 */
public class WorkerProviderTest {
	private static Logger logger = Log.getTestLog();
	
	String hostName;
	Context context;
	WorkerProvider provider;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		hostName = SorcerEnv.getLocalHost().getHostName();
		provider = new WorkerProvider();

        Work work = new Work() {
            public Context exec(Context cxt) throws InvalidWork, ContextException {
                int arg1 = (Integer)cxt.getValue("requestor/operand/1");
                int arg2 = (Integer)cxt.getValue("requestor/operand/2");
                cxt.putOutValue("provider/result", arg1 * arg2);
                return cxt;
            }
        };

		context = new ServiceContext("work");
		context.putValue("requestor/name", hostName);
		context.putValue("requestor/operand/1", 11);
		context.putValue("requestor/operand/2", 21);
        context.putValue("requestor/work", work);
        context.putValue("to/provider/name", "Testing Provider");
	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#sayHi(sorcer.service.Context)}.
	 * @throws IOException 
	 */
	@Test
	public void testSayHi() throws ContextException, IOException {
		Context result = provider.sayHi(context);
		//logger.info("result: " + result);
		// test serialization of the returned dataContext
		//TestUtil.testSerialization(result, true);
		assertTrue(result.getValue("provider/message").equals("Hi " + hostName + "!"));
	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#sayBye(sorcer.service.Context)}.
	 */
	@Test
	public void testSayBye() throws RemoteException, ContextException {
		Context result = provider.sayBye(context);
		//logger.info("result: " + result);
		assertEquals(result.getValue("provider/message"), "Bye " + hostName + "!");

	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#doWork(sorcer.service.Context)}.
	 */
	@Test
	public void testDoIt() throws RemoteException, InvalidWork, ContextException {
		Context result = provider.doWork(context);
		//logger.info("result: " + result);
		assertEquals(result.getValue("provider/result"), 231);
	}

}
