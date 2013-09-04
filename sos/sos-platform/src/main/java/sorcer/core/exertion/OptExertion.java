/*
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
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
import sorcer.service.Condition;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;

/**
 * The option Exertion. There is a single target exertion that executes if the condition is true (like if... then).
 * 
 * @author Mike Sobolewski
 * 
 */
@SuppressWarnings("rawtypes")
public class OptExertion extends Task {

	private static final long serialVersionUID = 172930501527871L;

	protected Condition condition;
	
	protected Exertion target;
	
	public OptExertion(String name) {
		super(name);
	}
		
	public OptExertion(String name, Exertion exertion) {
		super(name);
		this.condition = new Condition(true);
		this.target = exertion;
	}
	
	public OptExertion(String name, Condition condition, Exertion exertion) {
		super(name);
		this.condition = condition;
		this.target = exertion;
	}

	public Exertion getTarget() {
		return target;
	}

	public void setTarget(Exertion exertion) {
		this.target = exertion;
	}
	
	public Task doTask(Transaction txn) throws ExertionException,
			SignatureException, RemoteException {
		try {
			if (condition.isTrue()) {
				target = target.exert(txn);
				dataContext = (ServiceContext)target.getContext();
				controlContext = target.getControlContext();
				dataContext.putValue(Condition.CONDITION_VALUE, true);
				dataContext.putValue(Condition.CONDITION_TARGET, target.getName());
				return this;
			} else {
				dataContext.putValue(Condition.CONDITION_VALUE, false);
				dataContext.putValue(Condition.CONDITION_TARGET, target.getName());
				return this;
			}
		} catch (Exception e) {
			throw new ExertionException(e);
		}
	}
		
	public boolean isConditional() {
		return true;
	}

}
