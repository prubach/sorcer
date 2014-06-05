/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.entry.UnusableEntriesException;
import net.jini.id.Uuid;
import net.jini.space.JavaSpace05;
import sorcer.core.DispatchResult;
import sorcer.core.exertion.Jobs;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.exertion.NetJob;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.core.provider.SpaceTaker;
import sorcer.ext.ProvisioningException;
import sorcer.service.*;
import sorcer.service.space.SpaceAccessor;

import static sorcer.service.Exec.*;
import static sorcer.util.StringUtils.tName;

import java.rmi.RemoteException;

public class SpaceParallelDispatcher extends ExertDispatcher {

    protected JavaSpace05 space;
    protected int doneExertionIndex = 0;
    protected LokiMemberUtil loki;

    public SpaceParallelDispatcher(Exertion exertion,
           Set<Context> sharedContexts,
           boolean isSpawned,
           LokiMemberUtil loki,
           Provider provider,
           ProvisionManager provisionManager,
           ProviderProvisionManager providerProvisionManager) throws ExertionException, ContextException {
        super(exertion, sharedContexts, isSpawned, provider, provisionManager, providerProvisionManager);

        space = SpaceAccessor.getSpace();
        if (space == null) {
            throw new ExertionException("NO exertion space available!");
        }

        disatchGroup = new ThreadGroup("exertion-" + exertion.getId());
        disatchGroup.setDaemon(true);
        disatchGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);

