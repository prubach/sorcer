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
import java.util.Arrays;
import java.util.List;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ServiceContext;
import sorcer.service.Condition;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;

/**
 * The alternative Exertion that executes sequentially a collection of optional
 * exertions. It executes the first optExertion in the collection such that its
 * condition is true.
 * 
 * @author Mike Sobolewski
 */
public class AltExertion extends Task {

	private static final long serialVersionUID = 4012356285896459828L;
	
	protected List<OptExertion> optExertions;

	public AltExertion(String name, OptExertion... optExertions) {
		super(name);
		this.optExertions = Arrays.asList(optExertions);
	}

	public AltExertion(String name, List<OptExertion> optExertions) {
		super(name);
		this.optExertions = optExertions;
	}

	@Override
	public Task doTask(Transaction txn) throws ExertionException,
			SignatureException, RemoteException {
		OptExertion opt = null;
		try {
			for (int i = 0; i < optExertions.size(); i++) {
				opt = optExertions.get(i);
				if (opt.condition.isTrue()) {
					optExertions.set(i, (OptExertion) opt.exert(txn));
					dataContext = (ServiceContext)optExertions.get(i).getContext();
					controlContext = optExertions.get(i).getControlContext();
					dataContext.putValue(Condition.CONDITION_VALUE, true);
					dataContext.putValue(Condition.CONDITION_TARGET, opt.getName());
					return this;
				}
			}
			dataContext.putValue(Condition.CONDITION_VALUE, false);
			dataContext.putValue(Condition.CONDITION_TARGET, opt.getName());
		} catch (Exception e) {
			throw new ExertionException(e);
		}
		return this;
	}

	public OptExertion getOptExertion(int index) {
		return optExertions.get(index);
	}
	
	public boolean isConditional() {
		return true;
	}
}
