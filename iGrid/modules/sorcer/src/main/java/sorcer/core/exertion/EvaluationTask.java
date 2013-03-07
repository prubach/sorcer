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
import java.util.Map;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.core.context.ServiceContext;
import sorcer.core.signature.EvaluationSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.Context;
import sorcer.service.Evaluation;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;

/**
 * The SORCER evaluation task extending the basic task implementation
 * {@link Task}.
 * 
 * @author Mike Sobolewski
 */
public class EvaluationTask extends Task {

	static final long serialVersionUID = -3710507950682812041L;

	public EvaluationTask(String name) {
		super(name);
	}

	public EvaluationTask(Evaluation evaluator) {
		super(evaluator.getName());
		if (getProcessSignature() == null) {
			EvaluationSignature es = new EvaluationSignature(evaluator);
			es.setEvaluator(evaluator);
			addSignature(es);
			context.setExertion(this);
		}
	}

	public EvaluationTask(EvaluationSignature signature) {
		this(null, signature, null);
	}

	public EvaluationTask(String name, EvaluationSignature signature) {
		super(name);
		addSignature(signature);
	}

	public EvaluationTask(EvaluationSignature signature, Context context) {
		this(null, signature, context);
	}

	public EvaluationTask(String name, EvaluationSignature signature,
			Context context) {
		super(name);
		addSignature(signature);
		if (context != null)
			setContext(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Task#doTask(net.jini.core.transaction.Transaction)
	 */
	@Override
	public Task doTask(Transaction txn) throws ExertionException,
			SignatureException {
		((ServiceContext)context).setCurrentSelector(getProcessSignature().getSelector());
		((ServiceContext)context).setCurrentPrefix(((ServiceSignature)getProcessSignature()).getPrefix());

		if (signatures.size() > 1) {
			try {
				return super.doBatchTask(txn);
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new ExertionException(e);
			}
		}
		
		context.appendTrace("" + getEvaluation());
		return this;
	}

	public Evaluation getEvaluation() {
		EvaluationSignature es = (EvaluationSignature) getProcessSignature();
		return es.getEvaluator();
	}

}
