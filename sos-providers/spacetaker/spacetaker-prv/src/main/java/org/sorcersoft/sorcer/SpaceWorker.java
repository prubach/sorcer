/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package org.sorcersoft.sorcer;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace05;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.provider.Provider;
import sorcer.core.provider.ProviderDelegate;
import sorcer.river.TX;
import sorcer.service.Exec;
import sorcer.service.ServiceExertion;
import sorcer.service.Task;

class SpaceWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SpaceWorker.class);
    private ExertionEnvelop ee;
    private Transaction.Created tx;
    private JavaSpace05 space;


    public SpaceWorker(JavaSpace05 space, ExertionEnvelop envelope, Transaction.Created workerTxnCreated) {
        this.space = space;
        ee = envelope;
        tx = workerTxnCreated;
    }

    public void run() {
        Entry result = doEnvelope(ee, (tx == null) ? null
                : tx.transaction);

        if (result != null) {
            try {
                space.write(result, null, Lease.FOREVER);
            } catch (Exception e) {
                logger.warn("Error while writing the result", e);
                try {
                    TX.abortTransaction(tx);
                } catch (Exception e1) {
                    logger.warn("Error while aborting transaction", e1);
                    return;
                }
                return;
            }

            if (tx != null) {
                try {
                    TX.commitTransaction(tx);
                } catch (Exception e) {
                    logger.warn("Error while committing transaction", e);
                }
            }

        } else {
            if (tx != null) {
                try {
                    TX.abortTransaction(tx);
                } catch (Exception e) {
                    logger.warn("Error while aborting transaction", e);
                }
            }
        }
    }

    public Entry doEnvelope(ExertionEnvelop ee, Transaction transaction) {
        ServiceExertion se;
        ServiceExertion out = null;
        try {
            ee.exertion.getControlContext().appendTrace(
                    "taken by: " + data.provider.getProviderName() + ":"
                            + data.provider.getProviderID()
            );
            se = (ServiceExertion) ee.exertion;

            if (se instanceof Task) {

                // task for the worker's provider
                out = new ProviderDelegate(null, null).doTask((Task) se, transaction);
            }
            if (out != null) {
                out.setStatus(Exec.DONE);
                ee.state = Exec.DONE;
                ee.exertion = out;
            } else {
                se.setStatus(Exec.ERROR);
                ee.state = Exec.ERROR;
                ee.exertion = se;
            }
        } catch (Throwable th) {
            logger.debug("doEnvelope", th);
            if (th instanceof Exception) {
                ee.state = Exec.FAILED;
                ((ServiceExertion) ee.exertion).setStatus(Exec.FAILED);
            } else {
                ee.state = Exec.ERROR;
                ((ServiceExertion) ee.exertion).setStatus(Exec.ERROR);
            }
            ((ServiceExertion) ee.exertion).reportException(th);
        }
        return ee;
    }
}
