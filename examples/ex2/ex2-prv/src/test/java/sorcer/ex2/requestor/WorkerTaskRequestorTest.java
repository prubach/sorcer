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
package sorcer.ex2.requestor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.core.transaction.TransactionException;

import org.junit.Before;
import org.junit.Test;

import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.ObjectSignature;
import sorcer.ex2.provider.InvalidWork;
import sorcer.ex2.provider.Work;
import sorcer.ex2.provider.WorkerProvider;
import sorcer.org.rioproject.net.HostUtil;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.util.Log;

import com.gargoylesoftware.base.testing.TestUtil;

/**
 * @author Mike Sobolewski
 * 
 */
public class WorkerTaskRequestorTest {
	private static Logger logger = Log.getTestLog();
	
	private Context context;
	private String hostname;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		hostname = HostUtil.getInetAddress().getHostName();

        Work work = new Work() {
            public Context exec(Context cxt) throws InvalidWork, ContextException {
                int arg1 = (Integer)cxt.getValue("requestor/operand/1");
                int arg2 = (Integer)cxt.getValue("requestor/operand/2");
                cxt.putOutValue("provider/result", arg1 * arg2);
                return cxt;
            }
        };

        context = new ServiceContext("work");
        context.putValue("requestor/name", hostname);
        context.putValue("requestor/operand/1", 11);
        context.putValue("requestor/operand/2", 101);
        context.putValue("requestor/work", work);
        context.putValue("to/provider/name", "Testing Provider");
	}
	
	@Test
	public void providerResultTest() throws RemoteException, ContextException, TransactionException, 
		ExertionException, UnknownHostException, SignatureException {
		
		ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);

		Exertion task = new ObjectTask("doWork", signature, context);
		task = task.exert();
		logger.info("result: " + task);
		assertEquals((Integer)task.getDataContext().getValue("provider/result"), new Integer(1111));
	}
	
	@Test
	public void providerMessageTest() throws RemoteException, ContextException, TransactionException, 
		ExertionException, UnknownHostException, SignatureException {
		
		ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);

		Exertion task = new ObjectTask("doWork", signature, context);
		task = task.exert();
		logger.info("result: " + task);
		assertEquals(task.getDataContext().getValue("provider/message"),
                "Done work by: class sorcer.ex2.provider.WorkerProvider");
	}
	
	@Test
	public void providerHostNameTest() throws RemoteException, ContextException, TransactionException, 
		ExertionException, UnknownHostException, SignatureException {
		
		ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);
		Exertion task = new ObjectTask("doWork", signature, context);
		task = task.exert();
		logger.info("result: " + task);
		assertEquals(task.getDataContext().getValue("provider/host/name"), hostname);
	}
}
