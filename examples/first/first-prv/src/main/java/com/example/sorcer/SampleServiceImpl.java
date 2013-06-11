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
package com.example.sorcer;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.example.sorcer.ui.SampleUI;
import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.resolver.Resolver;
import sorcer.service.Context;
import sorcer.ui.serviceui.UIComponentFactory;
import sorcer.ui.serviceui.UIDescriptorFactory;
import sorcer.util.Artifact;
import sorcer.util.Log;
import sorcer.util.Sorcer;

public class SampleServiceImpl implements SampleService {

	private static Logger logger = Log.getTestLog();
	
	public Context sayHelloWorld(Context context) throws RemoteException {
		try {
			logger.info("SampleService Provider got a message: " + context);
			String input = (String) context.getValue("in/value");
			logger.info("SampleService Input = " + input);
			String output = "Hello there - " + input;
			context.putOutValue("out/value", output);
			logger.info("SampleService Provider sent a message" + context);
		} catch (Exception e) {
			logger.severe("SampleService Provider - problem interpreting message: " + context);
		}
		return context;		
	}
}