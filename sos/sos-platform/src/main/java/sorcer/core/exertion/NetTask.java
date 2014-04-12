/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013, 2014 SorcerSoft.com S.A.
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
import java.util.Arrays;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import sorcer.core.provider.IExertExecutor;
import sorcer.core.signature.NetSignature;
import sorcer.service.*;

/**
 * The SORCER service task extending the abstract task {@link Task}.
 * 
 * @author Mike Sobolewski
 */
public class NetTask extends ObjectTask implements Evaluation<Object>, Invocation<Object> {

	private static final long serialVersionUID = -6741189881780105534L;

	public NetTask() {
		// do nothing
	}

	public NetTask(String name) {			
		super(name);
	}

	public NetTask(Uuid jobId, int jobState) {
		setParentId(jobId);
		status = jobState;
	}

	public NetTask(String name, String description) {
		this(name);
		this.description = description;
	}

	public NetTask(String name, Signature signature)
			throws SignatureException {
		this(name, null, signature, null);
	}

	public NetTask(String name, String description, Signature signature)
			throws SignatureException {
		this(name, description, signature, null);
	}

	public NetTask(String name, Signature signature, Context context)
			throws SignatureException {
		this(name, null, signature, context);
	}
	public NetTask(Signature signature, Context context)
			throws SignatureException {
		this(null, null, signature, context);
	}
	
	public NetTask(String name, String description, Signature signature,
			Context context) throws SignatureException {
		this(name, description);
		if (signature instanceof NetSignature)
			addSignature(signature);
		else
			throw new SignatureException("Net task requires NetSignature: "
					+ signature);
		if (context != null)
			setContext(context);
	}

	public NetTask(String name, Signature[] signatures, Context context)
			throws SignatureException {
		this(name);
		setContext(context);

		try {
			for (Signature s : signatures) {
				if (s instanceof NetSignature)
					((NetSignature) s).setExertion(this);
				else
					throw new SignatureException("Net task requires NetSignature: "
							+ s);
			}
		} catch (ExertionException e) {
			e.printStackTrace();
		}
		this.signatures.addAll(Arrays.asList(signatures));
	}

	public void setService(Service provider) {
		((NetSignature) getProcessSignature()).setProvider(provider);
	}

	public Service getService() {
		return ((NetSignature) getProcessSignature()).getService();
	}

	public Task doTask(Transaction txn) throws ExertionException,
			SignatureException, RemoteException {
        IExertExecutor exertExecutor = Accessor.getService(IExertExecutor.class);
        try {
            return (Task) exertExecutor.exert(this);
        } catch (TransactionException e) {
			throw new ExertionException(e);
		}
	}

	public static NetTask getTemplate() {
		NetTask temp = new NetTask();
		temp.status = Exec.INITIAL;
		temp.priority = null;
		temp.index = null;
		temp.signatures = null;
		return temp;
	}

	public static NetTask getTemplate(String provider) {
		NetTask temp = getTemplate();
		temp.getProcessSignature().setProviderName(provider);
		return temp;
	}

    public boolean isNet() {
        return true;
    }
}
