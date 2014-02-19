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
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sorcer.boot.ServiceStarter;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import com.gargoylesoftware.base.testing.TestUtil;

/**
 * @author Mike Sobolewski
 *
 */
public class WorkerProviderTest implements Serializable {
	String hostName;
	Context context;
	WorkerProvider provider;
	
    private static class MyWork implements Work {
        public Context exec(Context cxt) throws InvalidWork, ContextException {
            int arg1 = (Integer)cxt.getValue("requestor/operand/1");
            int arg2 = (Integer)cxt.getValue("requestor/operand/2");
            cxt.putOutValue("provider/result", arg1 * arg2);
            return cxt;
        }
    }

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		hostName = SorcerEnv.getHostName();
		provider = new WorkerProvider();

        Work work = new MyWork();
        context = new ServiceContext("work");
        context.putValue("requestor/name", hostName);
        context.putValue("requestor/operand/1", 11);
        context.putValue("requestor/operand/2", 21);
        context.putValue("requestor/work", work);
        context.putValue("to/provider/name", "Testing Provider");
	}

	@Test
    @Ignore("MyWork hashcode is different after deserialization")
	public void contextTest() throws IOException,
			IllegalAccessException, InvocationTargetException {
		// test serialization of the provider's context
		TestUtil.testSerialization(context, true);

		// test serialization of the provider's context
		//TestUtil.testClone(context, true);
	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#sayHi(sorcer.service.Context)}.
	 * @throws IOException 
	 */
	@Test
	public void testSayHi() throws ContextException, IOException {
		Context result = provider.sayHi(context);
		//logger.info("result: " + result);
		// test serialization of the returned context
		//TestUtil.testSerialization(result, true);
		assertEquals("Hi " + hostName + "!", result.getValue("provider/message"));
	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#sayBye(sorcer.service.Context)}.
	 */
	@Test
	public void testSayBye() throws RemoteException, ContextException {
		Context result = provider.sayBye(context);
		//logger.info("result: " + result);
        assertEquals("Bye " + hostName + "!", result.getValue("provider/message"));

	}

	/**
	 * Test method for {@link sorcer.ex2.provider.WorkerProvider#doWork(sorcer.service.Context)}.
	 */
	@Test
	public void testDoIt() throws RemoteException, InvalidWork, ContextException {
		Context result = provider.doWork(context);
		//logger.info("result: " + result);
        assertEquals(231, result.getValue("provider/result"));
	}

}
