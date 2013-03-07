/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

package sorcer.core.exertion;

import java.rmi.RemoteException;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.jobber.ExertionJobber;
import sorcer.core.signature.ObjectSignature;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.Signature;
import sorcer.service.SignatureException;
import sorcer.util.obj.ObjectInvoker;

/**
 * The SORCER object job extending the basic job implementation {@link Job}.
 * 
 * @author Mike Sobolewski
 */
public class ObjectJob extends Job {

	static final long serialVersionUID = 1793342047789581449L;
	
	public ObjectJob(String name) {
		super(name);
		addSignature(new ObjectSignature("execute", ExertionJobber.class));
	}

	public ObjectJob(String name, Signature signature)
			throws SignatureException {
		super(name);
		if (signature instanceof ObjectSignature)
			addSignature(signature);
		else
			throw new SignatureException("ObjectJob requires ObjectSignature: "
					+ signature);
	}

	public ObjectJob(String name, Signature signature, Context context)
			throws SignatureException {
		this(name, signature);
		if (context != null)
			this.context = (ServiceContext) context;
	}
	
	public Job doJob(Transaction txn) throws ExertionException,
			SignatureException, RemoteException {
		// return (Job) new ExertionJobber().exec(job, txn);
		Job result = null;
		try {
			ObjectSignature os = (ObjectSignature) getProcessSignature();
			ObjectInvoker invoker = ((ObjectSignature) getProcessSignature())
					.getInvoker();
			if (invoker == null) {
				invoker = new ObjectInvoker(os.newInstance(),
						os.getSelector());
			}
			invoker.setParameterTypes(new Class[] { Exertion.class });
			invoker.setParameters(new Exertion[] { this });
			result = (Job)invoker.invoke();
			getControlContext().appendTrace("" + invoker);
		} catch (Exception e) {
			e.printStackTrace();
			context.reportException(e);
		}
		return result;
	}
	
}
