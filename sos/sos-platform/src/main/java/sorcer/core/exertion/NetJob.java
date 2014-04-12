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

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.core.provider.IExertExecutor;
import sorcer.core.provider.Jobber;
import sorcer.core.signature.NetSignature;
import sorcer.security.util.Auth;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.*;
import sorcer.service.Signature.Type;

public class NetJob extends Job implements Evaluation<Object>, Invocation<Object> {

	private static final long serialVersionUID = 7060442151643838169L;

	public NetJob() {
		// do nothing
	}

	public NetJob(String name) {
		super(name);
		try {
			signatures.add(new NetSignature("service", Jobber.class, Type.SRV));
		} catch (SignatureException e) {
			e.printStackTrace();
		}
	}

	public NetJob(SorcerPrincipal principal) throws ExertionException {
		this("undefined" + count++, principal);
	}

	public NetJob(String name, SorcerPrincipal principal)
			throws ExertionException {
		super(name);
		if (principal != null)
			subject = Auth.createSubject(principal);
		setPrincipal(principal);
	}

	public static ServiceExertion getTemplate() {
        return new NetJob();
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Job#doJob(net.jini.core.transaction.Transaction)
	 */
	@Override
	public Job doJob(Transaction txn) throws ExertionException,
			SignatureException, RemoteException, TransactionException {
        IExertExecutor exertExecutor = Accessor.getService(IExertExecutor.class);
		return (Job) exertExecutor.exert(this, txn);
	}

    public boolean isNet() {
        return true;
    }
}
