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
import sorcer.core.signature.ObjectSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.Signature;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.obj.ObjectInvoker;

/**
 * The SORCER object task extending the basic task implementation {@link Task}.
 * 
 * @author Mike Sobolewski
 */
public class ObjectTask extends Task {

	static final long serialVersionUID = 1793342047789581449L;

	public ObjectTask() { }
	
	public ObjectTask(String name) {
		super(name);
	}
	
	public ObjectTask(String name, Signature signature)
			throws SignatureException {
		this(name, null, signature);
	}

	public ObjectTask(String name, String description, Signature signature)
			throws SignatureException {
		super(name);
		if (signature instanceof ObjectSignature)
			addSignature(signature);
		else 
			throw new SignatureException("Object task requires ObjectSignature: "
					+ signature);
		if (((ObjectSignature)signature).getInvoker() == null)
			try {
				((ObjectSignature)signature).createInvoker();
			} catch (Exception e) {
				e.printStackTrace();
				throw new SignatureException(e);
			}
		this.description = description;
	}
	
	public ObjectTask(String name, Signature signature, Context context)
			throws SignatureException {
		this(name, signature);
		this.context = (ServiceContext) context;
	}
	
	public Task doTask(Transaction txn) throws ExertionException, SignatureException, RemoteException {
			ObjectSignature os = (ObjectSignature) getProcessSignature();
		((ServiceContext)context).setCurrentSelector(getProcessSignature().getSelector());
		((ServiceContext)context).setCurrentPrefix(((ServiceSignature)getProcessSignature()).getPrefix());
		ObjectInvoker invoker = ((ObjectSignature) getProcessSignature())
				.getInvoker();
		if (invoker == null) {
			invoker = new ObjectInvoker(os.newInstance(), os.getSelector());
		}
		try {
			if (getProcessSignature().getReturnPath() != null)
				context.setReturnPath(getProcessSignature().getReturnPath());
			
			if (getArgs() == null && os.getTypes() == null) {
				// assume this task context is used by the signature's provider
				invoker.setParameterTypes(new Class[] { Context.class });
				invoker.setContext(context);
			} else if (context.getArgsPath() != null) {
				invoker.setArgs(os.getTypes(), (Object[]) getArgs());
			}
			//invoker.setParameters(context);
			Object result = invoker.invoke();
			if (result instanceof Context) {
				if (context.getReturnPath() != null)
					context.setReturnValue(((Context) result).getValue(context
						.getReturnPath().path));
			} else {
				context.setReturnValue(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			context.reportException(e);
		}
		context.appendTrace(invoker.toString());
		return this;
	}
	
	public Object getArgs() throws ContextException {
		return context.getArgs();
	}

}
