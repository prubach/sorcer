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
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import net.jini.core.transaction.TransactionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.ObjectSignature;
import sorcer.ex2.provider.InvalidWork;
import sorcer.ex2.provider.Work;
import sorcer.ex2.provider.WorkerProvider;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;

import com.gargoylesoftware.base.testing.TestUtil;

/**
 * @author Mike Sobolewski
 *
 */
public class WorkerTaskRequestorTest {
    private Context context;
    private String hostname;

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
        hostname = SorcerEnv.getHostName();

        Work work = new MyWork();

        context = new ServiceContext("work");
        context.putValue("requestor/name", hostname);
        context.putValue("requestor/operand/1", 11);
        context.putValue("requestor/operand/2", 101);
        context.putValue("requestor/work", work);
        context.putValue("to/provider/name", "Testing Provider");
    }

    @Test
    @Ignore("MyWork hashcode is different after deserialization")
    public void contextSerializationTest() throws IOException {
        // test serialization of the requestor's context
        TestUtil.testSerialization(context, true);
    }

    @Test
    public void providerResultTest() throws RemoteException, ContextException, TransactionException,
            ExertionException, UnknownHostException, SignatureException {

        ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);

        Exertion task = new ObjectTask("work", signature, context);
        task = task.exert();
        //logger.info("result: " + task);
        assertEquals(1111, task.getContext().getValue("provider/result"));
    }

    @Test
    public void providerMessageTest() throws RemoteException, ContextException, TransactionException,
            ExertionException, UnknownHostException, SignatureException {

        ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);

        Exertion task = new ObjectTask("work", signature, context);
        task = task.exert();
        //logger.info("result: " + task);
        assertEquals("Done work by: " + WorkerProvider.class, task.getContext().getValue("provider/message"));
    }

    @Test
    public void providerHostNameTest() throws RemoteException, ContextException, TransactionException,
            ExertionException, UnknownHostException, SignatureException {

        ObjectSignature signature = new ObjectSignature("doWork", WorkerProvider.class);
        Exertion task = new ObjectTask("work", signature, context);
        task = task.exert();
        //logger.info("result: " + task);
        assertEquals(hostname, task.getContext().getValue("provider/host/name"));
    }
}
