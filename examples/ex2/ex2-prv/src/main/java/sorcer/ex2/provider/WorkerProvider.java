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

import com.sun.jini.start.LifeCycle;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.ServiceTasker;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public class WorkerProvider extends ServiceTasker implements Worker {
	
	private String hostName;
	
	public WorkerProvider() throws Exception {
		hostName = SorcerEnv.getHostName();
	}
	
	public WorkerProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
		hostName = SorcerEnv.getHostName();
	}

	public Context sayHi(Context context) throws RemoteException,
			ContextException {
		context.putValue("provider/host/name", hostName);
		String reply = "Hi" + " " + context.getValue("requestor/name") + "!";
		setMessage(context, reply);
		return context;
	}

	public Context sayBye(Context context) throws RemoteException,
			ContextException {
		context.putValue("provider/host/name", hostName);
		String reply = "Bye" + " " + context.getValue("requestor/name") + "!";
		setMessage(context, reply);
		return context;
	}

	public Context doWork(Context context) throws InvalidWork, RemoteException,
			ContextException {
        context.putValue("provider/host/name", hostName);
        String sigPrefix = ((ServiceContext)context).getCurrentPrefix();
        String path = "requestor/work";
        if (sigPrefix != null && sigPrefix.length() > 0)
        	path = sigPrefix + "/" + path;
        Object workToDo = context.getValue(path);
        if (workToDo != null && (workToDo instanceof Work)) {
            // requestor's work to be done
            Context out = ((Work)workToDo).exec(context);
            //context.putValue(out.getReturnPath().path, out.getReturnValue());
        } else {
            throw new InvalidWork("No Work found to do at path requestor/work'!");
        }
		String reply = "Done work by: "
                + (getProviderName() == null ? getClass() : getProviderName());
		setMessage(context, reply);

		// simulate longer execution time based on the value in
		// configs/worker-prv.properties
		String sleep = getProperty("provider.sleep.time");
		logger.info("sleep=" + sleep);
		if (sleep != null)
			try {
				context.putValue("provider/slept/ms", sleep);
				Thread.sleep(Integer.parseInt(sleep));
			} catch (Exception e) {
				throw new ContextException(e);
			}
		return context;
	}
	
	private String setMessage(Context context, String reply)
			throws ContextException {
		String previous = (String) context.getValue("provider/message");
		String message = "";
		if (previous != null && previous.length() > 0)
			message = previous + "; " + reply;
		else
			message = reply;
		context.putValue("provider/message", message);
		return message;
	}
	
}
