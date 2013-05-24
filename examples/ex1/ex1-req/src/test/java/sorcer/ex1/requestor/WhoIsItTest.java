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
package sorcer.ex1.requestor;

import org.junit.Test;
import sorcer.core.SorcerConstants;
import sorcer.util.Sorcer;

import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class WhoIsItTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(WhoIsItTest.class.getName());

	static {
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                + "/configs/sorcer.policy");
        System.setSecurityManager(new RMISecurityManager());
        Sorcer.setCodeBaseByArtifacts(new String[]{
                "org.sorcersoft.sorcer:sos-platform",
                "org.sorcersoft.sorcer:ex2-prv",
                "org.sorcersoft.sorcer:ex2-api"});
        System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
	}

	@Test
	public void exertTaskConcatenation() throws Exception {
        String hostname, ipAddress;
        InetAddress inetAddress = InetAddress.getLocalHost();
        hostname = inetAddress.getHostName();
        ipAddress = inetAddress.getHostAddress();

        Context context = new ServiceContext("Who Is It?");
        context.putValue("requestor/message", new RequestorMessage("SORCER"));
        context.putValue("requestor/hostname", hostname);
        context.putValue("requestor/address", ipAddress);

        Signature signature1 = new NetSignature("getHostAddress",
                sorcer.ex1.WhoIsIt.class, providername, Type.PRE);
        Signature signature2 = new NetSignature("getHostName",
                sorcer.ex1.WhoIsIt.class, providername, Type.SRV);
        Signature signature3 = new NetSignature("getCanonicalHostName",
                sorcer.ex1.WhoIsIt.class, providername, Type.POST);
        Signature signature4 = new NetSignature("getTimestamp",
                sorcer.ex1.WhoIsIt.class, providername, Type.POST);

        Task task = new NetTask("Who Is It?",
                new Signature[] { signature1, signature2, signature3, signature4 },
                context);

        Task result = task.exert();
        logger.info("task context: " + result.getContext());
        //assertEquals(result.getContext().getValue("3tasks/subtract/result/value"), 400.0);
    }


}
