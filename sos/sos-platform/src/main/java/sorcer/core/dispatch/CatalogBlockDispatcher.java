/*
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

package sorcer.core.dispatch;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.par.ParModel;
import sorcer.core.exertion.AltExertion;
import sorcer.core.exertion.LoopExertion;
import sorcer.core.exertion.OptExertion;
import sorcer.core.provider.Provider;
import sorcer.service.Condition;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;

import static sorcer.service.Exec.*;

/**
 * A dispatching class for exertion blocks in the PUSH mode.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings({"rawtypes", "unchecked" })

public class CatalogBlockDispatcher extends CatalogSequentialDispatcher {

	public CatalogBlockDispatcher(Exertion block, Set<Context> sharedContext,
			boolean isSpawned, Provider provider,
            ProvisionManager provisionManager,
			ProviderProvisionManager providerProvisionManager) {
		super(block, sharedContext, isSpawned, provider, provisionManager, providerProvisionManager);
	}


    @Override
    protected void doExec() throws ExertionException, SignatureException {
        super.doExec();
		try {
			Condition.cleanupScripts(xrt);
		} catch (ContextException e) {
			throw new ExertionException(e);
		}
	}

    protected void dispatchExertion(ServiceExertion se) throws ExertionException, SignatureException {
        try {
            ((ServiceContext)se.getContext()).setBlockScope(xrt.getContext());

            // Provider is expecting exertion to be in context
            se.getContext().setExertion(se);
        } catch (ContextException ex) {
            throw new ExertionException(ex);
        }
        try {
            preUpdate(se);
            se = (ServiceExertion) execExertion(se);
        } catch (Exception ce) {
            throw new ExertionException(ce);
        }

        super.dispatchExertion(se);
        try {
            postUpdate(se);
        } catch (Exception e) {
            throw new ExertionException(e);
		}
	}

	private void preUpdate(ServiceExertion exertion) throws ContextException {
		if (exertion instanceof AltExertion) {
			for (OptExertion oe : ((AltExertion)exertion).getOptExertions()) {
				oe.getCondition().getConditionalContext().append(xrt.getContext());
				oe.getCondition().setStatus(null);
			}
		} else if (exertion instanceof OptExertion) {
			Context pc = ((OptExertion)exertion).getCondition().getConditionalContext();
			((OptExertion)exertion).getCondition().setStatus(null);
			if (pc == null) {
				pc = new ParModel(exertion.getName());
				((OptExertion)exertion).getCondition().setConditionalContext(pc);
			}
			pc.append(xrt.getContext());
		} else if (exertion instanceof LoopExertion) {
			((LoopExertion)exertion).getCondition().setStatus(null);
			Context pc = ((LoopExertion)exertion).getCondition().getConditionalContext();
			if (pc == null) {
				pc = new ParModel(exertion.getName());
				((LoopExertion)exertion).getCondition().setConditionalContext(pc);
			}
			pc.append(xrt.getContext());
		}
	}
	
	private void postUpdate(ServiceExertion exertion) throws ContextException, RemoteException {
		if (exertion instanceof AltExertion) {
			xrt.getContext().append(((AltExertion)exertion).getActiveOptExertion().getContext());
		} else if (exertion instanceof OptExertion) {
			xrt.getContext().append(exertion.getContext());
		}
		
//		if (exertion instanceof AltExertion) {
//			((ParModel)((Block)xrt).getContext()).appendNew(((AltExertion)exertion).getActiveOptExertion().getContext());
//		} else if (exertion instanceof OptExertion) {
//			((ParModel)((Block)xrt).getContext()).appendNew(((OptExertion)exertion).getContext());
//		}
		
		ServiceContext cxt = (ServiceContext)xrt.getContext();
		if (exertion.getContext().getReturnPath() != null)
			cxt.putOutValue(exertion.getContext().getReturnPath().path, exertion.getContext().getReturnValue()); 
		else 
			cxt.appendNewEntries(exertion.getContext());
		
		((ServiceContext)exertion.getContext()).setBlockScope(null);
//		if (cxt.getReturnPath() != null)
//			cxt.putValue(cxt.getReturnPath().path, cxt.getReturnValue()); 
	}

    protected List<Exertion> getInputExertions() {
        return xrt.getExertions();
	}
}
