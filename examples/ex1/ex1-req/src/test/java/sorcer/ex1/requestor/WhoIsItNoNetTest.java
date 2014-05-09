/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
package sorcer.ex1.requestor;

import org.junit.Test;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.ObjectSignature;
import sorcer.ex1.bean.WhoIsItBean1;
import sorcer.ex1.provider.WhoIsItProvider1;
import sorcer.junit.SorcerRunner;
import sorcer.service.*;

import java.net.InetAddress;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(SorcerRunner.class)
public class WhoIsItNoNetTest {

	private final static Logger logger = Logger
			.getLogger(WhoIsItNoNetTest.class.getName());

    @Test
    public void localhost() throws Exception {
        InetAddress inetAddress = SorcerEnv.getLocalHost();
        String hostname = inetAddress.getHostName();
        String ipAddress = inetAddress.getHostAddress();

        logger.info("inetAddress: " + inetAddress);
        logger.info("hostname: " + hostname);
        logger.info("ipAddress: " + ipAddress);
    }

    @Test
    public void helloObjectTask() throws Exception {
        InetAddress inetAddress = SorcerEnv.getLocalHost();
        String hostname = SorcerEnv.getHostName();
        String ipAddress = inetAddress.getHostAddress();

        Context context = new ServiceContext("Who Is It?");
        context.putValue("requestor/message", "Hello Objects!");
        context.putValue("requestor/hostname", hostname);
        context.putValue("requestor/address", ipAddress);

        ObjectSignature signature = new ObjectSignature("getHostName",
                WhoIsItBean1.class);

        Task task = new ObjectTask("Who Is It?", signature, context);
        Exertion result = task.exert();
        if (result.getExceptions().size() > 0)
            logger.info("exceptions: " + result.getExceptions());
        assertTrue(result.getExceptions().isEmpty());
        logger.info("task context: " + result.getContext());
        assertEquals(hostname, result.getContext().getValue("provider/hostname"));
    }

	@Test
	public void execBatchTask() throws Exception {
        InetAddress inetAddress = SorcerEnv.getLocalHost();
        String hostname = SorcerEnv.getHostName();
        String ipAddress = inetAddress.getHostAddress();

        Context context = new ServiceContext("Who Is It?");
        context.putValue("requestor/message", new RequestorMessage("Hello Objects!"));
        context.putValue("requestor/hostname", hostname);
        context.putValue("requestor/address", ipAddress);

        Signature signature1 = new ObjectSignature("getHostAddress", new WhoIsItProvider1(null, null), null, null);
        Signature signature2 = new ObjectSignature("getHostName", new WhoIsItProvider1(null, null), null, null);
        Signature signature3 = new ObjectSignature("getCanonicalHostName", new WhoIsItProvider1(null, null), null, null);
        Signature signature4 = new ObjectSignature("getTimestamp", new WhoIsItProvider1(null, null), null, null);

        Task task = new ObjectTask("Who Is It?", signature1, signature2, signature3, signature4);
        task.setContext(context);

        Exertion result = task.exert();
        logger.info("task context: " + result.getContext());
        assertEquals(hostname, result.getContext().getValue("provider/hostname"));
        assertEquals(ipAddress, result.getContext().getValue("provider/address"));
    }

}
