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
package sorcer.arithmetic.provider;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sorcer.service.Context;
import sorcer.service.ContextException;

import static sorcer.service.monitor.MonitorUtil.checkpoint;

public class AdderImpl implements Adder {
	private Arithmometer arithmometer = new Arithmometer();
	private Logger logger = LoggerFactory.getLogger(AdderImpl.class.getName());
	
	public Context add(Context context) throws RemoteException, ContextException {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {

        }
		Context out = arithmometer.add(context);
		logger.info("add result: " + out);
		
//		Logger contextLogger = provider.getContextLogger();
//		contextLogger.info("dataContext logging; add result: " + out);
//		
//		Logger providerLogger =  provider.getProviderLogger();
//		providerLogger.info("provider logging; add result: " + out);
//		try {
//			Thread.sleep(1000 * 60 * 2);
//			System.out.println("slept: " + 1000 * 60 * 2);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		checkpoint(out);
//		Logger remoteLogger =  provider.getRemoteLogger();
//		remoteLogger.info("remote logging; add result: " + out);
		
		return out;
	}
}