        this.loki = loki;
	}

    @Override
    protected List<Exertion> getInputExertions() throws ContextException {
        if (xrt instanceof Job)
            return Jobs.getInputExertions((Job) xrt);
        else if (xrt instanceof Block)
            return xrt.getAllExertions();
        else
            return null;
    }

    @Override
    public void doExec() throws SignatureException, ExertionException {
        new Thread(disatchGroup, new CollectResultThread(), tName("collect-" + xrt.getName())).start();

        for (Exertion exertion : inputXrts) {
            dispatchExertion(exertion);
        }
	}

    protected void dispatchExertion(Exertion exertion) throws ExertionException, SignatureException {
        logger.debug("exertion #{}: exertion: {}", exertion.getIndex(), exertion);
        try {
            writeEnvelop(exertion);
            logger.debug("generateTasks ==> SPACE EXECUTE EXERTION: "
                    + exertion.getName());
        } catch (ProvisioningException pe) {
            xrt.setStatus(FAILED);
            throw new ExertionException(pe.getLocalizedMessage());
        } catch (RemoteException re) {
			logger.warn("Space not reachable....resetting space", re);
			space = SpaceAccessor.getSpace();
			if (space == null) {
				xrt.setStatus(FAILED);
				throw new ExertionException("NO exertion space available!");
			}
			if (masterXrt != null) {
				try {
					writeEnvelop(masterXrt);
				} catch (Exception e) {
					e.printStackTrace();
					xrt.setStatus(FAILED);
					throw new ExertionException(
							"Wrting master exertion into exertion space failed!",
							e);
				}
			}
		}
	}

	public void collectResults() throws ExertionException, SignatureException {
		int count = 0;
		// get all children of the underlying parent job
		ExertionEnvelop doneTemplate = ExertionEnvelop.getTakeTemplate(xrt.getId(),
				null);
        ExertionEnvelop failTmpl = ExertionEnvelop.getParentTemplate(xrt.getId(),
                null);
        failTmpl.state = FAILED;

        ExertionEnvelop errTmpl = ExertionEnvelop.getParentTemplate(xrt.getId(),
                null);
        errTmpl.state = ERROR;

        while(count < inputXrts.size() && state != FAILED) {
            Collection<ExertionEnvelop> results;
            try {
                results = space.take(Arrays.asList(doneTemplate, failTmpl, errTmpl), null, SpaceTaker.SPACE_TIMEOUT, Integer.MAX_VALUE);
                count += results.size();
            } catch (UnusableEntriesException e) {
                xrt.setStatus(FAILED);
                state = FAILED;
                Collection<UnusableEntryException> exceptions = e.getUnusableEntryExceptions();
                for (UnusableEntryException throwable : exceptions) {
                    logger.warn("UnusableEntryException! unusable fields = " + throwable.partialEntry, throwable);
                }
                handleError(results, FAILED);
                throw new ExertionException(e);
            } catch (Exception e) {
                throw new ExertionException("Taking exertion envelop failed", e);
            }
            for (ExertionEnvelop resultEnvelop : results) {
                logger.debug("collect exertions for template: {}",
              						doneTemplate.describe());
                ServiceExertion input = (ServiceExertion) ((NetJob) xrt)
                        .get(resultEnvelop.exertion
                                .getIndex());
                logger.debug("collected result envelope {}",
                        resultEnvelop.describe());
                ServiceExertion result = (ServiceExertion) resultEnvelop.exertion;
                postExecExertion(input, result);
            }
        }

        if(xrt.getStatus()!=FAILED) {
            executeMasterExertion();
            state = DONE;
        }
        dispatchers.remove(xrt.getId());
    }

    protected void addPoison(Exertion exertion) {
        space = SpaceAccessor.getSpace();
        if (space == null) {
            return;
        }
        ExertionEnvelop ee = new ExertionEnvelop();
        if (exertion == xrt)
            ee.parentID = exertion.getId();
        else
            ee.parentID = exertion.getParentId();
        ee.state = Exec.POISONED;
        try {
            space.write(ee, null, Lease.FOREVER);
            logger.debug("written poisoned envelop for: "
                    + ee.describe() + "\n to: " + space);
        } catch (Exception e) {
            logger.warn("writting poisoned ExertionEnvelop", e);
        }
    }

    protected synchronized void changeDoneExertionIndex(int index) {
        doneExertionIndex = index + 1;
        notifyAll();
    }

    // abstract in ExertionDispatcher
    protected void preExecExertion(Exertion exertion) throws ExertionException,
            SignatureException {
//		try {
//			exertion.getControlContext().appendTrace(provider.getProviderName()
//					+ " dispatcher: " + getClass().getName());
//		} catch (RemoteException e) {
//			// ignore it, local call
//		}
        try {
            updateInputs(exertion);
        } catch (ContextException e) {
            throw new ExertionException(e);
        }
        ((ServiceExertion) exertion).startExecTime();
        ((ServiceExertion) exertion).setStatus(RUNNING);
    }

    private void provisionProviderForExertion(Exertion exertion) throws ProvisioningException {
        providerProvisionManager.add(exertion, this);
    }

    protected void writeEnvelop(Exertion exertion) throws
            ExertionException, SignatureException, RemoteException, ProvisioningException {
        // setSubject before exertion is dropped
        space = SpaceAccessor.getSpace();
        if (space == null) {
            throw new ExertionException("NO exertion space available!");
        }

        if (exertion.isProvisionable())
            provisionProviderForExertion(exertion);

        ((ServiceExertion) exertion).setSubject(subject);
        preExecExertion(exertion);
        ExertionEnvelop ee = ExertionEnvelop.getTemplate(exertion);
        ee.state = INITIAL;
        try {
            space.write(ee, null, Lease.FOREVER);
            logger.debug("written envelop: "
                    + ee.describe() + "\n to: " + space);
        } catch (Exception e) {
            logger.warn("writeEnvelop", e);
            state = Exec.FAILED;
        }
    }

    protected ExertionEnvelop takeEnvelop(Entry template)
            throws ExertionException {
        space = SpaceAccessor.getSpace();
        if (space == null) {
            throw new ExertionException("NO exertion space available!");
        }
        ExertionEnvelop result;
        try {
            while (state == RUNNING) {
                result = (ExertionEnvelop) space.take(template, null, SpaceTaker.SPACE_TIMEOUT);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } catch (UnusableEntryException e) {
            logger.warn("UnusableEntryException! unusable fields = " + e.partialEntry, e);
            throw new ExertionException("Taking exertion envelop failed", e);
        } catch (Throwable e) {
            throw new ExertionException("Taking exertion envelop failed", e);
        }
    }

    protected void postExecExertion(Exertion ex, Exertion result)
            throws ExertionException, SignatureException {
        ((ServiceExertion) result).stopExecTime();
        try {
            result.getControlContext().appendTrace(provider.getProviderName()
                    + " dispatcher: " + getClass().getName());

            ((NetJob) xrt).setExertionAt(result, ex.getIndex());
            ServiceExertion ser = (ServiceExertion) result;
            if (ser.getStatus() > FAILED && ser.getStatus() != SUSPENDED) {
                ser.setStatus(DONE);
                collectOutputs(result);
            }
        } catch (Exception e) {
            throw new ExertionException(e);
        }
        changeDoneExertionIndex(result.getIndex());
    }

    protected void handleError(Exertion exertion, int state) {
        if (exertion != xrt)
            ((NetJob) xrt).setExertionAt(exertion,
                    exertion.getIndex());
        addPoison(exertion);
        this.state = state;
        cleanRemainingFailedExertions(xrt.getId());
    }

    private void cleanRemainingFailedExertions(Uuid id) {
        logger.debug("clean remaining failed exertions for {}", id);
        ExertionEnvelop template = ExertionEnvelop.getParentTemplate(id, null);
        ExertionEnvelop ee = null;

        do {
            try {
                logger.debug("take envelop {}", template);
                ee = takeEnvelop(template);
            } catch (ExertionException e) {
                logger.warn("Error while taking {}", template);
            }
        } while (ee != null);
    }

    protected void executeMasterExertion() throws
            ExertionException, SignatureException {
        if (masterXrt == null)
            return;
        logger.info("executeMasterExertion ==============> SPACE EXECUTE MASTER EXERTION");
        try {
            writeEnvelop(masterXrt);
        } catch (ProvisioningException pe) {
            masterXrt.setStatus(FAILED);
            throw new ExertionException(pe.getLocalizedMessage());
        } catch (RemoteException re) {
            re.printStackTrace();
            logger.warn("Space died....resetting space");
            space = SpaceAccessor.getSpace();
            if (space == null) {
                throw new ExertionException("NO exertion space available!");
            }
            try {
                writeEnvelop(masterXrt);
            } catch (Exception e) {
                logger.warn("Space died....could not recover", e);
            }
        }

        ExertionEnvelop template = ExertionEnvelop.getTakeTemplate(
                masterXrt.getParentId(), masterXrt.getId());

        ExertionEnvelop result = takeEnvelop(template);
        logger.debug("executeMasterExertion MASTER EXERTION RESULT RECEIVED");
        if (result != null && result.exertion != null) {
            postExecExertion(masterXrt, result.exertion);
        }
    }

    @Override
    public DispatchResult getResult() {
        return new DispatchResult(State.values()[state], xrt);
    }

}
