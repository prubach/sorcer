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

import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace05;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.exertion.ExertionEnvelop;

class SpaceTaskWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SpaceTaskWorker.class);
    private ExertionEnvelop ee;
    private Transaction.Created tx;
    private JavaSpace05 space;


    public SpaceTaskWorker(JavaSpace05 space, ExertionEnvelop envelope, Transaction.Created tx) {
        this.space = space;
        ee = envelope;
        this.tx = tx;
    }

    public void run() {

    }
}
